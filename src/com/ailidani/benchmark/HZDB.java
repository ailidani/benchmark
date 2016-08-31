package com.ailidani.benchmark;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

public class HZDB extends DB<Integer, byte[]> {

    private String NAME = "test";
    HazelcastInstance client;
    IMap<Integer, byte[]> map;

    @Override
    public void init(String address) {
        ClientConfig config = new ClientConfig();
        config.getNetworkConfig().setSmartRouting(false);
        config.getNetworkConfig().addAddress(address);
        config.getGroupConfig().setName(Config.getGroupName()).setPassword(Config.getGroupPassword());
        client = HazelcastClient.newHazelcastClient(config);
        map = client.getMap(NAME);
    }

    @Override
    public void cleanup() {
        client.shutdown();
    }

    @Override
    public byte[] get(Integer key) {
        return map.get(key);
    }

    @Override
    public void put(Integer key, byte[] value) {
        map.put(key, value);
    }

    @Override
    public void delete(Integer key) {
        map.delete(key);
    }

    @Override
    public boolean snapshot() {
        return false;
    }
}
