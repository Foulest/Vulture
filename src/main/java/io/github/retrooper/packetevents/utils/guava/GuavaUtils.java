package io.github.retrooper.packetevents.utils.guava;

import com.google.common.collect.MapMaker;

import java.util.concurrent.ConcurrentMap;

public class GuavaUtils {

    public static <T, K> ConcurrentMap<T, K> makeMap() {
        return new MapMaker().weakValues().makeMap();
    }
}
