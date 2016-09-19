package com.ailidani.benchmark;

import com.hazelcast.config.Config;
import com.hazelcast.core.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class DistributedBenchmark extends Benchmark {

    private HazelcastInstance instance;

    protected ICountDownLatch barrier;
    protected IExecutorService executor;

    private String address;
    private int throttle;
    private long startTime, lastTime;

    @Override
    public void init() {
        super.init();
        address = com.ailidani.benchmark.Config.getAddress();
        throttle = com.ailidani.benchmark.Config.getThrottle();
        int n = com.ailidani.benchmark.Config.getClients();
        clients = new Client[n];
        for (int i = 0; i < n; i++) {
            // TODO add overlap
            Client c = new Client(i, 1, recordcount, com.ailidani.benchmark.Config.getAddress(), this);
            clients[i] = c;
        }
        com.hazelcast.config.Config config = new Config("benchmark");
        config.getGroupConfig().setName("benchmark").setPassword("benchmark1735");
        this.instance = Hazelcast.newHazelcastInstance(config);
        this.barrier = instance.getCountDownLatch("");
        this.barrier.trySetCount(1);
        this.executor = instance.getExecutorService("");
    }

    @Override
    public void await() throws InterruptedException {
        this.barrier.await(60, TimeUnit.MINUTES);
    }

    @Override
    public void run() {
        System.out.println("Running benchmark for " + totalTime + " seconds... ");
        Map<Client, Future<Stat>> futures = new HashMap<>();
        startTime = System.nanoTime();
        for (Client c : clients) {
            futures.put(c, this.executor.submit(c));
        }

        barrier.countDown();

        for (Client c : clients) {
            try {
                futures.get(c).get();
            } catch (InterruptedException | ExecutionException e) {}
        }


    }

    @Override
    public void load() {

    }

    @Override
    public void shutdown() {

    }
}
