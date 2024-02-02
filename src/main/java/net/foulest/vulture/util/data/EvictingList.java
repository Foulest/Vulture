package net.foulest.vulture.util.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

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
@SuppressWarnings("unused")
public final class EvictingList<T> extends LinkedList<T> {

    private final int maxSize;

    @Override
    public boolean add(T t) {
        if (size() >= getMaxSize()) {
            removeFirst();
        }
        return super.add(t);
    }

    public boolean isFull() {
        return size() >= getMaxSize();
    }

    @Contract(" -> new")
    public @NotNull List<T> toList() {
        return new LinkedList<>(this);
    }
}
