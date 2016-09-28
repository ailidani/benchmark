package benchmark;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public final class Config implements Serializable {

    public static final String FILE_NAME = "benchmark.properties";
    public static final String MAP_NAME = "config";
    public static final String GROUP_NAME = "benchmark";
    public static final String GROUP_PASS = "benchmark";

    private final Properties properties = new Properties();

    public Map<String, String> asMap() {
        Map<String, String> map = new HashMap<String, String>();
        for (Map.Entry entry : properties.entrySet()) {
            String key = (String) entry.getKey();
            map.put(key, get(key));
        }
        return map;
    }

    public void fromMap(Map<String, String> config) {
        properties.clear();
        properties.putAll(config);
    }

    public void load() {
        File configFile = new File(System.getProperty("config", FILE_NAME));
        if (!configFile.exists()) {
            System.err.printf("File %s in %s did not exists.\n", FILE_NAME, configFile.getAbsolutePath());
            return;
        }
        try (FileInputStream inputStream = new FileInputStream(configFile)) {
            properties.clear();
            properties.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load " + configFile, e);
        }
    }

    private String get(String name) {
        return get(name, null);
    }

    private String get(String name, String value) {
        String result = System.getProperty(name);
        if (result != null) {
            System.err.printf("Reading %s from system properties.\n", name);
            return result;
        }
        result = System.getenv(name.replace('.', '_'));
        if (result != null) {
            System.err.printf("Reading %s from environment.\n", name);
            return result;
        }
        result = properties.getProperty(name);
        if (result != null) {
            return result;
        }
        return value;
    }

    private String getRequired(String name) {
        String result = properties.getProperty(name);
        if (result == null) {
            throw new RuntimeException("Missing property " + name);
        }
        return result;
    }

    private boolean getFlag(String name, boolean value) {
        return Boolean.parseBoolean(get(name, String.valueOf(value)));
    }

    private int getInt(String name, int value) {
        return Integer.parseInt(get(name, String.valueOf(value)));
    }

    private long getLong(String name, long value) {
        return Long.parseLong(get(name, String.valueOf(value)));
    }

    private float getFloat(String name, float value) {
        return Float.parseFloat(get(name, String.valueOf(value)));
    }

    private double getDouble(String name, double value) {
        return Double.parseDouble(get(name, String.valueOf(value)));
    }

    public Properties get() {
        return properties;
    }

    public void set(String name, String value) {
        properties.setProperty(name, value);
    }

    public String getDB() {
        return get("db", "database.SimulatorDB");
    }

    public Mode getBenchmarkMode() {
        return Mode.valueOf(get("mode", "CENTRALIZED"));
    }

    public long getRecordCount() {
        long c = getLong("recordcount", 1000);
        assert c >= 0 : "[ERROR] Config: recordcount cannot be negative";
        return c;
    }

    public KeyGenerator.Distribution getDistribution() {
        return KeyGenerator.Distribution.valueOf(get("distribution", "Uniform"));
    }

    public double getParameter() {
        return getDouble("parameter", 0.2);
    }

    public float getOverlap() {
        float overlap = getFloat("overlap", 1.0f);
        if (overlap < 0 || overlap > 1) overlap = 1.0f;
        return overlap;
    }

    public int getDataSize() {
        return getInt("datasize", 100 /* bytes */);
    }

    public long getOperationCount() {
        return getLong("operationcount", 0);
    }

    public long getInterval() {
        return getLong("interval", 1000 /* milliseconds */);
    }

    public int getTotalTime() {
        return getInt("totaltime", 60 /* seconds */);
    }

    public int getThrottle() {
        return getInt("throttle", -1 /* ops/second */);
    }

    public int getClients() {
        return getInt("clients", 1);
    }

    public String getAddress() {
        return get("address", "127.0.0.1");
    }

    public float getGetProportion() {
        float p = getFloat("getproportion", 0);
        if (p < 0 || p > 1) p = 0;
        return p;
    }

    public float getPutProportion() {
        float p = getFloat("putproportion", 1);
        if (p < 0 || p > 1) p = 0;
        return p;
    }

    public float getRemoveProportion() {
        float p = getFloat("removeproportion", 0);
        if (p < 0 || p > 1) p = 0;
        return p;
    }

    public long getSnapshotCount() {
        return getLong("snapshotcount", 0);
    }
}
