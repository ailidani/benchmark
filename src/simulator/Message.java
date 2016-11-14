package simulator;

import java.io.Serializable;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class Message implements Serializable, Cloneable, Delayed {

    protected String id;
    protected String source;
    protected String destination;

    protected long latency = 0;

    public Message() {
        this(null, null, null);
    }

    public Message(String id) {
        this(id, null, null);
    }

    public Message(String id, String source, String destination) {
        this.id = id;
        this.source = source;
        this.destination = destination;
    }

    public String getId() {
        return id;
    }

    public Message setId(String id) {
        this.id = id;
        return this;
    }

    public String getSource() {
        return source;
    }

    public Message setSource(String source) {
        this.source = source;
        return this;
    }

    public String getDestination() {
        return destination;
    }

    public Message setDestination(String destination) {
        this.destination = destination;
        return this;
    }

    public void send() {
        assert this.source != null;
        Networks.send(this);
    }

    public void broadcast() {
        assert this.source != null;
        Networks.broadcast(this);
    }

    public Message setLatency(long latency) {
        this.latency = latency + System.currentTimeMillis();
        return this;
    }

    public long getLatency() {
        return latency;
    }

    @Override
    public Message clone() {
        try {
            return (Message) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    @Override
    public long getDelay(TimeUnit unit) {
        long diff = latency - System.currentTimeMillis();
        return unit.convert(diff, TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed that) {
        return (int) (this.latency - ((Message) that).getLatency());
    }

    @Override
    public String toString() {
        return id;
    }
}
