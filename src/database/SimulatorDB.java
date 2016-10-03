package database;

import benchmark.DB;
import benchmark.KeyGenerator;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class SimulatorDB extends DB<Long, byte[]> {

    Map<Long, byte[]> database = new HashMap<>();

    private KeyGenerator generator;

    private void sleep() {
        try {
            Thread.sleep(generator.next());
        } catch (InterruptedException e) {
        }
    }

    @Override
    public Map.Entry<Long, byte[]> cast(long k, byte[] v) {
        return new AbstractMap.SimpleEntry<>(k, v);
    }

    @Override
    public void init(String address, Properties properties) {
        generator = new KeyGenerator(0, 100, KeyGenerator.Distribution.Uniform);
    }

    @Override
    public void cleanup() {}

    @Override
    public byte[] get(Long key) {
        sleep();
        return database.get(key);
    }

    @Override
    public byte[] put(Long key, byte[] value) {
        sleep();
        return database.put(key, value);
    }

    @Override
    public byte[] remove(Long key) {
        sleep();
        return database.remove(key);
    }

    @Override
    public boolean snapshot() {
        return false;
    }
}
