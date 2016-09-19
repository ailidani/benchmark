package benchmark;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Properties;

public class HazelcastDB extends DB<Long, byte[]> {

    HazelcastInstance client;
    IMap<Long, byte[]> map;

    @Override
    public void init(String address, Properties properties) {
        String name = properties.getProperty("group.name");
        String pass = properties.getProperty("group.password");
        ClientConfig config = new ClientConfig();
        config.getNetworkConfig().addAddress(address);
        config.getGroupConfig().setName(name).setPassword(pass);
        client = HazelcastClient.newHazelcastClient(config);
        map = client.getMap(name);
    }

    @Override
    public void cleanup() {
        client.shutdown();
    }

    @Override
    public Map.Entry<Long, byte[]> cast(long k, byte[] v) {
        return new AbstractMap.SimpleEntry<>(k, v);
    }

    @Override
    public byte[] get(Long key) {
        return map.get(key);
    }

    @Override
    public byte[] put(Long key, byte[] value) {
        return map.put(key, value);
    }

    @Override
    public byte[] remove(Long key) {
        return map.remove(key);
    }

    @Override
    public void set(Long key, byte[] value) {
        map.set(key, value);
    }

    @Override
    public void delete(Long key) {
        map.delete(key);
    }

    @Override
    public boolean snapshot() {
        return false;
    }
}
