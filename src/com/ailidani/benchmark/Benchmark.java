package com.ailidani.benchmark;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class Benchmark {

    protected long recordcount;
    protected long operationcount;
    protected long snapshotcount;
    protected float getp;
    protected float putp;
    protected float removep;

    protected long interval; /* ms */
    protected int totalTime; /* second */

    protected Client[] clients;

    protected AtomicInteger finished = new AtomicInteger(0);
    protected List<Double> getLatency = Collections.synchronizedList(new ArrayList<>(10000));
    protected List<Double> putLatency = Collections.synchronizedList(new ArrayList<>(10000));
    protected List<Double> removeLatency = Collections.synchronizedList(new ArrayList<>(10000));

    public void init() {
        recordcount = Config.getRecordCount();
        operationcount = Config.getOperationCount();
        snapshotcount = Config.getSnapshotCount();
        getp = Config.getGetProportion();
        putp = Config.getPutProportion();
        removep = Config.getRemoveProportion();
        interval = Config.getInterval();
        totalTime = Config.getTotalTime();
    }


    /**
     * Barrier, cluster-wide synchronization aid that allows one or more threads to wait until every client is ready.
     *
     * @throws InterruptedException handles times out
     */
    public abstract void await() throws InterruptedException;

    /**
     * Pre-load some data into DB.
     */
    public abstract void load();

    /**
     * Main body.
     */
    public abstract void run();

    public abstract void shutdown();


    public static void main(String [] args) {

        Benchmark benchmark;

        switch (Config.getBenchmark()) {
            case "centralized":
            case "c":
                benchmark = new CentralizedBenchmark();
                break;
            case "distributed":
            case "d":
                benchmark = new DistributedBenchmark();
                break;
            default:
                System.err.println("Benchmark: illegal option benchmark");
                return;
        }

        benchmark.init();

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
//                case "-interval":
//                    if (i < args.length) benchmark.interval = Integer.parseInt(args[i++]);
//                    else System.err.println("-interval requires an integer argument (ms)");
//                    break;
//                case "-throttle":
//                    if (i < args.length) benchmark.throttle = Integer.parseInt(args[i++]);
//                    else System.err.println("-throttle requires an integer argument (ops/s)");
//                    break;
                default:
                    System.err.println("CentralizedBenchmark: illegal option " + arg);
            }
        }

        // Load the data if there are get or remove op
        if (benchmark.getp + benchmark.removep > 0) {
            benchmark.load();
        }

        benchmark.run();

        benchmark.shutdown();

        System.exit(0);

    }

}
