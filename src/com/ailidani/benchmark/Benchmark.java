package com.ailidani.benchmark;

import java.util.*;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

public class Benchmark {

    private DB db;
    private int recordcount;
    private int operationcount;
    private int snapshotcount;
    // ms
    protected long interval;
    // second
    protected int totalTime;
    private String address;
    private int throttle;

    private Client[] clients;
    private Map<Integer, Thread> running = new HashMap<>();
    protected AtomicInteger finished = new AtomicInteger(0);

    protected CyclicBarrier barrier;

    private long startTime, lastTime;
    private int lastFinished = 0;

    private void init() {
        db = loadDB();
        recordcount = Config.getRecordCount();
        operationcount = Config.getOperationCount();
        snapshotcount = Config.getSnapshotCount();
        interval = Config.getInterval();
        totalTime = Config.getTotalTime();
        address = Config.getAddress();
        throttle = Config.getThrottle();
        int n = Config.getClients();
        clients = new Client[n];
        for (int i = 0; i < n; i++) {
            Client c = new Client(i, Config.getAddress(), db, this);
            clients[i] = c;
            running.put(i, new Thread(c));
        }
        barrier = new CyclicBarrier(n + 1);
    }

    private DB loadDB() {
        ClassLoader classLoader = Benchmark.class.getClassLoader();
        DB db = null;
        try {
            Class dbclass = classLoader.loadClass(Config.getDBName());
            db = (DB) dbclass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return db;
    }

    private void load() {
        System.out.println("Loading...");

        System.out.println("Loading Done.");
    }


    private void run() {
        System.out.println("Running benchmark for " + totalTime + " seconds... ");
        startTime = System.nanoTime();
        lastTime = startTime;

        for (Thread thread : running.values()) {
            thread.start();
        }

        try {
            barrier.await();
        } catch (BrokenBarrierException e) {
            System.err.println("Some other client was interrupted; Benchmark main thread is out of sync");
        } catch (InterruptedException e) {
            System.err.println("Benchmark main thread was interrupted while waiting on barrier");
        }

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

    private void shutdown() {
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

    public static void main(String [] args) {
        Benchmark benchmark = new Benchmark();
        int i = 0;
        while (i < args.length && args[i].startsWith("-")) {
            String arg = args[i++];
            switch (arg) {
                case "-recordcount":
                    if (i < args.length) benchmark.recordcount = Integer.parseInt(args[i++]);
                    else System.err.println("-recordcount requires an integer argument");
                    break;
                case "-operations":
                    if (i < args.length) benchmark.operationcount = Integer.parseInt(args[i++]);
                    else System.err.println("-operations requires an integer argument");
                    break;
                case "-interval":
                    if (i < args.length) benchmark.interval = Integer.parseInt(args[i++]);
                    else System.err.println("-interval requires an integer argument (ms)");
                    break;
                case "-throttle":
                    if (i < args.length) benchmark.throttle = Integer.parseInt(args[i++]);
                    else System.err.println("-throttle requires an integer argument (ops/s)");
                    break;
                default:
                    System.err.println("Benchmark: illegal option " + arg);
            }
        }

        benchmark.init();

        benchmark.load();

        benchmark.run();

        benchmark.shutdown();

        System.exit(0);
    }

}
