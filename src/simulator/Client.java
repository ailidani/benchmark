package simulator;

import java.util.Vector;

public class Client extends Node {

    private long iid = 0;
    private final Vector<Long> requests = new Vector<>();

    public Client(String id) {
        super(id);
        Networks.addNode(this);
    }

    public void send() {
        iid++;
        requests.add(iid);
        Request request = new Request(iid);
        request.setSource(id).send();

        synchronized (requests) {
            while (requests.contains(request.iid)) {
                try { requests.wait(); } catch (InterruptedException e) {}
            }
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
            synchronized (requests) {
                requests.remove(reply.iid);
                requests.notifyAll();
            }
        }
    }
}

class Request extends Message {
    long iid;
    Request(long iid) {
        super("request-" + iid);
        this.iid = iid;
    }

    @Override
    public Request clone() {
        return (Request) super.clone();
    }
}

class Reply extends Message {
    long iid;
    Reply(long iid) {
        super("reply-" + iid);
        this.iid = iid;
    }

    @Override
    public Reply clone() {
        return (Reply) super.clone();
    }
}
