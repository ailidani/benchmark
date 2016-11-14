package benchmark;

import java.io.Serializable;
import java.util.*;

class Stats implements Serializable, Iterable<Stat> {

    private static final long serialVersionUID = 42L;

    public static final double StoMS = 1000.0;
    public static final double MStoNS = 1000000.0;
    public static final double StoNS = 1000000000.0;

    private Vector<Stat> stats = new Vector<>();

    private transient Vector<Double> latencies = new Vector<>();
    private transient int index = 0;

    public synchronized void add(double latency /* ms */) {
        this.latencies.add(latency);
    }

    public synchronized Stat slice() {
        List<Double> slice = new ArrayList<>(latencies.subList(index, latencies.size()));
        Stat stat = new Stat();
        int n = slice.size();
        if (n == 0) {
            stats.add(stat);
            return stat;
        }
        Collections.sort(slice);
        double sum /* ms */ = 0;
        for (double i : slice) {
            sum += i;
        }
        double elapsed /* s */ = sum / StoMS;
        stat.setCount(n);
        stat.setThroughput((double) n / elapsed);
        stat.setLatency(sum / (double) n);
        stat.setLatency95( slice.get((int) (n * 0.95)) );
        stat.setLatency99( slice.get((int) (n * 0.99)) );
        stat.setLatencymin(slice.get(0));
        stat.setLatencymax(slice.get(n - 1));
        stats.add(stat);
        index = n;
        Log.info("Stats", "Client Elapsed time " + elapsed + " seconds Throughput = " + stat.getThroughput() + " (op/s)");
        // System.out.printf("Client[] Elapsed time %s seconds ", elapsed);
        // System.out.printf("Throughput = %f (ops/s)\n", stat.getThroughput());
        return stat;
    }

    public boolean isEmpty() {
        return stats.isEmpty();
    }

    public int size() {
        return latencies.size();
    }

    public void add(Stats that) {
        if (that.isEmpty()) return;
        if (this.isEmpty()) {
            this.stats = that.stats;
        } else {
            for (int i = 0; i < that.stats.size(); i++) {
                if (i >= this.stats.size()) {
                    this.stats.add(that.stats.get(i));
                }
                this.stats.get(i).add(that.stats.get(i));
            }
        }
    }

    public Stat overall() {
        return Stat.aggregate(stats);
    }

    @Override
    public Iterator<Stat> iterator() {
        return stats.iterator();
    }
}
