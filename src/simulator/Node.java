package simulator;

import benchmark.Log;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class Node implements Runnable {

    protected String id;
    protected BlockingQueue<Message> inbox = new LinkedBlockingQueue<>();
    private volatile boolean running = true;

    public void terminate() {
        running = false;
    }

    public Node(String id) {
        this.id = id;
    }

    public abstract void handle(Message message);

    public boolean isClient() {
        return false;
    }

    @Override
    public void run() {

        while (running) {
            try {
                Message message = inbox.take();
                handle(message);
            } catch (InterruptedException e) {
                Log.error(id, "InterruptedException");
                running = false;
                e.printStackTrace();
            }
        }
    }

}
