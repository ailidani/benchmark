package com.ailidani.benchmark;

import java.util.*;

public class CentralizedBenchmark extends Benchmark {

    private String address;
    private int throttle;


    private Map<Integer, Thread> running = new HashMap<>();

    //protected CyclicBarrier barrier;

    private long startTime, lastTime;
    private int lastFinished = 0;

    @Override
    public void init() {
        super.init();
        address = Config.getAddress();
        throttle = Config.getThrottle();
        int n = Config.getClients();
        clients = new Client[n];
        for (int i = 0; i < n; i++) {
            // TODO add overlap
            Client c = new Client(i, Config.getAddress(), 1, recordcount, db, this);
            clients[i] = c;
            running.put(i, new Thread(c));
        }
        //barrier = new CyclicBarrier(n + 1);
    }

    @Override
    public void load() {
        System.out.println("Loading...");
        db.init(address);
        db.data.forEach( (k,v) -> db.put(k, v) );
        System.out.println("Loading Done.");
    }

    @Override
    public void run() {
        System.out.println("Running benchmark for " + totalTime + " seconds... ");
        startTime = System.nanoTime();
        lastTime = startTime;

        for (Thread thread : running.values()) {
            thread.start();
        }

        barrier.countDown();

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new UpdateTimer(), interval, interval);

        int step = totalTime * 1000 / (snapshotcount + 1);
        for (int i=0; i<snapshotcount; i++) {
            Timer snapshot = new Timer();
            snapshot.schedule(new SnapshotTimer(), step * (i+1));
        }

        for (Thread thread : running.values()) {
            try {
                thread.join();
            } catch (InterruptedException e) {}
        }

        timer.cancel();

        System.out.printf("Total operations done = %d \n", finished.get());
    }

    @Override
    public void shutdown() {
        System.out.println("Shutting down...");
        db.cleanup();
        System.out.println("Done.");
    }

    private class UpdateTimer extends TimerTask {
        @Override
        public void run() {
            int totalFinished = finished.get();
            if (totalFinished == 0) return;

            long currentTime = System.nanoTime();
            double elapsed = (currentTime - startTime) / 1000000000.0;
            double throughput = (double)(totalFinished - lastFinished) / ((double)(currentTime - lastTime) / 1000000000.0);
            System.out.printf("Current Throughput = %f (ops/s)\n", throughput);



            lastTime = currentTime;
            lastFinished = totalFinished;
        }
    }

    private class SnapshotTimer extends TimerTask {
        @Override
        public void run() {
            long start = System.nanoTime();
            clients[0].db.snapshot();
            long end = System.nanoTime();
            System.out.printf("Snapshot at %f second took %f ms.\n", (start - startTime) / 1000000000.0, (end - start) / 1000000.0);
        }
    }

}
