package benchmarkold;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Stats implements Serializable, Iterable<Map.Entry<Operation, Stat>> {

    private static final long serialVersionUID = 42L;

    private Map<Operation, Stat> stats = new HashMap<>();

    public Stat get(Operation o) {
        return stats.get(o);
    }

    public Stats put(Operation o, Stat stat) {
        stats.put(o, stat);
        return this;
    }

    public void add(Stats that) {
        if (that.isEmpty()) { return; }
        if (this.isEmpty()) {
            this.stats = that.stats;
        } else {
            for (Map.Entry<Operation, Stat> entry : that) {
                if (!this.stats.containsKey(entry.getKey())) {
                    this.stats.put(entry.getKey(), entry.getValue());
                } else {
                    this.stats.get(entry.getKey()).add(entry.getValue());
                }
            }
        }
    }

    public boolean isEmpty() {
        return stats.isEmpty();
    }

    public long getOperationCount() {
        long sum = 0;
        for (Stat stat : stats.values()) {
            sum += stat.count;
        }
        return sum;
    }

    @Override
    public Iterator<Map.Entry<Operation, Stat>> iterator() {
        return stats.entrySet().iterator();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Operation, Stat> entry : stats.entrySet()) {
            sb.append("Operation ").append(entry.getKey().name());
            sb.append("\nStatistics ").append(entry.getValue());
        }
        return sb.toString();
    }
}
