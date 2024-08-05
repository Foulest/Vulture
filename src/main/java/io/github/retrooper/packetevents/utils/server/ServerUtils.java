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
package io.github.retrooper.packetevents.utils.server;

import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;
import io.github.retrooper.packetevents.packetwrappers.api.WrapperPacketReader;
import io.github.retrooper.packetevents.utils.boundingbox.BoundingBox;
import io.github.retrooper.packetevents.utils.entityfinder.EntityFinderUtils;
import io.github.retrooper.packetevents.utils.nms.NMSUtils;
import io.github.retrooper.packetevents.utils.npc.NPCManager;
import lombok.NoArgsConstructor;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spigotmc.SpigotConfig;

import java.util.List;
import java.util.Map;

@NoArgsConstructor
public final class ServerUtils {

    private static Class<?> geyserClass;
    private boolean geyserClassChecked;
    private final NPCManager npcManager = new NPCManager();

    // Initialized in PacketEvents#load
    public Map<Integer, Entity> entityCache;

    /**
     * Get recent TPS array from NMS.
     *
     * @return Get Recent TPS
     */
    private static double[] getRecentTPS() {
        return NMSUtils.recentTPS();
    }

    /**
     * Get the current TPS.
     *
     * @return Get Current TPS
     */
    public static double getTPS() {
        return getRecentTPS()[0];
    }

    /**
     * Get the NPC Manager.
     *
     * @return NPC Manager
     */
    public NPCManager getNPCManager() {
        return npcManager;
    }

    public static boolean isBungeeCordEnabled() {
        return SpigotConfig.bungee;
    }

    public static @NotNull BoundingBox getEntityBoundingBox(Entity entity) {
        Object nmsEntity = NMSUtils.getNMSEntity(entity);
        Object aabb = NMSUtils.getNMSAxisAlignedBoundingBox(nmsEntity);
        WrapperPacketReader wrappedBoundingBox = new WrappedPacket(new NMSPacket(aabb));
        double minX = wrappedBoundingBox.readDouble(0);
        double minY = wrappedBoundingBox.readDouble(1);
        double minZ = wrappedBoundingBox.readDouble(2);
        double maxX = wrappedBoundingBox.readDouble(3);
        double maxY = wrappedBoundingBox.readDouble(4);
        double maxZ = wrappedBoundingBox.readDouble(5);
        return new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
    }

    private @Nullable Entity getEntityByIdIterateWorld(@NotNull World world, int entityID) {
        for (Entity entity : getEntityList(world)) {
            if (entity.getEntityId() == entityID) {
                entityCache.putIfAbsent(entity.getEntityId(), entity);
                return entity;
            }
        }
        return null;
    }

    public @Nullable Entity getEntityById(@Nullable World world, int entityID) {
        Entity entity = entityCache.get(entityID);

        if (entity != null) {
            return entity;
        }
        return EntityFinderUtils.getEntityByIdUnsafe(world, entityID);
    }

    public Entity getEntityById(int entityID) {
        return getEntityById(null, entityID);
    }

    private static List<Entity> getEntityList(@NotNull World world) {
        return world.getEntities();
    }

    public boolean isGeyserAvailable() {
        if (!geyserClassChecked) {
            geyserClassChecked = true;

            try {
                geyserClass = Class.forName("org.geysermc.connector.GeyserConnector");
                return true;
            } catch (ClassNotFoundException ex) {
                return false;
            }
        }
        return geyserClass != null;
    }
}
