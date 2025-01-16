/*
 * Vulture - a server protection plugin designed for Minecraft 1.8.9 servers.
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

import lombok.Data;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Utility class to define a concurrent stream that can be used to perform parallel operations on a collection.
 *
 * @param <T> The type of the elements in the stream.
 */
@Data
public class ConcurrentStream<T> {

    private final @NotNull Supplier<Stream<T>> supplier;
    private final @NotNull Collection<T> collection;
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
