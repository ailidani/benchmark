package simulator;

import benchmark.Generator;
import benchmark.Log;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;

public class Networks extends Thread {

    private static Generator latency = new Generator(1, 100, Generator.Distribution.Zipfian);

    private static Map<String, Node> nodes = new ConcurrentHashMap<>();

    private static DelayQueue<Message> messages = new DelayQueue<>();

    private volatile boolean running = true;

    public static Collection<Node> getNodes() {
        return nodes.values();
    }

    public static void addNode(Node node) {
        nodes.put(node.id, node);
    }

    public static void send(Message msg) {
        if (msg.getDestination() == null) {
            String des = nodes.values().stream().filter(node -> !node.isClient()).findAny().get().id;
            msg.setDestination(des);
        }
        Log.debug("Network", "%s send message %s to %s", msg.source, msg, msg.destination);
        long delay = latency.next();
        msg.setLatency(delay);
        messages.offer(msg);
    }

    public static void broadcast(Message msg) {
        Log.debug("Network", "%s broadcast message %s", msg.source, msg);
        for (Node node : nodes.values()) {
            if (node.id.equalsIgnoreCase(msg.getSource()) || node.isClient()) continue;
            Message message = msg.clone();
            message.setDestination(node.id);
            send(message);
        }
    }

    public Networks() {
        super("Networks");
    }

    public void terminate() {
        running = false;
        messages.clear();
        nodes.clear();
    }

    @Override
    public void run() {
        while (running) {
            try {
                Message message = messages.take();
                if (message.getDestination() != null) {
                    Node des = nodes.get(message.getDestination());
                    des.inbox.offer(message);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

}
