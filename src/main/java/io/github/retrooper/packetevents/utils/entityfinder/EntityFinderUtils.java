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
package io.github.retrooper.packetevents.utils.entityfinder;

import io.github.retrooper.packetevents.utils.nms.NMSUtils;
import io.github.retrooper.packetevents.utils.server.ServerVersion;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ConcurrentModificationException;
import java.util.Objects;

/**
 * Internal utility class to find entities by their Entity ID.
 *
 * @author retrooper
 * @since 1.6.8
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EntityFinderUtils {

    public static ServerVersion version;
    private static Method getEntityByIdMethod;
    private static Method craftWorldGetHandle;

    public static void load() {
        Class<?> worldServerClass = NMSUtils.getNMSClassWithoutException("WorldServer");

        if (worldServerClass == null) {
            worldServerClass = NMSUtils.getNMClassWithoutException("server.level.WorldServer");
        }

        try {
            craftWorldGetHandle = NMSUtils.craftWorldClass.getMethod("getHandle");
            getEntityByIdMethod = Objects.requireNonNull(worldServerClass).getMethod("a", int.class);
        } catch (NoSuchMethodException e) {
            try {
                getEntityByIdMethod = Objects.requireNonNull(worldServerClass).getMethod("getEntity", int.class);
            } catch (NoSuchMethodException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Get an entity by their ID.
     *
     * @param id Entity ID
     * @return Bukkit Entity.
     */
    public static @Nullable Entity getEntityByIdUnsafe(World origin, int id) {
        Entity e = getEntityByIdWithWorldUnsafe(origin, id);

        if (e != null) {
            return e;
        }

        for (World world : Bukkit.getWorlds()) {
            Entity entity = getEntityByIdWithWorldUnsafe(world, id);

            if (entity != null) {
                return entity;
            }
        }

        for (World world : Bukkit.getWorlds()) {
            try {
                for (Entity entity : world.getEntities()) {
                    if (entity.getEntityId() == id) {
                        return entity;
                    }
                }
            } catch (ConcurrentModificationException ex) {
                return null;
            }
        }
        return null;
    }

    /**
     * Get an entity by their ID, guaranteed to be in the specified world.
     *
     * @param world Bukkit world.
     * @param id    Entity ID.
     * @return Bukkit Entity.
     */
    private static @Nullable Entity getEntityByIdWithWorldUnsafe(World world, int id) {
        if (world == null) {
            return null;
        }

        if (NMSUtils.craftWorldClass == null) {
            throw new IllegalStateException("PacketEvents failed to locate the CraftWorld class.");
        }

        Object craftWorld = NMSUtils.craftWorldClass.cast(world);

        try {
            Object worldServer = craftWorldGetHandle.invoke(craftWorld);
            Object nmsEntity = getEntityByIdMethod.invoke(worldServer, id);

            if (nmsEntity == null) {
                return null;
            }
            return NMSUtils.getBukkitEntity(nmsEntity);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
