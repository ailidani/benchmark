package benchmark;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;

import java.util.concurrent.TimeUnit;

public class RemoteClient extends Client implements HazelcastInstanceAware {

    private HazelcastInstance instance;

    public RemoteClient(int id, long min, long max, String address) {
        super(id, min, max, address);
    }

    @Override
    protected void ready() throws InterruptedException {
        instance.getCountDownLatch("ready").countDown();
        try {
            instance.getCountDownLatch("start").await(10, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            System.err.println("Client #" + id + " was interrupted while waiting on barrier.");
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public void setHazelcastInstance(HazelcastInstance instance) {
        this.instance = instance;
    }
}