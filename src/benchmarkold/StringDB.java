package benchmarkold;

import java.util.AbstractMap;
import java.util.Map;

public abstract class StringDB extends DB<String, String> {

    /**
     * Replace the default key value type to strings.
     *
     * @param k given key id, convert to {@code String} type
     * @param v given byte array value, convert to {@code String} type
     * @return kv pair as map entry
     */
    @Override
    public Map.Entry<String, String> cast(long k, byte[] v) {
        return new AbstractMap.SimpleEntry<>(String.valueOf(k), String.valueOf(v));
    }

}
