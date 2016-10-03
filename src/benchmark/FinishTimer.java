package benchmark;

import java.util.TimerTask;

public class FinishTimer extends TimerTask {

    private Client client;
    private long deadline;
    private long count = 0;

    public FinishTimer(Client client) {
        this.client = client;
        int time = client.config().getTotalTime() + client.config().getWarmupTime();
        this.deadline = (time * 1000) / client.config().getInterval();
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
