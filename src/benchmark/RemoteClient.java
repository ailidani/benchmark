package benchmark;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;

import java.util.concurrent.TimeUnit;

class RemoteClient extends Client implements HazelcastInstanceAware {

    private HazelcastInstance instance;

    RemoteClient(int id, long min, long max, String address) {
        super(id, min, max, address);
    }

    @Override
    protected void ready() throws InterruptedException {
        instance.getCountDownLatch("ready").countDown();
        System.out.printf("%s is ready\n", this);
        try {
            instance.getCountDownLatch("start").await(10, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            System.err.println("Client #" + id + " was interrupted while waiting on barrier.");
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    protected void publish(Stat stat) {

    }

    @Override
    public void setHazelcastInstance(HazelcastInstance instance) {
        this.instance = instance;
    }
}
