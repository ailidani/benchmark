package com.ailidani.benchmark;

import java.util.Map;

public abstract class DB<K, V> {

    protected Map<K, V> data;

    public abstract void init(String address);

    public abstract void cleanup();

    public abstract V get(K key);

    public abstract void put(K key, V value);

    public abstract void delete(K key);

    public abstract boolean snapshot();

}
