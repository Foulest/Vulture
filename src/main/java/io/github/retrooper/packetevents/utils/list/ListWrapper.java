package io.github.retrooper.packetevents.utils.list;

import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@AllArgsConstructor
public abstract class ListWrapper implements List {

    private final List list;

    public abstract void processAdd(Object o);

    public List getOriginalList() {
        return list;
    }

    @Override
    public int size() {
        synchronized (this) {
            return list.size();
        }
    }

    @Override
    public boolean isEmpty() {
        synchronized (this) {
            return list.isEmpty();
        }
    }


    @Override
    public boolean contains(Object o) {
        synchronized (this) {
            return list.contains(o);
        }
    }

    @Override
    public @NotNull Iterator iterator() {
        synchronized (this) {
            return listIterator();
        }
    }

    @Override
    public Object @NotNull [] toArray() {
        synchronized (this) {
            return list.toArray();
        }
    }

    @Override
    public boolean add(Object o) {
        processAdd(o);

        synchronized (this) {
            return list.add(o);
        }
    }

    @Override
    public boolean remove(Object o) {
        synchronized (this) {
            return list.remove(o);
        }
    }

    @Override
    public boolean addAll(@NotNull Collection c) {
        for (Object o : c) {
            processAdd(o);
        }

        synchronized (this) {
            return list.addAll(c);
        }
    }

    @Override
    public boolean addAll(int index, @NotNull Collection c) {
        for (Object o : c) {
            processAdd(o);
        }

        synchronized (this) {
            return list.addAll(index, c);
        }
    }

    @Override
    public void clear() {
        synchronized (this) {
            list.clear();
        }
    }

    @Override
    public Object get(int index) {
        synchronized (this) {
            return list.get(index);
        }
    }

    @Override
    public Object set(int index, Object element) {
        synchronized (this) {
            return list.set(index, element);
        }
    }

    @Override
    public void add(int index, Object element) {
        synchronized (this) {
            list.add(index, element);
        }
    }

    @Override
    public Object remove(int index) {
        synchronized (this) {
            return list.remove(index);
        }
    }

    @Override
    public int indexOf(Object o) {
        synchronized (this) {
            return list.indexOf(o);
        }
    }

    @Override
    public int lastIndexOf(Object o) {
        synchronized (this) {
            return list.lastIndexOf(o);
        }
    }

    @Override
    public @NotNull ListIterator listIterator() {
        synchronized (this) {
            return list.listIterator();
        }
    }

    @Override
    public @NotNull ListIterator listIterator(int index) {
        synchronized (this) {
            return list.listIterator(index);
        }
    }

    @Override
    public @NotNull List subList(int fromIndex, int toIndex) {
        synchronized (this) {
            return list.subList(fromIndex, toIndex);
        }
    }

    @Override
    public boolean retainAll(@NotNull Collection c) {
        synchronized (this) {
            return list.retainAll(c);
        }
    }

    @Override
    public boolean removeAll(@NotNull Collection c) {
        synchronized (this) {
            return list.removeAll(c);
        }
    }

    @Override
    public boolean containsAll(@NotNull Collection c) {
        synchronized (this) {
            return new HashSet<>(list).containsAll(c);
        }
    }

    @Override
    public Object @NotNull [] toArray(Object @NotNull [] a) {
        synchronized (this) {
            return list.toArray(a);
        }
    }
}
