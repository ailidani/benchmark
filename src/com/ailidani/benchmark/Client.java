package com.ailidani.benchmark;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.core.ReplicatedMap;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.LockSupport;

public class Client implements Callable<Stat>, Serializable, HazelcastInstanceAware {

    private static final long serialVersionUID = 42L;

    private int id;
    private long min;
    private long max;
    private String address;

    // TODO should be injected
    private transient DB db;
    //private transient Benchmark benchmark;

    private boolean complete = false;
    private long done = 0;
    private double throttle;
    private long throttleTick;

    private Stat stat;

    private transient HazelcastInstance instance;

    public Client(int id, long min, long max, String address) {
        this.id = id;
        this.address = address;
        this.min = min;
        this.max = max;

        double t = (double) Config.getThrottle() / 1000.0; // ops/ms
        this.throttle = t >= 0 ?  (t / (double) Config.getClients()) : -1.0; // ops/ms/client
        this.throttleTick = (long)(1000000.0 / throttle); // ns
    }

    private void loadConfig() {
        ReplicatedMap<String, String> config = instance.getReplicatedMap("config");

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
            Class dbclass = classLoader.loadClass(Config.getDBName());
            db = (DB) dbclass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        return db;
    }

    public void load() {
        db.init(address);
        byte[] v = new byte[Config.getDataSize()];
        new Random().nextBytes(v);
        for (long i = min; i <= max; i++) {
            Map.Entry entry = db.cast(i, v);
            db.put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public Stat call() {
        db = loadDB();
        db.init(address);

        float get = Config.getGetProportion();
        float put = get + Config.getPutProportion();
        float delete = put + Config.getRemoveProportion();

        try {
            benchmark.await();
        } catch (InterruptedException e) {
            System.err.println("Client #" + id + " was interrupted while waiting on barrier.");
            e.printStackTrace();
        }

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new FinishTimer(), benchmark.interval, benchmark.interval);

        long n = max - min + 1;
        Random random = new Random();
        byte[] v = new byte[Config.getDataSize()];
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

        long start = System.nanoTime();

        while (true) {

            long k = random.nextLong() % n + min;
            Map.Entry entry = db.cast(k, v);

            double r = Math.random();
            long s, e;
            if (r <= get) {
                s = System.nanoTime();
                db.get(entry.getKey());
                e = System.nanoTime();
                benchmark.getLatency.add( (e - s) / 1000000.0 );
            }

            else if (r <= put) {
                s = System.nanoTime();
                db.put(entry.getKey(), entry.getValue());
                e = System.nanoTime();
                benchmark.putLatency.add( (e - s) / 1000000.0 );
            }

            else if (r <= delete) {
                s = System.nanoTime();
                db.remove(entry.getKey());
                e = System.nanoTime();
                benchmark.removeLatency.add( (e - s) / 1000000.0 );
            }

            benchmark.finished.incrementAndGet();
            done++;
            if (complete) {
                return stat;
            }
            throttle(start);

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

    class FinishTimer extends TimerTask {
        long deadline = (benchmark.totalTime * 1000) / benchmark.interval;
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
    public void setHazelcastInstance(HazelcastInstance instance) {
        this.instance = instance;
    }

    @Override
    public String toString() {
        return String.format("Client id[%d] key[%d-%d] address[%s]", id, min, max, address);
    }
}
