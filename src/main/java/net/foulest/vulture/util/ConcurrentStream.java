package net.foulest.vulture.util;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

@Getter
@Setter
public final class ConcurrentStream<T> {

    private final Supplier<Stream<T>> supplier;
    private final Collection<T> collection;
    private final boolean parallel;

    public ConcurrentStream(@NonNull List<T> list, boolean parallel) {
        supplier = list::stream;
        collection = list;
        this.parallel = parallel;
    }

    public boolean any(@NonNull Predicate<T> t) {
        return parallel ? supplier.get().parallel().anyMatch(t) : supplier.get().anyMatch(t);
    }

    public boolean all(@NonNull Predicate<T> t) {
        return parallel ? supplier.get().parallel().allMatch(t) : supplier.get().allMatch(t);
    }
}
