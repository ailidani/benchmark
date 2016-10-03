package benchmark;


import java.util.*;

/**
 * Periodic timer task to generate current statistics
 */
public class UpdateTimer extends TimerTask {

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
