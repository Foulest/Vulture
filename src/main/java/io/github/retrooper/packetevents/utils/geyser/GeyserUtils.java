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
package io.github.retrooper.packetevents.utils.geyser;

import io.github.retrooper.packetevents.utils.reflection.Reflection;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GeyserUtils {

    private static Class<?> geyserClass;
    private static Class<?> geyserApiClass;
    private static Method geyserApiMethod;
    private static Method connectionByUuidMethod;

    public static boolean isGeyserPlayer(UUID uuid) {
        if (geyserClass == null) {
            geyserClass = Reflection.getClassByNameWithoutException("org.geysermc.api.Geyser");
        }

        if (geyserClass == null) {
            return false;
        }

        if (geyserApiClass == null) {
            geyserApiClass = Reflection.getClassByNameWithoutException("org.geysermc.api.GeyserApiBase");
        }

        if (geyserApiMethod == null) {
            geyserApiMethod = Reflection.getMethod(geyserClass, "api", null);
        }

        if (connectionByUuidMethod == null) {
            connectionByUuidMethod = Reflection.getMethod(geyserApiClass, "connectionByUuid", 0);
        }

        Object apiInstance = null;

        try {
            apiInstance = Objects.requireNonNull(geyserApiMethod).invoke(null);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            ex.printStackTrace();
        }

        Object connection = null;

        try {
            if (apiInstance != null) {
                connection = connectionByUuidMethod.invoke(apiInstance, uuid);
            }
        } catch (IllegalAccessException | InvocationTargetException ex) {
            ex.printStackTrace();
        }
        return connection != null;
    }
}
