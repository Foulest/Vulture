package net.foulest.vulture.util.data;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

@Getter
@Setter
@SuppressWarnings("unused")
public final class ConcurrentStream<T> {

    private final Supplier<Stream<T>> supplier;
    private final Collection<T> collection;
    private final boolean parallel;

    @Contract(pure = true)
    public ConcurrentStream(@NotNull List<T> list, boolean parallel) {
        supplier = list::stream;
        collection = list;
        this.parallel = parallel;
    }

    public boolean any(Predicate<T> t) {
        return parallel ? supplier.get().parallel().anyMatch(t) : supplier.get().anyMatch(t);
    }

    public boolean all(Predicate<T> t) {
        return parallel ? supplier.get().parallel().allMatch(t) : supplier.get().allMatch(t);
    }
}
