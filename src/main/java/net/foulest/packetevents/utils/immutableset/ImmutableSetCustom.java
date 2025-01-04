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
package net.foulest.packetevents.utils.immutableset;

import java.util.List;

public class ImmutableSetCustom<T> {

    private final ImmutableSetAbstract<T> immutableSetAbstract;

    public ImmutableSetCustom() {
        immutableSetAbstract = new ImmutableSet_8<>();
    }

    public ImmutableSetCustom(List<T> data) {
        immutableSetAbstract = new ImmutableSet_8<>(data);
    }

    @SafeVarargs
    public ImmutableSetCustom(T... data) {
        immutableSetAbstract = new ImmutableSet_8<>(data);
    }

    public boolean contains(T element) {
        return immutableSetAbstract.contains(element);
    }

    public void add(T element) {
        immutableSetAbstract.add(element);
    }

    @SafeVarargs
    @SuppressWarnings("FinalMethod")
    public final void addAll(T... elements) {
        immutableSetAbstract.addAll(elements);
    }
}
