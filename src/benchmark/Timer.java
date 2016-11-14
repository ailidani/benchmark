package benchmark;

import java.util.TimerTask;

class FinishTimer extends TimerTask {

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

/**
 * Periodic timer task to generate current statistics
 */
class UpdateTimer extends TimerTask {

    private Client client;

    public UpdateTimer(Client client) {
        this.client = client;
    }

    @Override
    public void run() {
        Stat stat = client.stats.slice();
        client.publish(stat);
    }

}
