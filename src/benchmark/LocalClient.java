package benchmark;

class LocalClient extends Client {

    LocalClient(int id, long min, long max, String address) {
        super(id, min, max, address);
    }

    @Override
    protected void ready() throws InterruptedException {
        try {
            Log.debug("Client #" + id + " is ready.");
            Coordinator.get().ready();
        } catch (InterruptedException e) {
            Log.error(this + " was interrupted while waiting on barrier.");
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    protected void publish(Stat stat) {
        Coordinator.get().publish(stat);
    }
}
