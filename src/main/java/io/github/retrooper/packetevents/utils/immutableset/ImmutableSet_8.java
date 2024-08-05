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
package io.github.retrooper.packetevents.utils.immutableset;

import com.google.common.collect.ImmutableSet;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;

class ImmutableSet_8<T> extends ImmutableSetAbstract<T> {

    private ImmutableSet<T> immutableSet;

    ImmutableSet_8() {
        immutableSet = ImmutableSet.<T>builder().build();
    }

    ImmutableSet_8(Iterable<T> data) {
        immutableSet = ImmutableSet.<T>builder().addAll(data).build();
    }

    @SafeVarargs
    ImmutableSet_8(T @NotNull ... data) {
        ImmutableSet.Builder<T> builder = ImmutableSet.builder();

        for (T value : data) {
            builder.add(value);
        }

        immutableSet = builder.build();
    }

    @Override
    public boolean contains(T element) {
        return immutableSet.contains(element);
    }

    @Override
    public void add(T element) {
        Iterable<T> elements = new ArrayList<>(immutableSet);
        immutableSet = ImmutableSet.<T>builder().addAll(elements).add(element).build();
    }

    @SafeVarargs
    @Override
    @SuppressWarnings("FinalMethod")
    public final void addAll(T... elements) {
        Iterable<T> localElements = new ArrayList<>(immutableSet);
        immutableSet = ImmutableSet.<T>builder().addAll(localElements).addAll(Arrays.asList(elements)).build();
    }
}
