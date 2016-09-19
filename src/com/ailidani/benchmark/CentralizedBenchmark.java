package com.ailidani.benchmark;

import java.util.*;
import java.util.concurrent.*;

public class CentralizedBenchmark extends Benchmark {

    private String address;
    private int throttle;

    /**
     * Centralized executor has same number of threads as number of clients.
     */
    private ExecutorService executor;
    private Map<Client, Future<Stat>> futures = new HashMap<>();

    private CountDownLatch barrier;

    private long startTime, lastTime;

    @Override
    public void init() {
        super.init();
        address = Config.getAddress();
        throttle = Config.getThrottle();
        long min = 1;
        long max = recordcount;
        int n = Config.getClients();
        executor = Executors.newFixedThreadPool(n);
        clients = new Client[n];
        for (int i = 0; i < n; i++) {
            // TODO add overlap
            Client c = new Client(i, Config.getAddress(), min, max, db, this);
            clients[i] = c;
        }
        //barrier = new CyclicBarrier(n + 1);
        barrier = new CountDownLatch(1);
    }

    @Override
    public void await() throws InterruptedException {
        this.barrier.await();
    }

    @Override
    public void load() {
        System.out.println("Loading...");
        Client client = new Client(0, Config.getAddress(), 1, recordcount, db, this);
        client.load();
        System.out.println("Loading Done.");
    }

    @Override
    public void run() {
        System.out.println("Running benchmark for " + totalTime + " seconds... ");
        startTime = System.nanoTime();
        lastTime = startTime;

        for (Client client : clients) {
            futures.put(client, executor.submit(client));
        }

        barrier.countDown();

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new UpdateTimer(), interval, interval);

        long step = totalTime * 1000 / (snapshotcount + 1);
        for (int i = 0; i < snapshotcount; i++) {
            Timer snapshot = new Timer();
            snapshot.schedule(new SnapshotTimer(), step * (i+1));
        }

        for (Client client : clients) {
            try {
                Stat result = futures.get(client).get();
            } catch (InterruptedException e) {
                // ignore
            } catch (ExecutionException e) {
                System.err.printf("%s has error.", client);
            }
        }

        timer.cancel();

        System.out.printf("Total operations done = %d \n", finished.get());
        System.out.printf("Get operations done = %d \n", getLatency.size());
        System.out.printf("Put operations done = %d \n", putLatency.size());
        System.out.printf("Remove operations done = %d \n", removeLatency.size());
    }

    @Override
    public void shutdown() {
        System.out.println("Shutting down...");
        db.cleanup();
        System.out.println("Done.");
    }

    /**
     * Periodic timer task to generate current statistics
     */
    private class UpdateTimer extends TimerTask {

        private int lastFinished = 0;
        private int putLastIndex = 0;
        private int getLastIndex = 0;

        private void throughput() {
            int totalFinished = finished.get();
            if (totalFinished == 0) return;

            long currentTime = System.nanoTime();
            double elapsed = (currentTime - startTime) / 1000000000.0;
            double throughput = (double)(totalFinished - lastFinished) / ((double)(currentTime - lastTime) / 1000000000.0);
            System.out.printf("Elapsed time %s seconds [", elapsed);
            System.out.printf("Throughput = %f (ops/s)\n", throughput);
            lastTime = currentTime;
            lastFinished = totalFinished;
        }

        private void measure(List<Double> latencies) {
            if (latencies.size() == 0) return;
            Collections.sort(latencies);
            int size = latencies.size();
            double sum = 0;
            for (double latency : latencies) {
                sum += latency;
            }
            double mean = sum / (double)size;
            double min = latencies.get(0);
            double max = latencies.get(size - 1);
            double ninefive = latencies.get((int)(size * 0.95));
            double ninenine = latencies.get((int)(size * 0.99));
            System.out.printf("Average Latency = %f (ms)\n", mean);
            System.out.printf("Min Latency = %f (ms)\n", min);
            System.out.printf("Max Latency = %f (ms)\n", max);
            System.out.printf("95th Percentile Latency = %f (ms)\n", ninefive);
            System.out.printf("99th Percentile Latency = %f (ms) ]\n", ninenine);
        }

        @Override
        public void run() {
            throughput();

            // todo ailidani jump to next block instead of return
            synchronized (putLatency) {
                int n = putLatency.size();
                if (n == 0) return;
                List<Double> latencies = new ArrayList<>(putLatency.subList(putLastIndex, n));
                measure(latencies);
                putLastIndex = n;
            }

            synchronized (getLatency) {
                int n = getLatency.size();
                if (n == 0) return;
                List<Double> latencies = new ArrayList<>(getLatency.subList(getLastIndex, n));
                measure(latencies);
                getLastIndex = n;
            }

        }
    }

    /**
     * Periodic timer task to take snapshot
     */
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
