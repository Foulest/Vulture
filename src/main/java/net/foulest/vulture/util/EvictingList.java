package net.foulest.vulture.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;

/**
 * A list that evicts the oldest entry when the size limit is reached.
 *
 * @param <T> The type of the list.
 * @author Foulest
 * @project Vulture
 */
@Getter
@Setter
@AllArgsConstructor
public final class EvictingList<T> extends LinkedList<T> {

    private final int maxSize;

    @Override
    public boolean add(@NonNull T t) {
        if (size() >= getMaxSize()) {
            removeFirst();
        }
        return super.add(t);
    }

    public boolean isFull() {
        return size() >= getMaxSize();
    }

    public List<T> toList() {
        return new LinkedList<>(this);
    }
}
