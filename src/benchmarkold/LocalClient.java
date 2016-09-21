package benchmarkold;

public class LocalClient extends Client {

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
}
