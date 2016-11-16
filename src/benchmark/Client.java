package benchmark;

import java.io.Serializable;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;

abstract class Client implements Callable<Stat>, Serializable {

    protected static final long serialVersionUID = 42L;

    protected int id;
    protected long min;
    protected long max;
    protected String address;
    protected Config config;

    // injected
    protected transient DB db;
    protected transient Generator generator;
    private transient Timer updateTimer;
    private transient Timer finishTimer;

    protected transient Stats stats;
    protected transient AtomicBoolean complete;
    private transient double throttle;
    private transient long throttleTick;
    protected transient byte[] data;


    public Client(int id, long min, long max, String address) {
        this.id = id;
        this.min = min;
        this.max = max;
        this.address = address;
    }

    public Config config() {
        return config;
    }

    public Client setConfig(Config config) {
        this.config = config;
        return this;
    }

    /**
     * Called by finish timer.
     */
    public void complete() {
        this.complete.compareAndSet(false, true);
    }

    protected void init() {
        config.setLogLevel();
        Log.debug("Client", this + " is initiating...");
        stats = new Stats();
        complete = new AtomicBoolean(false);
        updateTimer = new Timer();
        finishTimer = new Timer();

        db = loadDB();
        db.init(address, config.get());

        generator = new Generator(min, max, config.getDistribution());
        generator.setParameter(config.getParameter());
        generator.setOperations(config.getOperations());

        double t = (double) config.getThrottle() / 1000.0; // ops/ms
        this.throttle = t >= 0 ?  (t / (double) config.getClients()) : -1; // ops/ms/client
        this.throttleTick = (long)(1000000.0 / throttle); // ns

        data = new byte[config.getDataSize()];
        ThreadLocalRandom.current().nextBytes(data);

        Log.debug("Client", this + " is initiated.");
    }

    protected abstract void ready() throws InterruptedException;

    protected abstract void publish(Stat stat);

    /**
     * Let clients start with random delay
     */
    protected void delay() {
        if (throttle > 0 && throttle <= 1.0) {
            long randomdelay = ThreadLocalRandom.current().nextLong( (long) throttle);
            long deadline = System.nanoTime() + randomdelay;
            long now;
            while ((now = System.nanoTime()) < deadline) {
                LockSupport.parkNanos(deadline - now);
            }
        }
    }

    void throttle(long startTime) {
        if (throttle > 0) {
            long deadline = startTime + stats.size() * throttleTick;
            long now;
            while ((now = System.nanoTime()) < deadline) {
                LockSupport.parkNanos(deadline - now);
            }
        }
    }

    /**
     * DB interface implementation loader
     *
     * @return DB instance
     */
    protected DB loadDB() {
        ClassLoader classLoader = Client.class.getClassLoader();
        DB db = null;
        try {
            Class dbclass = classLoader.loadClass(config.getDB());
            db = (DB) dbclass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        return db;
    }

    @Override
    public Stat call() throws Exception {

        init();

        ready();

        delay();

        finishTimer.scheduleAtFixedRate(new FinishTimer(this), config.getInterval(), config.getInterval());

        updateTimer.scheduleAtFixedRate(new UpdateTimer(this), config.getInterval(), config.getInterval());

        if (db instanceof KVDB) {
            return go((KVDB) db);
        }

        else if (db instanceof SQLDB) {
            return go((SQLDB) db);
        }

        else if (db instanceof FSDB) {
            return go((FSDB) db);
        }

        else {
            System.err.println("Unknown DB type.");
            return null;
        }
    }

    protected Stat go(FSDB db) {
        long startTime = System.nanoTime();
        long start = 0;
        long end = 0;
        while (true) {
            switch (generator.nextOperation()) {
                case CREATE:
                    start = System.nanoTime();
                    //db.create()
                    end = System.nanoTime();
                    break;
                case DELETE:
                    start = System.nanoTime();
                    end = System.nanoTime();
                    break;
                case READ:
                    start = System.nanoTime();
                    end = System.nanoTime();
                    break;
                case WRITE:
                    start = System.nanoTime();
                    end = System.nanoTime();
                    break;
                default:
                    Log.error("Unknown operation type in FSDB.");
            }
            stats.add( (end - start) / Stats.MStoNS );
            if (complete.get()) {
                updateTimer.cancel();
                db.cleanup();
                return stats.overall();
            }
            throttle(startTime);
        }
    }

    protected Stat go(SQLDB db) {
        long startTime = System.nanoTime();
        long start = 0;
        long end = 0;
        while (true) {

            switch (generator.nextOperation()) {
                case QUERY:
                    start = System.nanoTime();
                    //db.query();
                    end = System.nanoTime();
                    break;
                case INSERT:
                    start = System.nanoTime();
                    //db.insert();
                    end = System.nanoTime();
                    break;
                case UPDATE:
                    start = System.nanoTime();
                    //db.update();
                    end = System.nanoTime();
                    break;
                case SCAN:
                    start = System.nanoTime();
                    //db.scan();
                    end = System.nanoTime();
                    break;
                case DELETE:
                    start = System.nanoTime();
                    //db.delete();
                    end = System.nanoTime();
                    break;
                default:
                    Log.error("Unknown operation type in SQLDB.");
            }
            stats.add( (end - start) / Stats.MStoNS );
            if (complete.get()) {
                updateTimer.cancel();
                db.cleanup();
                return stats.overall();
            }
            throttle(startTime);
        }
    }

    protected Stat go(KVDB db) {
        long startTime = System.nanoTime();
        long start = 0;
        long end = 0;
        while (true) {
            long k = generator.next();
            Map.Entry entry = db.cast(k, data);

            switch (generator.nextOperation()) {
                case GET:
                    start = System.nanoTime();
                    db.get(entry.getKey());
                    end = System.nanoTime();
                    break;
                case PUT:
                    start = System.nanoTime();
                    db.put(entry.getKey(), entry.getValue());
                    end = System.nanoTime();
                    break;
                case SET:
                    start = System.nanoTime();
                    db.set(entry.getKey(), entry.getValue());
                    end = System.nanoTime();
                    break;
                case REMOVE:
                    start = System.nanoTime();
                    db.remove(entry.getKey());
                    end = System.nanoTime();
                    break;
                case DELETE:
                    start = System.nanoTime();
                    db.delete(entry.getKey());
                    end = System.nanoTime();
                    break;
                default:
                    Log.error("Unknown operation type in KVDB.");
                    break;
            }
            stats.add( (end - start) / Stats.MStoNS );

            if (complete.get()) {
                updateTimer.cancel();
                db.cleanup();
                return stats.overall();
            }
            throttle(startTime);
        }

    }

    @Override
    public String toString() {
        return String.format("Client [id=%d key=%d-%d address=%s] ", id, min, max, address);
    }

}
