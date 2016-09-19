package com.ailidani.benchmark;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ailidani.benchmark.OperationType.GET;
import static com.ailidani.benchmark.OperationType.PUT;
import static com.ailidani.benchmark.OperationType.REMOVE;

public class Stat implements Serializable {

    private static final long serialVersionUID = 42L;

    protected Map<OperationType, Long> done = new HashMap<>();
    protected Map<OperationType, List<Double>> throughput = new HashMap<>();
    protected Map<OperationType, List<Double>> latency = new HashMap<>();

    public Stat() {
        if (Config.getGetProportion() > 0) {
            done.put(GET, 0L);
            throughput.put(GET, new ArrayList<>());
            latency.put(GET, new ArrayList<>());
        }
    }

    protected long get = 0;
    protected long put = 0;
    protected long set = 0;
    protected long remove = 0;
    protected long delete = 0;
    protected long snapshot = 0;

    //protected List<Double> throughput = new ArrayList<>();
    protected List<Double> getLatency = new ArrayList<>();
    protected List<Double> putLatency = new ArrayList<>();
    protected List<Double> setLatency = new ArrayList<>();
    protected List<Double> removeLatency = new ArrayList<>();
    protected List<Double> deleteLatency = new ArrayList<>();
    protected List<Double> snapshotLatency = new ArrayList<>();

    public void measure(OperationType op, long nano) {
        switch (op) {
            case GET:
                get++;
                getLatency.add(nano / 1000000.0);
                break;
            case PUT:
                put++;
                putLatency.add(nano / 1000000.0);
                break;
            case SET:
                set++;
                setLatency.add(nano / 1000000.0);
                break;
            case REMOVE:
                remove++;
                removeLatency.add(nano / 1000000.0);
                break;
            case DELETE:
                delete++;
                deleteLatency.add(nano / 1000000.0);
                break;
            case SNAPSHOT:
                snapshot++;
                snapshotLatency.add(nano / 1000000.0);
            default:
                System.err.printf("Unknown operation type: %s \n", op);
        }
    }

}
