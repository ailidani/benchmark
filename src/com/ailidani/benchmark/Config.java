package com.ailidani.benchmark;

import java.util.Properties;

public class Config extends Configuration {

    private static Config instance = new Config();

    private Config() {
        super(System.getProperty("config", "benchmark.properties"));
    }

    public static Config instance() {
        return instance;
    }

    public static Properties get() {
        return instance.getProperties();
    }

    public static String getDBName() {
        return instance.getProperty("db", "com.ailidani.benchmark.HZDB");
    }

    public static String getBenchmark() {
        return instance.getProperty("benchmark", "centralized");
    }

    public static int getRecordCount() {
        return instance.getIntProperty("recordcount", 1000);
    }

    public static int getDataSize() {
        return instance.getIntProperty("datasize", 100);
    }

    public static int getOperationCount() {
        return instance.getIntProperty("operationcount", 0);
    }

    public static long getInterval() {
        return instance.getLongProperty("interval", 1000 /* milliseconds */);
    }

    public static int getTotalTime() {
        return instance.getIntProperty("totalTime", 60 /* seconds */);
    }

    public static int getThrottle() {
        return instance.getIntProperty("throttle", -1 /* ops/second */);
    }

    public static int getClients() {
        return instance.getIntProperty("clients", 1);
    }

    public static String getAddress() {
        return instance.getProperty("address", "127.0.0.1");
    }

    public static String getGroupName() {
        return instance.getProperty("group.name", "test");
    }

    public static String getGroupPassword() {
        return instance.getProperty("group.password", "test");
    }

    public static float getGetProportion() {
        float p = instance.getFloatProperty("getproportion", 0);
        if (p < 0 || p > 1) p = 0;
        return p;
    }

    public static float getPutProportion() {
        float p = instance.getFloatProperty("putproportion", 1);
        if (p < 0 || p > 1) p = 0;
        return p;
    }

    public static float getDeleteProportion() {
        float p = instance.getFloatProperty("deleteproportion", 0);
        if (p < 0 || p > 1) p = 0;
        return p;
    }

    public static int getSnapshotCount() {
        return instance.getIntProperty("snapshotcount", 0);
    }
}
