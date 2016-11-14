package simulator;

import benchmark.Generator;
import benchmark.Log;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;

public class Networks extends Thread {

    private static Generator latency = new Generator(3, 1000, Generator.Distribution.Zipfian);

    private static Map<String, Node> nodes = new ConcurrentHashMap<>();

    private static DelayQueue<Message> messages = new DelayQueue<>();

    public static Collection<Node> getNodes() {
        return nodes.values();
    }

    public static void addNode(Node node) {
        nodes.put(node.id, node);
    }

    public static void send(Message msg) {
        Log.debug("Network", "message = " + msg);
        long delay = latency.next();
        msg.setLatency(delay);
        messages.offer(msg);
    }

    public static void broadcast(Message msg) {
        Log.debug("Network", "message broadcast " + msg);
        for (Node node : nodes.values()) {
            if (node.id.equalsIgnoreCase(msg.getSource()) || node.isClient()) continue;
            Message message = msg.clone();
            message.setDestination(node.id);
            send(message);
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                Message message = messages.take();
                if (message.getDestination() != null) {
                    Node des = nodes.get(message.getDestination());
                    des.inbox.add(message);
                } else {
                    int size = nodes.size();
                    Node des;
                    do {
                        des = nodes.values().stream().findAny().get();
                    } while (des.isClient());
                    des.inbox.add(message);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

}
