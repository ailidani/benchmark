package benchmark;

import java.util.Map;

public class LoadClient extends Client {

    public LoadClient(int id, long min, long max, String address) {
        super(id, min, max, address);
    }

    @Override
    protected void ready() throws InterruptedException { }

    @Override
    protected void publish(Stat stat) { }

    @Override
    public Stats call() throws Exception {

        init();
        delay();

        long startTime = System.nanoTime();
        long s, e;
        for (long k = min; k < max; k++) {
            Map.Entry entry = db.cast(k, data);

            s = System.nanoTime();
            db.put(entry.getKey(), entry.getValue());
            e = System.nanoTime();
            stats.add( (e - s) / Stats.MStoNS );

            throttle(startTime);
        }

        return stats;
    }
}
