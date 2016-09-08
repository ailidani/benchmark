package com.ailidani.benchmark;

import java.util.AbstractMap;
import java.util.Map;

public abstract class StringDB extends DB<String, String> {

    @Override
    protected Map.Entry<String, String> next(long k, byte[] v) {
        return new AbstractMap.SimpleEntry<>(String.valueOf(k), String.valueOf(v));
    }

}
