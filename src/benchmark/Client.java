package benchmark;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.LockSupport;

import static benchmark.Operation.GET;
import static benchmark.Operation.PUT;
import static benchmark.Operation.REMOVE;

public abstract class Client implements Callable<Stats>, Serializable {

    protected static final long serialVersionUID = 42L;

    protected int id;
    protected long min;
    protected long max;
    protected String address;

    protected transient Config config;
    protected transient DB db;

    private transient Stats stats = new Stats();
    private transient long startTime;
    private volatile transient boolean complete = false;
    private volatile transient long done = 0;
    private transient double throttle;
    private transient long throttleTick;
    private final transient Map<Operation, List<Double>> latency = new HashMap<>();

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

    /**
     * Load key from min to max into DB
     * @param size value data size in bytes
     */
    protected void load(int size) {
        byte[] data = new byte[size];
        new Random().nextBytes(data);
        for (long i = min; i <= max; i++) {
            Map.Entry entry = db.cast(i, data);
            db.put(entry.getKey(), entry.getValue());
        }
    }

    public int getId() {
        return id;
    }

    public Client setId(int id) {
        this.id = id;
        return this;
    }

    public long getMin() {
        return min;
    }

    public Client setMin(long min) {
        this.min = min;
        return this;
    }

    public long getMax() {
        return max;
    }

    public Client setMax(long max) {
        this.max = max;
        return this;
    }

    public String getAddress() {
        return address;
    }

    public Client setAddress(String address) {
        this.address = address;
        return this;
    }

    public Client setConfig(Config config) {
        this.config = config;
        return this;
    }

    protected void init() {
        db = loadDB();
        db.init(address, config.get());

        if (config.getGetProportion() + config.getRemoveProportion() > 0) {
            load(config.getDataSize());
        }

        if (config.getGetProportion() > 0) {
            stats.put(GET, new Stat());
            latency.put(GET, Collections.synchronizedList(new ArrayList<>(10000)));
        }
        if (config.getPutProportion() > 0) {
            stats.put(PUT, new Stat());
            latency.put(PUT, Collections.synchronizedList(new ArrayList<>(10000)));
        }
        if (config.getRemoveProportion() > 0) {
            stats.put(REMOVE, new Stat());
            latency.put(REMOVE, Collections.synchronizedList(new ArrayList<>(10000)));
        }

        double t = (double) config.getThrottle() / 1000.0; // ops/ms
        this.throttle = t >= 0 ?  (t / (double) config.getClients()) : -1.0; // ops/ms/client
        this.throttleTick = (long)(1000000.0 / throttle); // ns
    }

    protected abstract void ready() throws InterruptedException;


    @Override
    public Stats call() throws Exception {
        init();

        ready();

        Timer ftimer = new Timer();
        ftimer.scheduleAtFixedRate(new FinishTimer(), config.getInterval(), config.getInterval());

        long n = max - min + 1;
        Random random = new Random();
        byte[] v = new byte[config.getDataSize()];
        new Random().nextBytes(v);

        // let clients start with random delay
        if (throttle > 0 && throttle <= 1.0) {
            long randomdelay = random.nextInt((int)throttle);
            long deadline = System.nanoTime() + randomdelay;
            long now;
            while ((now = System.nanoTime()) < deadline) {
                LockSupport.parkNanos(deadline - now);
            }
        }

        float get = config.getGetProportion();
        float put = get + config.getPutProportion();
        float remove = put + config.getRemoveProportion();

        startTime = System.nanoTime();

        Timer utimer = new Timer();
        utimer.scheduleAtFixedRate(new UpdateTimer(), config.getInterval(), config.getInterval());

        while (true) {

            long k = random.nextLong() % n + min;
            Map.Entry entry = db.cast(k, v);

            double r = Math.random();
            long s, e;
            if (r <= get) {
                s = System.nanoTime();
                db.get(entry.getKey());
                e = System.nanoTime();
                stats.get(GET).count++;
                latency.get(GET).add( (e - s) / 1000000.0);
            }

            else if (r <= put) {
                s = System.nanoTime();
                db.put(entry.getKey(), entry.getValue());
                e = System.nanoTime();
                stats.get(PUT).count++;
                latency.get(PUT).add( (e - s) / 1000000.0 );
            }

            else if (r <= remove) {
                s = System.nanoTime();
                db.remove(entry.getKey());
                e = System.nanoTime();
                stats.get(REMOVE).count++;
                latency.get(REMOVE).add( (e - s) / 1000000.0 );
            }

            done++;
            if (complete) {
                utimer.cancel();
                return stats;
            }
            throttle(startTime);

        }
    }

