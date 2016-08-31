package com.ailidani.benchmark;

import java.io.Serializable;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.locks.LockSupport;

public class Client implements Runnable, Serializable {

    private int id;
    private String address;
    // TODO should be injected
    protected transient DB db;
    private transient Benchmark benchmark;

    volatile boolean complete = false;
    int throttle = -1;
    long throttleTick;

    public Client(int id, String address, DB db, Benchmark benchmark) {
        this.id = id;
        this.address = address;
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
        } catch (BrokenBarrierException e) {
            System.err.println("Some other client was interrupted. Client #" + id + " is out of sync");
        }

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new FinishTimer(), benchmark.interval, benchmark.interval);

        long start = System.nanoTime();
        //double r;
        while (true) {

            db.data.forEach((k,v) -> {
                double r = Math.random();
                if (r <= get) {
                    db.get(k);
                } else if (r <= put) {
                    db.put(k, v);
                } else if (r <= delete) {
                    db.delete(k);
                }
                benchmark.finished.incrementAndGet();
                if (complete) {
                    return;
                }
                throttle(start);
            });
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
