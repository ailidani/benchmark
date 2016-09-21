package benchmark;

import java.util.*;

/**
 * Periodic timer task to generate current statistics
 */
public class UpdateTimer extends TimerTask {

    private Stats stats;

    public UpdateTimer(Stats stats) {
        this.stats = stats;
    }

    @Override
    public void run() {
        stats.slice();
    }

}
