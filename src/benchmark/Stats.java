package benchmark;

import java.util.*;

class Stats {

    public static final double StoMS = 1000.0;
    public static final double MStoNS = 1000000.0;
    public static final double StoNS = 1000000000.0;

    private Vector<Double> latencies = new Vector<>();
    
    private int index = 0;

    public synchronized void add(double latency /* ms */) {
        this.latencies.add(latency);
    }

    public synchronized Stat slice() {
        List<Double> slice = new ArrayList<>(latencies.subList(index, latencies.size()));
        Stat stat = summary(slice);
        index += slice.size();
        Log.info("Stats", "Client Elapsed time %f seconds Throughput = %f (op/s)", stat.getElapsed(), stat.getThroughput());
        Log.debug("Stats", stat.toString());
        return stat;
    }

    public Stat overall() {
        return summary(latencies);
    }

    private synchronized Stat summary(List<Double> list) {
        Stat stat = new Stat();
        int n = list.size();
        if (n == 0) return stat;
        Collections.sort(list);
        double sum /* ms */ = 0;
        for (double i : list) sum += i;
        double elapsed /* s */ = sum / StoMS;
        stat.setCount(n);
        stat.setElapsed(elapsed);
        stat.setThroughput((double) n / elapsed);
        stat.setLatency(sum / (double) n);
        stat.setLatency95( list.get((int) ((double) n * 0.95)) );
        stat.setLatency99( list.get((int) ((double) n * 0.99)) );
        stat.setLatencymin(list.get(0));
        stat.setLatencymax(list.get(n - 1));
        return stat;
    }

    public boolean isEmpty() {
        return latencies.isEmpty();
    }

    public int size() {
        return latencies.size();
    }

}
