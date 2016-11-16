package simulator;

import benchmark.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static simulator.Raft.N;

public class Raft {

    public static int N = 3;

    private static Raft raft = new Raft();
    private static Map<Node, Thread> nodes = new HashMap<>();
    private static Networks networks;
    private static AtomicBoolean started = new AtomicBoolean(false);

    private Raft() {}

    public static Raft instance() {
        return raft;
    }

    public void start() {
        if (started.getAndSet(true)) {
            return;
        }
        for (int i = 0; i < N; i++) {
            if (i == 0) {
                Leader leader = new Leader();
                Networks.addNode(leader);
                nodes.put(leader, new Thread(leader, leader.id));
            } else {
                Follower follower = new Follower(String.valueOf(i));
                Networks.addNode(follower);
                nodes.put(follower, new Thread(follower, follower.id));
            }
        }
        for (Thread t : nodes.values()) {
            t.start();
        }
        networks = new Networks();
        networks.start();
    }

    public void stop() {
        for (Map.Entry<Node, Thread> entry : nodes.entrySet()) {
            entry.getKey().terminate();
        }
        nodes.clear();
        networks.terminate();
        started.getAndSet(false);
    }

}

class Append extends Message {
    long term;
    long index;
    long commit;
    Request request;

    Append(long term, long index, Request request, long commit) {
        super("append-" + index);
        this.term = term;
        this.index = index;
        this.request = request;
        this.commit = commit;
    }

    @Override
    public Append clone() {
        return (Append) super.clone();
    }
}

class Ack extends Message {
    long term;
    long index;

    Ack(long term, long index) {
        super("ack-" + index);
        this.term = term;
        this.index = index;
    }

    @Override
    public Ack clone() {
        return (Ack) super.clone();
    }
}

class Leader extends Node {
    long term = 1;
    long index = 0;
    long commit = 0;
    Map<Long, Request> requests = new HashMap<>();
    Map<Long, AtomicInteger> acks = new HashMap<>();
    Vector<Long> commits = new Vector<>();
    Map<Long, Long> database = new ConcurrentHashMap<>();

    public Leader() {
        super("Leader");
    }

    @Override
    public void handle(Message message) {
        Log.debug(id, "Received message: " + message);

        if (message instanceof Request) {
            Request request = (Request) message;
            index++;
            requests.put(index, request);
            acks.put(index, new AtomicInteger(1));
            new Append(term, index, request, commit).setSource(id).broadcast();
        }

        else if (message instanceof Ack) {
            Ack ack = (Ack) message;
            long index = ack.index;
            int count = acks.get(index).incrementAndGet();
            if (requests.containsKey(index) && count > N / 2) {
                commit = index;
                commits.add(index);
                long iid = requests.get(index).iid;
                String client = requests.get(index).source;
                new Reply(iid).setSource(id).setDestination(client).send();
                requests.remove(index);
            }
        }
    }
}

class Follower extends Node {
    long term = 1;
    long index = 0;
    long commit = 0;
    Vector<Long> accepted = new Vector<>();
    Vector<Long> commits = new Vector<>();

    public Follower(String id) {
        super("Follower-" + id);
    }

    @Override
    public void handle(Message message) {
        Log.debug(id, "Received message: " + message);

        if (message instanceof Append) {
            Append append = (Append) message;
            if (append.index > index) {
                Log.debug(id, "Accepted index %d", append.index);
                accepted.add(append.index);
                new Ack(term, append.index).setSource(id).setDestination(append.source).send();
            }
            if (append.commit > commit) {
                while (accepted.contains(index + 1)) {
                    index++;
                    commit++;
                    Log.debug(id, "Commited index %d", append.commit);
                    accepted.remove(index);
                    commits.add(commit);
                }
            }
        }

        else if (message instanceof Request) {
            Log.debug(id, "Forward request to Leader.");
            message.setDestination("Leader").send();
        }
    }
}
