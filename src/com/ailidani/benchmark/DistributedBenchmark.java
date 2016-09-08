package com.ailidani.benchmark;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ICountDownLatch;
import com.hazelcast.core.IExecutorService;

public class DistributedBenchmark extends Benchmark {

    private HazelcastInstance instance;

    protected ICountDownLatch barrier;
    protected IExecutorService executor;

    @Override
    public void init() {
        this.instance = Hazelcast.newHazelcastInstance();
        this.barrier = instance.getCountDownLatch("");
        this.executor = instance.getExecutorService("");
    }


    @Override
    public void run() {

    }

    @Override
    public void load() {

    }

    @Override
    public void shutdown() {

    }
}
