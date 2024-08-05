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
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * A list that evicts the oldest entry when the size limit is reached.
 *
 * @param <T> The type of the list.
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@SuppressWarnings("unused")
public final class EvictingList<T> extends LinkedList<T> {

    private static final long serialVersionUID = -2926788977148580670L;
    private final int maxSize;

    @Override
    public boolean add(T t) {
        if (size() >= maxSize) {
            removeFirst();
        }
        return super.add(t);
    }

    public boolean isFull() {
        return size() >= maxSize;
    }

    @Contract(" -> new")
    public @NotNull List<T> toList() {
        return new LinkedList<>(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof EvictingList)) {
            return false;
        }

        if (!super.equals(o)) {
            return false;
        }

        EvictingList<?> list = (EvictingList<?>) o;
        return maxSize == list.maxSize && super.equals(list);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), maxSize);
    }

    private void readObject(ObjectInputStream in) throws ClassNotFoundException, NotSerializableException {
        throw new NotSerializableException("net.foulest.vulture.util.data.EvictingList");
    }

    private void writeObject(ObjectOutputStream out) throws NotSerializableException {
        throw new NotSerializableException("net.foulest.vulture.util.data.EvictingList");
    }

    @Override
    public EvictingList<T> clone() throws AssertionError {
        throw new AssertionError();
    }
}
