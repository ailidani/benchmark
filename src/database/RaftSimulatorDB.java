package database;

import benchmark.KVDB;
import benchmark.Status;
import simulator.Raft;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Properties;

public class RaftSimulatorDB implements KVDB<Long, Long> {

    private Raft.Client client;

    @Override
    public Map.Entry<Long, Long> cast(long key, byte[] value) {
        return new AbstractMap.SimpleEntry<Long, Long>(key, 2L);
    }

    @Override
    public void init(String address, Properties properties) {
        // int n = Integer.valueOf(properties.getProperty("simulator.nodes"));
        Raft raft = Raft.instance();
        raft.start();
        client = raft.new Client("Client");
        new Thread(client).start();
    }

    @Override
    public Long get(Long key) {
        return null;
    }

    @Override
    public Long put(Long key, Long value) {
        return null;
    }

    @Override
    public Long remove(Long key) {
        return null;
    }

    @Override
    public Status set(Long key, Long value) {
        client.send(key, value);
        return Status.OK;
    }

    @Override
    public void cleanup() {

    }

    @Override
    public Status delete(Long key) {
        return null;
    }
}
