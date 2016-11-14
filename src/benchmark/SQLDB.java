package benchmark;

import java.util.Map;
import java.util.Set;

public interface SQLDB<K, F, V> extends DB {

     K castK(long key);

     Set<F> castFields(Set<Long> fields);

     Map<F, V> castFields(Map<Long, byte[]> fields);

     Status query(String table, K key, Set<F> fields);

     Status insert(String table, K key, Map<F, V> fields);

     Status update(String table, K key, Map<F, V> fields);

     Status delete(String table, K key, Map<F, V> fields);

     Status scan(String table, K key, Set<F> fields);
}
