package com.ailidani.benchmark;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class Benchmark {

    protected DB db;
    protected int recordcount;
    protected int operationcount;
    protected int snapshotcount;

    protected long interval; /* ms */
    protected int totalTime; /* second */

    protected Client[] clients;

    protected CountDownLatch barrier;

    protected AtomicInteger finished = new AtomicInteger(0);
    protected List<Double> getLatency = Collections.synchronizedList(new ArrayList<>(10000));
    protected List<Double> putLatency = Collections.synchronizedList(new ArrayList<>(10000));
    protected List<Double> deleteLatency = Collections.synchronizedList(new ArrayList<>(10000));

    public void init() {
        db = loadDB();
        recordcount = Config.getRecordCount();
        operationcount = Config.getOperationCount();
        snapshotcount = Config.getSnapshotCount();
        interval = Config.getInterval();
        totalTime = Config.getTotalTime();
        barrier = new CountDownLatch(1);
    }

    private DB loadDB() {
        ClassLoader classLoader = CentralizedBenchmark.class.getClassLoader();
        DB db = null;
        try {
            Class dbclass = classLoader.loadClass(Config.getDBName());
            db = (DB) dbclass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return db;
    }

    public abstract void load();

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

        // Load the data if there are get or delete op
        if (Config.getGetProportion() + Config.getDeleteProportion() > 0) {
            benchmark.load();
        }

        benchmark.run();

        benchmark.shutdown();

        System.exit(0);

    }

}
