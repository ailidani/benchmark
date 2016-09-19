package com.ailidani.benchmark;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public abstract class Configuration {

    private long nextCheckTime = 0;
    private long lastLoadTime = 0;
    private Properties properties = new Properties();
    private final File configFile;

    protected Configuration(String filename) {
        this.configFile = new File(filename);
    }

    public String getProperty(String name, String defaultValue) {
        String result = getProperty(name);
        if (result == null) {
            System.err.printf("Missing property %s in %s\n", name, properties.keySet());
            return defaultValue;
        }
        return result;
    }

    public String getRequiredProperty(String name) {
        String result = getProperty(name);
        if (result == null) {
            throw new RuntimeException("Missing property " + name);
        }
        return result;
    }

    public boolean getFlag(String name, boolean defaultValue) {
        return Boolean.parseBoolean(getProperty(name, String.valueOf(defaultValue)));
    }

    public int getIntProperty(String name, int defaultValue) {
        return Integer.parseInt(getProperty(name, String.valueOf(defaultValue)));
    }

    public long getLongProperty(String name, long defaultValue) {
        return Long.parseLong(getProperty(name, String.valueOf(defaultValue)));
    }

    public float getFloatProperty(String name, float defaultValue) {
        return Float.parseFloat(getProperty(name, String.valueOf(defaultValue)));
    }

    public Properties getProperties() {
        ensureConfigurationIsFresh();
        return properties;
    }

    private String getProperty(String name) {
        if (System.getProperty(name) != null) {
            System.err.printf("Reading %s from system properties\n", name);
            return System.getProperty(name);
        }
        if (System.getenv(name.replace('.', '_')) != null) {
            System.err.printf("Reading %s from environment\n", name);
            return System.getenv(name.replace('.', '_'));
        }

        ensureConfigurationIsFresh();
        return properties.getProperty(name);
    }

    private synchronized void ensureConfigurationIsFresh() {
        if (System.currentTimeMillis() < nextCheckTime) return;
        nextCheckTime = System.currentTimeMillis() + 10000;
        //System.out.printf("Rechecking %s", configFile);

        if (!configFile.exists()) {
            System.err.printf("Missing configuration file %s\n", configFile);
        }

        if (lastLoadTime >= configFile.lastModified()) return;
        lastLoadTime = configFile.lastModified();
        //System.out.printf("Reloading %s", configFile);

        try (FileInputStream inputStream = new FileInputStream(configFile)) {
            properties.clear();
            properties.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load " + configFile, e);
        }
    }

}
