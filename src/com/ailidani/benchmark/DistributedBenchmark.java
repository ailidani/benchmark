package com.ailidani.benchmark;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ICountDownLatch;
import com.hazelcast.core.IExecutorService;

public class DistributedBenchmark {

    private HazelcastInstance instance;

    protected ICountDownLatch barrier;
    protected IExecutorService executor;

    public void init() {
        this.instance = Hazelcast.newHazelcastInstance();
        this.barrier = instance.getCountDownLatch("");
        this.executor = instance.getExecutorService("");
    }


    public void run() {

    }

}
