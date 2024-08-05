/*
 * Vulture - an advanced anti-cheat plugin designed for Minecraft 1.8.9 servers.
 * Copyright (C) 2024 Foulest (https://github.com/Foulest)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package net.foulest.vulture.util.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;

/**
 * A map that evicts the oldest entry when the size limit is reached.
 *
 * @param <K> The type of the key.
 * @param <V> The type of the value.
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
public final class EvictingMap<K, V> extends HashMap<K, V> {

    private static final long serialVersionUID = -4377528753798215948L;
    private final int mapSize;
    private final Deque<K> storedKeys = new LinkedList<>();

    @Override
    @SuppressWarnings("SuspiciousMethodCalls")
    public boolean remove(Object key, Object value) {
        storedKeys.remove(key);
        return super.remove(key, value);
    }

    @Override
    public V putIfAbsent(K key, V value) {
        if (!storedKeys.contains(key) || !get(key).equals(value)) {
            checkAndRemove();
        }
        return super.putIfAbsent(key, value);
    }

    @Override
    public V put(K key, V value) {
        checkAndRemove();
        storedKeys.addLast(key);
        return super.put(key, value);
    }

    @Override
    public void putAll(@NotNull Map<? extends K, ? extends V> map) {
        map.forEach(this::put);
    }

    @Override
    public void clear() {
        storedKeys.clear();
        super.clear();
    }

    @Override
    @SuppressWarnings("SuspiciousMethodCalls")
    public V remove(Object key) {
        storedKeys.remove(key);
        return super.remove(key);
    }

    private void checkAndRemove() {
        if (storedKeys.size() >= mapSize) {
            K key = storedKeys.removeFirst();
            remove(key);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof EvictingMap)) {
            return false;
        }

        if (!super.equals(o)) {
            return false;
        }

        EvictingMap<?, ?> map = (EvictingMap<?, ?>) o;
        return mapSize == map.mapSize && storedKeys.equals(map.storedKeys) && super.equals(map);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), mapSize, storedKeys);
    }

    private void readObject(ObjectInputStream in) throws ClassNotFoundException, NotSerializableException {
        throw new NotSerializableException("net.foulest.vulture.util.data.EvictingMap");
    }

    private void writeObject(ObjectOutputStream out) throws NotSerializableException {
        throw new NotSerializableException("net.foulest.vulture.util.data.EvictingMap");
    }

    @Override
    public EvictingMap<K, V> clone() throws AssertionError {
        throw new AssertionError();
    }
}
