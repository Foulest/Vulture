package net.foulest.vulture.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * A map that evicts the oldest entry when the size limit is reached.
 *
 * @param <K> The type of the key.
 * @param <V> The type of the value.
 * @author Foulest
 * @project Vulture
 */
@Getter
@Setter
@AllArgsConstructor
public final class EvictingMap<K, V> extends HashMap<K, V> {

    private final int size;
    private final Deque<K> storedKeys = new LinkedList<>();

    @Override
    @SuppressWarnings("SuspiciousMethodCalls")
    public boolean remove(@NonNull Object key, @NonNull Object value) {
        storedKeys.remove(key);
        return super.remove(key, value);
    }

    @Override
    public V putIfAbsent(@NonNull K key, @NonNull V value) {
        if (!storedKeys.contains(key) || !get(key).equals(value)) {
            checkAndRemove();
        }
        return super.putIfAbsent(key, value);
    }

    @Override
    public V put(@NonNull K key, @NonNull V value) {
        checkAndRemove();
        storedKeys.addLast(key);
        return super.put(key, value);
    }

    @Override
    public void putAll(@NonNull Map<? extends K, ? extends V> map) {
        map.forEach(this::put);
    }

    @Override
    public void clear() {
        storedKeys.clear();
        super.clear();
    }

    @Override
    @SuppressWarnings("SuspiciousMethodCalls")
    public V remove(@NonNull Object key) {
        storedKeys.remove(key);
        return super.remove(key);
    }

    private void checkAndRemove() {
        if (storedKeys.size() >= size) {
            K key = storedKeys.removeFirst();
            remove(key);
        }
    }
}
