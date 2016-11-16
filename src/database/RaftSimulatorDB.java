package database;

import benchmark.KVDB;
import benchmark.Status;
import simulator.Client;
import simulator.Raft;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Properties;

public class RaftSimulatorDB implements KVDB<Long, Long> {

    private Client client;

    @Override
    public Map.Entry<Long, Long> cast(long key, byte[] value) {
        return new AbstractMap.SimpleEntry<Long, Long>(key, 2L);
    }

    @Override
    public void init(String address, Properties properties) {
        Raft.N  = Integer.valueOf(properties.getProperty("simulator.nodes"));
        System.out.println("N = " + Raft.N);
        Raft raft = Raft.instance();
        raft.start();
        client = new Client("Client" + (int) (Math.random() * 100));
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
        client.send();
        return Status.OK;
    }

    @Override
    public void cleanup() {
        client.terminate();
        Raft.instance().stop();
    }

    @Override
    public Status delete(Long key) {
        return null;
    }
}
