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
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A pair of objects.
 *
 * @param <X> The type of the first object.
 * @param <Y> The type of the second object.
 * @author Foulest
 */
@Getter
@Setter
@AllArgsConstructor
public final class Pair<X, Y> {

    private final X first;
    private final Y last;

    @Contract("_, _ -> new")
    public static <X, Y> @NotNull Pair<X, Y> of(X x, Y y) {
        return new Pair<>(x, y);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(Object obj) {
        if (!(obj instanceof Pair)) {
            return false;
        }

        Pair<X, Y> pair = (Pair<X, Y>) obj;
        return Objects.equals(first, pair.first) && Objects.equals(last, pair.last);
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, last);
    }

    @Override
    @Contract(pure = true)
    public @NotNull String toString() {
        return "Pair{" +
                "first=" + first +
                ", last=" + last +
                '}';
    }
}
