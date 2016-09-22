package database;

import benchmark.DB;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class SimulatorDB extends DB<Long, byte[]> {

    Map<Long, byte[]> database = new HashMap<>();

    @Override
    public Map.Entry<Long, byte[]> cast(long k, byte[] v) {
        return new AbstractMap.SimpleEntry<>(k, v);
    }

    @Override
    public void init(String address, Properties properties) {}

    @Override
    public void cleanup() {}

    @Override
    public byte[] get(Long key) {
        return new byte[0];
    }

    @Override
    public byte[] put(Long key, byte[] value) {
        return new byte[0];
    }

    @Override
    public byte[] remove(Long key) {
        return new byte[0];
    }

    @Override
    public boolean snapshot() {
        return false;
    }
}
