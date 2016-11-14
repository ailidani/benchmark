package benchmark;

import java.util.Map;

class LoadClient extends Client {

    LoadClient(int id, long min, long max, String address) {
        super(id, min, max, address);
    }

    @Override
    protected void ready() throws InterruptedException { }

    @Override
    protected void publish(Stat stat) { }

    @Override
    protected Stats go(FSDB db) {
        return null;
    }

    @Override
    protected Stats go(SQLDB db) {
        return null;
    }

    @Override
    protected Stats go(KVDB db) {
        long startTime = System.nanoTime();
        long start, end;

        for (long k = min; k < max; k++) {
            Map.Entry entry = db.cast(k, data);

            start = System.nanoTime();
            db.put(entry.getKey(), entry.getValue());
            end = System.nanoTime();
            stats.add( (end - start) / Stats.MStoNS );

            throttle(startTime);
        }

        return stats;
    }
}
