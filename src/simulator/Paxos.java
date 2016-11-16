package simulator;

import benchmark.Log;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class Paxos {

    public static int N = 3;

    private static Paxos paxos = new Paxos();
    private static Thread[] threads = new Thread[N];
    private static Networks networks = new Networks();

    private Paxos() {}

    public static Paxos instance() { return paxos; }

}

class Prepare extends Message {
    long iid;
    long ballot;
    Prepare(long iid, long ballot) {
        super("prepare-" + iid);
        this.iid = iid;
        this.ballot = ballot;
    }

    @Override
    public Prepare clone() { return (Prepare) super.clone(); }
}

class Promise extends Message {
    long iid;
    long ballot;
    long value_ballot;
    Request request;
    Promise(long iid) {
        super("promise-" + iid);
        this.iid = iid;
    }

    @Override
    public Promise clone() { return (Promise) super.clone(); }
}

class Accept extends Message {
    long iid;
    long ballot;
    Request request;
    Accept(long iid) {
        super("accept-" + iid);
    }

    @Override
    public Accept clone() { return (Accept) super.clone(); }
}

class Accepted extends Message {
    long iid;
    long ballot;
    long value_ballot;
    Request request;
}

class Preempted extends Message {
    long iid;
    long ballot;
}

class Instance {
    long iid;
    long ballot = 0;
    Request value;
    Request promised_value;
    long value_ballot;
    AtomicInteger acks = new AtomicInteger(1);
}


class Replica extends Node {
    Set<Request> values = new HashSet<>();
    long next_prepare_iid = 0;
    long trim_iid = 0;
    Map<Long, Instance> prepare_instances = new HashMap<>();
    Map<Long, Instance> accept_instances = new HashMap<>();

    Replica(long id) {
        super("Replica-" + id);
    }

    @Override
    public void handle(Message message) {
        Log.debug(id, "Received message: " + message);

        if (message instanceof Request) {
            Request request = (Request) message;
            Instance instance = new Instance();
            instance.iid = ++next_prepare_iid;
            instance.value = request;
            prepare_instances.put(instance.iid, instance);

            Prepare prepare = new Prepare(instance.iid, instance.ballot);
            prepare.setSource(id).broadcast();
        }

        else if (message instanceof Promise) {
            Promise promise = (Promise) message;
            if (!prepare_instances.containsKey(promise.iid)) {
                Log.debug("Replica", "Promise dropped, instance " + promise.iid + " not pending.");
                return;
            }
            Instance instance = prepare_instances.get(promise.iid);
            if (promise.ballot < instance.ballot) {
                Log.debug("Replica", "Promise dropped, too old.");
                return;
            }
            if (promise.ballot > instance.ballot) {
                Log.debug("Replica", "Instance %u preempted: ballot %d ack ballot %d", instance.iid, instance.ballot, promise.ballot);
                
            }
        }

        else if (message instanceof Prepare) {
            Prepare prepare = (Prepare) message;
            if (prepare.iid <= trim_iid) {
                return;
            }

            Accepted accepted = new Accepted();

        }

        else if (message instanceof Accept) {
            Accept accept = (Accept) message;


        }
    }
}
