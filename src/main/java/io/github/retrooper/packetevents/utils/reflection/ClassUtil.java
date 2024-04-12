package io.github.retrooper.packetevents.utils.reflection;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClassUtil {

    private static final Map<Class<?>, String> CLASS_SIMPLE_NAME_CACHE = new ConcurrentHashMap<>();

    public static String getClassSimpleName(Class<?> cls) {
        return CLASS_SIMPLE_NAME_CACHE.computeIfAbsent(cls, k -> cls.getSimpleName());
    }
}
