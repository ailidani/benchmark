package benchmark;

import java.util.TimerTask;

public class FinishTimer extends TimerTask {

    private Client client;
    private long deadline;
    private long count = 0;

    public FinishTimer(Client client) {
        this.client = client;
        this.deadline = (client.config().getTotalTime() * 1000) / client.config().getInterval();
    }

    @Override
    public void run() {
        count++;
        if (count == deadline) {
            this.cancel();
            client.complete();
        }
    }

}
