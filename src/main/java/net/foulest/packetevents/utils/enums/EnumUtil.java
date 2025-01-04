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
package net.foulest.packetevents.utils.enums;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EnumUtil {

    public static <E extends Enum<E>> @Nullable E valueOf(@NotNull Class<E> cls, String constantName) {
        for (E enumConstant : cls.getEnumConstants()) {
            if (enumConstant.name().equals(constantName)) {
                return enumConstant;
            }
        }
        return null;
    }

    public static <E extends Enum<E>> E valueByIndex(@NotNull Class<E> cls, int index) {
        return cls.getEnumConstants()[index];
    }
}
