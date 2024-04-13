package io.github.retrooper.packetevents.utils.server;

import io.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;
import io.github.retrooper.packetevents.utils.boundingbox.BoundingBox;
import io.github.retrooper.packetevents.utils.entityfinder.EntityFinderUtils;
import io.github.retrooper.packetevents.utils.nms.NMSUtils;
import io.github.retrooper.packetevents.utils.npc.NPCManager;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spigotmc.SpigotConfig;

import java.util.List;
import java.util.Map;

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
    public double[] getRecentTPS() {
        return NMSUtils.recentTPS();
    }

    /**
     * Get the current TPS.
     *
     * @return Get Current TPS
     */
    public double getTPS() {
        return getRecentTPS()[0];
    }

    /**
     * Get the operating system of the local machine
     *
     * @return Get Operating System
     */
    public SystemOS getOS() {
        return SystemOS.getOS();
    }

    /**
     * Get the NPC Manager.
     *
     * @return NPC Manager
     */
    public NPCManager getNPCManager() {
        return npcManager;
    }

    public boolean isBungeeCordEnabled() {
        return SpigotConfig.bungee;
    }

    public @NotNull BoundingBox getEntityBoundingBox(Entity entity) {
        Object nmsEntity = NMSUtils.getNMSEntity(entity);
        Object aabb = NMSUtils.getNMSAxisAlignedBoundingBox(nmsEntity);
        WrappedPacket wrappedBoundingBox = new WrappedPacket(new NMSPacket(aabb));
        double minX = wrappedBoundingBox.readDouble(0);
        double minY = wrappedBoundingBox.readDouble(1);
        double minZ = wrappedBoundingBox.readDouble(2);
        double maxX = wrappedBoundingBox.readDouble(3);
        double maxY = wrappedBoundingBox.readDouble(4);
        double maxZ = wrappedBoundingBox.readDouble(5);
        return new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
    }

    private @Nullable Entity getEntityByIdIterateWorld(@NotNull World world, int entityID) {
        for (Entity entity : PacketEvents.get().getServerUtils().getEntityList(world)) {
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

    public List<Entity> getEntityList(@NotNull World world) {
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
