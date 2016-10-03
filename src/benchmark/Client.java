package benchmark;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;

public abstract class Client implements Callable<Stats>, Serializable {

    protected static final long serialVersionUID = 42L;

    protected int id;
    protected long min;
    protected long max;
    protected String address;
    protected Config config;

    // injected
    protected transient DB db;
    protected transient KeyGenerator generator;

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
        stats = new Stats();
        complete = new AtomicBoolean(false);

        db = loadDB();
        db.init(address, config.get());

        generator = new KeyGenerator(min, max, config.getDistribution());
        generator.setParameter(config.getParameter());

        double t = (double) config.getThrottle() / 1000.0; // ops/ms
        this.throttle = t >= 0 ?  (t / (double) config.getClients()) : -1; // ops/ms/client
        this.throttleTick = (long)(1000000.0 / throttle); // ns

        data = new byte[config.getDataSize()];
        ThreadLocalRandom.current().nextBytes(data);

        System.out.printf("%s is initiated\n", this);
    }

    protected abstract void ready() throws InterruptedException;

    protected abstract void publish(Stat stat);

    /**
     * Let clients start with random delay
     */
    protected void delay() {
        if (throttle > 0 && throttle <= 1.0) {
            long randomdelay = ThreadLocalRandom.current().nextLong((long)throttle);
            long deadline = System.nanoTime() + randomdelay;
            long now;
            while ((now = System.nanoTime()) < deadline) {
                LockSupport.parkNanos(deadline - now);
            }
        }
    }

    protected void throttle(long startTime) {
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
    private DB loadDB() {
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
    public Stats call() throws Exception {
        init();

        float get = config.getGetProportion();
        float put = get + config.getPutProportion();
        float remove = put + config.getRemoveProportion();

        ready();

        delay();

        Timer ftimer = new Timer();
        ftimer.scheduleAtFixedRate(new FinishTimer(this), config.getInterval(), config.getInterval());

        Timer utimer = new Timer();
        utimer.scheduleAtFixedRate(new UpdateTimer(this), config.getInterval(), config.getInterval());

        long startTime = System.nanoTime();
        long s, e;
        while (true) {

            long k = generator.next();
            Map.Entry entry = db.cast(k, data);

            double r = Math.random();

            if (r <= get) {
                s = System.nanoTime();
                db.get(entry.getKey());
                e = System.nanoTime();
                stats.add( (e - s) / Stats.MStoNS );
            }

            else if (r <= put) {
                s = System.nanoTime();
                db.put(entry.getKey(), entry.getValue());
                e = System.nanoTime();
                stats.add( (e - s) / Stats.MStoNS );
            }

            else if (r <= remove) {
                s = System.nanoTime();
                db.remove(entry.getKey());
                e = System.nanoTime();
                stats.add( (e - s) / Stats.MStoNS );
            }

            if (complete.get()) {
                utimer.cancel();
                return stats;
            }
            throttle(startTime);
        }
    }

    @Override
    public String toString() {
        return String.format("Client id[%d] key[%d-%d] address[%s] ", id, min, max, address);
    }

}
