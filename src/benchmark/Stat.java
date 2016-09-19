package benchmark;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.max;

public class Stat implements Serializable {

    private static final long serialVersionUID = 42L;

    protected long count = 0;
    protected List<Double> throughput = new ArrayList<>();
    protected List<Double> latency = new ArrayList<>();
    protected List<Double> latency95 = new ArrayList<>();
    protected List<Double> latency99 = new ArrayList<>();
    protected List<Double> latencymin = new ArrayList<>();
    protected List<Double> latencymax = new ArrayList<>();

    public void add(Stat that) {
        if (that.isEmpty()) { return; }
        if (this.isEmpty()) {
            this.count = that.count;
            this.throughput = that.throughput;
            this.latency = that.latency;
            this.latency99 = that.latency99;
            this.latencymax = that.latencymax;
        } else {
            this.count += that.count;
            int n = that.throughput.size();
            for (int i = 0; i < n; i++) {
                if (i > this.throughput.size() - 1) {
                    this.throughput.add(that.throughput.get(i));
                    continue;
                }
                this.throughput.set(i, this.throughput.get(i) + that.throughput.get(i));
            }
            n = that.latency.size();
            for (int i = 0; i < n; i++) {
                if (i > this.latency.size() - 1) {
                    this.latency.add(that.latency.get(i));
                    continue;
                }
                this.latency.set(i, (this.latency.get(i) + that.latency.get(i)) / 2.0);
            }
            n = that.latency99.size();
            for (int i = 0; i < n; i++) {
                if (i > this.latency99.size() - 1) {
                    this.latency99.add(that.latency99.get(i));
                    continue;
                }
                this.latency99.set(i, max(this.latency99.get(i), that.latency99.get(i)));
            }
            n = that.latencymax.size();
            for (int i = 0; i < n; i++) {
                if (i > this.latencymax.size() - 1) {
                    this.latencymax.add(that.latencymax.get(i));
                    continue;
                }
                this.latencymax.set(i, max(this.latencymax.get(i), that.latencymax.get(i)));
            }
        }

    }

    public boolean isEmpty() {
        return count == 0 && throughput.isEmpty();
    }

    public static Stat aggregate(Stat... stats) {
        Stat result = new Stat();
        for (Stat stat : stats) {
            result.add(stat);
        }
        return result;
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
