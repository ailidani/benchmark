package simulator;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class Node implements Runnable {

    protected String id;
    protected BlockingQueue<Message> inbox = new LinkedBlockingQueue<>();

    public Node(String id) {
        this.id = id;
    }

    public abstract void handle(Message message);

    public boolean isClient() {
        return false;
    }

    @Override
    public void run() {

        Networks.addNode(this);

        while (true) {
            try {
                Message message = inbox.take();
                handle(message);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
