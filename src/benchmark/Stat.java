package benchmark;

import java.io.Serializable;
import java.util.Collection;

import static java.lang.Math.max;
import static java.lang.Math.min;

class Stat implements Serializable {

    private static final long serialVersionUID = 42L;

    private long count = 0;
    private double throughput = 0; // op/s
    private double latency = -1; // ms
    private double latency95 = -1;
    private double latency99 = -1;
    private double latencymin = -1;
    private double latencymax = -1;

    public boolean isEmpty() {
        return count == 0 && throughput == 0;
    }

    public void add(Stat that) {
        if (that.isEmpty()) return;
        if (this.isEmpty()) {
            this.count = that.count;
            this.throughput = that.throughput;
            this.latency = that.latency;
            this.latency95 = that.latency95;
            this.latency99 = that.latency99;
            this.latencymin = that.latencymin;
            this.latencymax = that.latencymax;
        } else {
            this.count += that.count;
            this.throughput += that.throughput;
            this.latency = (this.latency + that.latency) / 2.0;
            this.latency95 = max(this.latency95, that.latency95);
            this.latency99 = max(this.latency99, that.latency99);
            this.latencymin = min(this.latencymin, that.latencymin);
            this.latencymax = max(this.latencymax, that.latencymax);
        }
    }

    public static Stat aggregate(Stat... stats) {
        Stat result = new Stat();
        for (Stat stat : stats) {
            result.add(stat);
        }
        return result;
    }

    public static Stat aggregate(Collection<Stat> stats) {
        Stat result = new Stat();
        for (Stat stat : stats) {
            result.add(stat);
        }
        return result;
    }

    public long getCount() {
        return count;
    }

    public Stat setCount(long count) {
        this.count = count;
        return this;
    }

    public double getThroughput() {
        return throughput;
    }

    public Stat setThroughput(double throughput) {
        this.throughput = throughput;
        return this;
    }

    public double getLatency() {
        return latency;
    }

    public Stat setLatency(double latency) {
        this.latency = latency;
        return this;
    }

    public double getLatency95() {
        return latency95;
    }

    public Stat setLatency95(double latency95) {
        this.latency95 = latency95;
        return this;
    }

    public double getLatency99() {
        return latency99;
    }

    public Stat setLatency99(double latency99) {
        this.latency99 = latency99;
        return this;
    }

    public double getLatencymin() {
        return latencymin;
    }

    public Stat setLatencymin(double latencymin) {
        this.latencymin = latencymin;
        return this;
    }

    public double getLatencymax() {
        return latencymax;
    }

    public Stat setLatencymax(double latencymax) {
        this.latencymax = latencymax;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("count = ").append(count);
        sb.append("\nthroughput = ").append(throughput);
        sb.append("\nlatency = ").append(latency);
        sb.append("\n95th percentile latency = ").append(latency95);
        sb.append("\n99th percentile latency = ").append(latency99);
        sb.append("\nmin latency = ").append(latencymin);
        sb.append("\nmax latency = ").append(latencymax);
        return sb.toString();
    }

}
