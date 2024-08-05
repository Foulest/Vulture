/*
 * This file is part of packetevents - https://github.com/retrooper/packetevents
 * Copyright (C) 2022 retrooper and contributors
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
package io.github.retrooper.packetevents.utils.list;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Synchronized;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@Getter
@AllArgsConstructor
public abstract class ListWrapper implements List {

    private final List originalList;

    protected abstract void processAdd(Object o);

    @Override
    @Synchronized
    public int size() {
        return originalList.size();
    }

    @Override
    @Synchronized
    public boolean isEmpty() {
        return originalList.isEmpty();
    }

    @Override
    @Synchronized
    public boolean contains(Object o) {
        return originalList.contains(o);
    }

    @Override
    @Synchronized
    public @NotNull Iterator iterator() {
        return listIterator();
    }

    @Override
    @Synchronized
    public Object @NotNull [] toArray() {
        return originalList.toArray();
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean add(Object o) {
        processAdd(o);

        synchronized (this) {
            return originalList.add(o);
        }
    }

    @Override
    @Synchronized
    public boolean remove(Object o) {
        return originalList.remove(o);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean addAll(@NotNull Collection c) {
        for (Object o : c) {
            processAdd(o);
        }

        synchronized (this) {
            return originalList.addAll(c);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean addAll(int index, @NotNull Collection c) {
        for (Object o : c) {
            processAdd(o);
        }

        synchronized (this) {
            return originalList.addAll(index, c);
        }
    }

    @Override
    @Synchronized
    public void clear() {
        originalList.clear();
    }

    @Override
    @Synchronized
    public Object get(int index) {
        return originalList.get(index);
    }

    @Override
    @Synchronized
    @SuppressWarnings("unchecked")
    public Object set(int index, Object element) {
        return originalList.set(index, element);
    }

    @Override
    @Synchronized
    @SuppressWarnings("unchecked")
    public void add(int index, Object element) {
        originalList.add(index, element);
    }

    @Override
    @Synchronized
    public Object remove(int index) {
        return originalList.remove(index);
    }

    @Override
    @Synchronized
    public int indexOf(Object o) {
        return originalList.indexOf(o);
    }

    @Override
    @Synchronized
    public int lastIndexOf(Object o) {
        return originalList.lastIndexOf(o);
    }

    @Override
    @Synchronized
    public @NotNull ListIterator listIterator() {
        return originalList.listIterator();
    }

    @Override
    @Synchronized
    public @NotNull ListIterator listIterator(int index) {
        return originalList.listIterator(index);
    }

    @Override
    @Synchronized
    public @NotNull List subList(int fromIndex, int toIndex) {
        return originalList.subList(fromIndex, toIndex);
    }

    @Override
    @Synchronized
    @SuppressWarnings("unchecked")
    public boolean retainAll(@NotNull Collection c) {
        return originalList.retainAll(c);
    }

    @Override
    @Synchronized
    @SuppressWarnings("unchecked")
    public boolean removeAll(@NotNull Collection c) {
        return originalList.removeAll(c);
    }

    @Override
    @Synchronized
    @SuppressWarnings("unchecked")
    public boolean containsAll(@NotNull Collection c) {
        return new HashSet<>(originalList).containsAll(c);
    }

    @Override
    @Synchronized
    @SuppressWarnings("unchecked")
    public Object @NotNull [] toArray(Object @NotNull [] a) {
        return originalList.toArray(a);
    }
}
