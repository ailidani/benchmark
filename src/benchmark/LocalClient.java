package benchmark;

public class LocalClient extends Client {

    public LocalClient(int id, long min, long max, String address) {
        super(id, min, max, address);
    }

    @Override
    protected void ready() throws InterruptedException {
        try {
            Coordinator.get().ready();
        } catch (InterruptedException e) {
            System.err.println("Client #" + id + " was interrupted while waiting on barrier.");
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    protected void publish(Stat stat) {
        Coordinator.get().publish(stat);
    }
}