    private void throttle(long startTime) {
        if (throttle > 0) {
            long deadline = startTime + done * throttleTick;
            long now;
            while ((now = System.nanoTime()) < deadline) {
                LockSupport.parkNanos(deadline - now);
            }
        }
    }

    /**
     * Periodic timer task to generate current statistics
     */
    private class UpdateTimer extends TimerTask {

        private long totalFinished = 0;
        private long lastFinished = 0;
        private long lastGet = 0;
        private long lastPut = 0;
        private long lastRemove = 0;
        private long lastTime = startTime;

        private void throughput(Operation o) {
            long currentTime = System.nanoTime();
            double elapsed = (currentTime - startTime) / 1000000000.0;
            long sum = 0;
            switch (o) {
                case GET:
                    sum = stats.get(o).count - lastGet;
                    break;
                case PUT:
                    sum = stats.get(o).count - lastPut;
                    break;
                case REMOVE:
                    sum = stats.get(o).count - lastRemove;
                    break;
            }

            double throughput = (double)(sum) / ((double)(currentTime - lastTime) / 1000000000.0);
            stats.get(o).throughput.add(throughput);

            System.out.printf("Client[%d] Elapsed time %s seconds ", id, elapsed);
            System.out.printf("Throughput = %f (ops/s)\n", throughput);
            lastTime = currentTime;
        }

        private void latency(Operation o) {
            int tail = latency.get(o).size();
            int head;
            List<Double> current;
            switch (o) {
                case GET:
                    head = (int)lastGet;
                    break;
                case PUT:
                    head = (int)lastPut;
                    break;
                case REMOVE:
                    head = (int)lastRemove;
                    break;
                default:
                    return;

            }
            if (tail - head <= 1) return;
            current = new ArrayList<>(latency.get(o).subList(head, tail));
            Collections.sort(current);
            int size = current.size();
            double sum = 0;
            for (double latency : current) {
                sum += latency;
            }
            double mean = sum / (double)size;
            double min = current.get(0);
            double max = current.get(size - 1);
            double ninefive = current.get((int)(size * 0.95));
            double ninenine = current.get((int)(size * 0.99));
            stats.get(o).latency.add(mean);
            stats.get(o).latency95.add(ninefive);
            stats.get(o).latency99.add(ninenine);
            stats.get(o).latencymin.add(min);
            stats.get(o).latencymax.add(max);
        }

        @Override
        public void run() {
            totalFinished = done;
            if (totalFinished - lastFinished == 0) return;

            for (Map.Entry<Operation, Stat> entry : stats) {
                Operation o = entry.getKey();
                throughput(o);
                latency(o);
                switch (o) {
                    case GET:
                        lastGet = stats.get(o).count;
                        break;
                    case PUT:
                        lastPut = stats.get(o).count;
                        break;
                    case REMOVE:
                        lastRemove = stats.get(o).count;
                        break;

                }
            }

            lastFinished = totalFinished;
        }
    }

    private class FinishTimer extends TimerTask {
        long deadline = (config.getTotalTime() * 1000) / config.getInterval();
        long count = 0;
        @Override
        public void run() {
            count++;
            if (count == deadline) {
                this.cancel();
                complete = true;
            }
        }
    }

    @Override
    public String toString() {
        return String.format("Client id[%d] key[%d-%d] address[%s]", id, min, max, address);
    }
}
