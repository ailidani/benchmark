package benchmarkold;

import java.util.Map;
import java.util.Properties;

public abstract class DB<K, V> {

    /**
     *
     * @param k given key id, convert to user defined {@code K} type
     * @param v given byte array value, convert to user defined {@code V} type
     * @return a user defined KV pair
     */
    public abstract Map.Entry<K, V> cast(long k, byte[] v);

    /**
     * Initialize the local client, put up connection and other required resources.
     *
     * @param address the server address used by current client
     */
    public abstract void init(String address, Properties properties);

    /**
     * Cleanup the benchmarkold data, disconnect the client etc.
     */
    public abstract void cleanup();

    /**
     * Read from key
     *
     * @param key returned by {@link DB#cast(long, byte[])} function from a key id
     * @return the value of key
     */
    public abstract V get(K key);

    /**
     * Write key value pair
     *
     * @param key returned by {@link DB#cast(long, byte[])} function from a key id
     * @param value returned by {@link DB#cast(long, byte[])} function from random bytes
     * @return the old value of the key
     */
    public abstract V put(K key, V value);

    /**
     * Remove the key value pair
     *
     * @param key key to be removed
     * @return the old value of the key
     */
    public abstract V remove(K key);

    /**
     * Same as {@link DB#put(Object, Object)} without return value.
     */
    public void set(K key, V value) {}

    /**
     * Same as {@link DB#remove(Object)} without return value.
     */
    public void delete(K key) {}

    /**
     * DB snapshot feature, if support
     *
     * @return successful
     */
    public abstract boolean snapshot();

}
