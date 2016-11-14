package benchmark;

import java.util.Properties;

/**
 *
 *             | Class | Package | Subclass | Subclass | World
 *             |       |         |(same pkg)|(diff pkg)|
 * ————————————+———————+—————————+——————————+——————————+————————
 * public      |   +   |    +    |    +     |     +    |   +
 * ————————————+———————+—————————+——————————+——————————+————————
 * protected   |   +   |    +    |    +     |     +    |   o
 * ————————————+———————+—————————+——————————+——————————+————————
 * no modifier |   +   |    +    |    +     |     o    |   o
 * ————————————+———————+—————————+——————————+——————————+————————
 * private     |   +   |    o    |    o     |     o    |   o
 *
 * + : accessible
 * o : not accessible
 *
 */

public interface DB {

    /**
     * Initialize the local client, put up connection and other required resources.
     *
     * @param address the DB server address used by current client
     */
    public abstract void init(String address, Properties properties);

    /**
     * Cleanup the benchmark data, disconnect the client etc.
     */
    public abstract void cleanup();
}
