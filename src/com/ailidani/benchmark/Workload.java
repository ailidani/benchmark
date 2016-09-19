package com.ailidani.benchmark;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Workload {

    public enum Type { CENTRALIZED, DISTRIBUTED }

    String dbname = "com.ailidani.benchmark.HZDB";
    Type type = Type.CENTRALIZED;
    long recordCount;
    int dataSize = 100;
    long interval = 1000;
    int totalTime = 60;
    int throttle = -1;
    int clients = 1;
    String address = "127.0.0.1";
    String groupName = "test";
    String groupPassword = "test";
    float getProportion = 0.9f;
    float putProportion = 0.1f;
    float removeProportion = 0;
    int snapshotCount = 0;


    private static Workload instance = new Workload();
    private Properties properties = new Properties();

    private Workload() {
        load(System.getProperty("config", "benchmark.properties"));
    }

    private void load(String filename) {
        File configfile = new File(filename);
        try (FileInputStream inputStream = new FileInputStream(configfile)) {
            properties.clear();
            properties.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load " + configfile, e);
        }
    }

    public static String getDBName() {
        return instance.dbname;
    }
}
