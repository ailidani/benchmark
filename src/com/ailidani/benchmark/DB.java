package com.ailidani.benchmark;

import java.util.Map;

public abstract class DB<K, V> {

    //protected Map<K, V> data;

    protected abstract Map.Entry<K, V> cast(long k, byte[] v);

    public abstract void init(String address);

    public abstract void cleanup();

    public abstract V get(K key);

    public abstract V put(K key, V value);

    public abstract V remove(K key);

    public void set(K key, V value) {}

    public void delete(K key) {}

    public abstract boolean snapshot();

}
