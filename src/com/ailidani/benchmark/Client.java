package com.ailidani.benchmark;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.locks.LockSupport;

public class Client implements Runnable, Serializable {

    private int id;
    private String address;
    private long min;
    private long max;

    // TODO should be injected
    transient DB db;
    private transient Benchmark benchmark;

    private volatile boolean complete = false;
    private int throttle = -1;
    private long throttleTick;

    private List<Double> getLatency = new ArrayList<>();
    private List<Double> putLatency = new ArrayList<>();
    private List<Double> deleteLatency = new ArrayList<>();

    public Client(int id, String address, long min, long max, DB db, Benchmark benchmark) {
        this.id = id;
        this.address = address;
        this.min = min;
        this.max = max;
        this.db = db;
        this.benchmark = benchmark;

        int t = Config.getThrottle() * 1000; // ops/ms
        this.throttle = t >= 0 ?  (t / Config.getClients()) : -1; // ops/ms/client
        this.throttleTick = (long)(1000000.0 / throttle); // ns
    }

    @Override
    public void run() {
        db.init(address);
        float get = Config.getGetProportion();
        float put = get + Config.getPutProportion();
        float delete = put + Config.getDeleteProportion();

        try {
            benchmark.barrier.await();
        } catch (InterruptedException e) {
            System.err.println("Client #" + id + " was interrupted while waiting on barrier");
            e.printStackTrace();
        }

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new FinishTimer(), benchmark.interval, benchmark.interval);

        long n = max - min;
        byte[] v = new byte[Config.getDataSize()];
        new Random().nextBytes(v);
        long start = System.nanoTime();

        while (true) {

            long k = new Random().nextLong() % n + min;
            Map.Entry entry = db.next(k, v);

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
                db.delete(entry.getKey());
                e = System.nanoTime();
                benchmark.deleteLatency.add( (e - s) / 1000000.0 );
            }

            benchmark.finished.incrementAndGet();
            if (complete) {
                return;
            }
            throttle(start);


//            db.data.forEach((k,v) -> {
//                double r = Math.random();
//                long s, e;
//                if (r <= get) {
//                    s = System.nanoTime();
//                    db.get(k);
//                    e = System.nanoTime();
//                    benchmark.getLatency.add( (e - s) / 1000000.0 );
//                }
//
//                else if (r <= put) {
//                    s = System.nanoTime();
//                    db.put(k, v);
//                    e = System.nanoTime();
//                    benchmark.putLatency.add( (e - s) / 1000000.0 );
//                }
//
//                else if (r <= delete) {
//                    s = System.nanoTime();
//                    db.delete(k);
//                    e = System.nanoTime();
//                    benchmark.deleteLatency.add( (e - s) / 1000000.0 );
//                }
//
//                benchmark.finished.incrementAndGet();
//                if (complete) {
//                    return;
//                }
//                throttle(start);
//            });
        }

    }

    private void throttle(long startTime) {
        if (throttle > 0) {
            long deadline = startTime + benchmark.finished.get() * throttleTick;
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

}
