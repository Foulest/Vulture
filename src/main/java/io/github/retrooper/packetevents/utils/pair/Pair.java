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
package io.github.retrooper.packetevents.utils.pair;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Getter
@ToString
@AllArgsConstructor
public class Pair<A, B> {

    private final A first;
    private final B second;

    @Contract(value = "_, _ -> new", pure = true)
    public static <T, K> @NotNull Pair<T, K> of(T a, K b) {
        return new Pair<>(a, b);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Pair)) {
            return false;
        }

        Pair<?, ?> b = (Pair<?, ?>) o;
        return Objects.equals(first, b.first) && Objects.equals(second, b.second);
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }
}
