package simulator;

import benchmark.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Raft {

    public static int N = 3;

    private static Raft raft = new Raft();
    private static Thread[] threads = new Thread[N];
    private static Networks networks = new Networks();

    private Raft() {}

    public static Raft instance() {
        return raft;
    }

    public void start() {
        Leader leader = new Leader();
        threads[0] = new Thread(leader);
        for (int i = 1; i < N; i++) {
            threads[i] = new Thread(new Follower(String.valueOf(i)));
        }
        for (Thread t : threads) {
            t.start();
        }
        networks.start();
    }

    private class Request extends Message {
        long rid;
        Request(String id, long rid) {
            super("request-" + id);
            this.rid = rid;
        }

        @Override
        public Request clone() {
            return (Request) super.clone();
        }
    }

    private class Reply extends Message {
        long rid;
        Reply(String id, long rid) {
            super("reply-" + id);
            this.rid = rid;
        }

        @Override
        public Reply clone() {
            return (Reply) super.clone();
        }
    }

    private class Append extends Message {
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

    private class Ack extends Message {
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

    private class Leader extends Node {
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
                    long rid = requests.get(index).rid;
                    String client = requests.get(index).source;
                    new Reply(String.valueOf(rid), rid).setSource(id).setDestination(client).send();
                    requests.remove(index);
                }
            }
        }
    }

    private class Follower extends Node {
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
                index++;
                Append append = (Append) message;
                if (append.index == index) {
                    Log.debug(id, "Accepted index " + index);
                    accepted.add(index);
                    new Ack(term, index).setSource(id).setDestination(append.source).send();
                }
                Log.debug(id, "Commited index " + append.commit);
                accepted.remove(append.commit);
                commit = append.commit;
                commits.add(append.commit);
            }

            else if (message instanceof Request) {
                Log.debug(id, "Forward request to Leader.");
                message.setDestination("Leader").send();
            }
        }
    }

    public class Client extends Node {
        private long rid = 0;
        private final Vector<Long> requests = new Vector<>();

        public Client(String id) {
            super(id);
        }

        public void send(long key, long value) {
            rid++;
            requests.add(rid);
            Request request = new Request(String.valueOf(rid), rid);
            request.setSource(id).send();

            while (requests.contains(request.rid)) {
                try {
                    synchronized (requests) {
                        requests.wait();
                    }
                } catch (InterruptedException e) {}
            }
        }

        @Override
        public boolean isClient() {
            return true;
        }

        @Override
        public void handle(Message message) {
            if (message instanceof Reply) {
                Reply reply = (Reply) message;
                if (requests.contains(reply.rid)) {
                    requests.remove(reply.rid);
                    synchronized (requests) {
                        requests.notifyAll();
                    }
                }
            }
        }
    }

}
