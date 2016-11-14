package benchmark;

import java.util.Map;

public interface KVDB<K, V> extends DB {

    /**
     *
     * @param key given key id, convert to user defined {@code K} type
     * @param value given byte array value, convert to user defined {@code V} type
     * @return a user defined KV pair
     */
    Map.Entry<K, V> cast(long key, byte[] value);

    /**
     * Read source key
     *
     * @param key
     * @return the value of key
     */
    V get(K key);

    /**
     * Write key value pair, return the old value
     *
     * @param key
     * @param value
     * @return the old value of the key
     */
    V put(K key, V value);

    /**
     * Remove the key value pair, return the old value
     *
     * @param key
     * @return the old value of the key
     */
    V remove(K key);

    /**
     * Same as {@link KVDB#put(Object, Object)} without return value.
     */
    Status set(K key, V value);

    /**
     * Same as {@link KVDB#remove(Object)} without return value.
     */
    Status delete(K key);

    /**
     * DB snapshot feature, if support
     *
     * @return successful
     */
    default Status snapshot() { return Status.NOT_IMPLEMENTED; }
}
