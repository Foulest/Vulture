package io.github.retrooper.packetevents.utils.server;

import io.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;
import io.github.retrooper.packetevents.utils.boundingbox.BoundingBox;
import io.github.retrooper.packetevents.utils.entityfinder.EntityFinderUtils;
import io.github.retrooper.packetevents.utils.nms.NMSUtils;
import io.github.retrooper.packetevents.utils.npc.NPCManager;
import io.github.retrooper.packetevents.utils.reflection.Reflection;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spigotmc.SpigotConfig;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class ServerUtils {

    private static Method getLevelEntityGetterIterable;
    private static Class<?> persistentEntitySectionManagerClass;
    private static Class<?> levelEntityGetterClass;
    private static byte v_1_17 = -1;
    private static Class<?> geyserClass;
    private boolean geyserClassChecked;
    private final NPCManager npcManager = new NPCManager();

    // Initialized in PacketEvents#load
    public Map<Integer, Entity> entityCache;

    /**
     * Get the server version.
     *
     * @return Get Server Version
     */
    public ServerVersion getVersion() {
        return ServerVersion.getVersion();
    }

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

    @Nullable
    private Entity getEntityByIdIterateWorld(@NotNull World world, int entityID) {
        for (Entity entity : PacketEvents.get().getServerUtils().getEntityList(world)) {
            if (entity.getEntityId() == entityID) {
                entityCache.putIfAbsent(entity.getEntityId(), entity);
                return entity;
            }
        }
        return null;
    }

    @Nullable
    public Entity getEntityById(@Nullable World world, int entityID) {
        Entity e = entityCache.get(entityID);

        if (e != null) {
            return e;
        }

        if (v_1_17 == -1) {
            v_1_17 = (byte) (getVersion().isNewerThanOrEquals(ServerVersion.v_1_17) ? 1 : 0);
        }

        if (v_1_17 == 1) {
            try {
                if (world != null) {
                    Entity newEntity = getEntityByIdIterateWorld(world, entityID);

                    if (newEntity != null) {
                        return newEntity;
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            try {
                for (World w : Bukkit.getWorlds()) {
                    Entity newEntity = getEntityByIdIterateWorld(w, entityID);

                    if (newEntity != null) {
                        return newEntity;
                    }
                }
            } catch (Exception ex) {
                // No entity found
                return null;
            }
        } else {
            return EntityFinderUtils.getEntityByIdUnsafe(world, entityID);
        }
        return null;
    }

    @Nullable
    public Entity getEntityById(int entityID) {
        return getEntityById(null, entityID);
    }

    public List<Entity> getEntityList(World world) {
        if (v_1_17 == -1) {
            v_1_17 = (byte) (getVersion().isNewerThanOrEquals(ServerVersion.v_1_17) ? 1 : 0);
        }

        if (v_1_17 == 1) {
            if (persistentEntitySectionManagerClass == null) {
                persistentEntitySectionManagerClass = NMSUtils.getNMClassWithoutException("world.level.entity.PersistentEntitySectionManager");
            }

            if (levelEntityGetterClass == null) {
                levelEntityGetterClass = NMSUtils.getNMClassWithoutException("world.level.entity.LevelEntityGetter");
            }

            if (getLevelEntityGetterIterable == null) {
                getLevelEntityGetterIterable = Reflection.getMethod(levelEntityGetterClass, Iterable.class, 0);
            }

            Object worldServer = NMSUtils.convertBukkitWorldToWorldServer(world);
            WrappedPacket wrappedWorldServer = new WrappedPacket(new NMSPacket(worldServer));
            Object persistentEntitySectionManager = wrappedWorldServer.readObject(0, persistentEntitySectionManagerClass);
            WrappedPacket wrappedPersistentEntitySectionManager = new WrappedPacket(new NMSPacket(persistentEntitySectionManager));
            Object levelEntityGetter = wrappedPersistentEntitySectionManager.readObject(0, levelEntityGetterClass);
            Iterable<Object> nmsEntitiesIterable = null;

            try {
                nmsEntitiesIterable = (Iterable<Object>) getLevelEntityGetterIterable.invoke(levelEntityGetter);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }

            List<Entity> entityList = new ArrayList<>();

            if (nmsEntitiesIterable != null) {
                for (Object nmsEntity : nmsEntitiesIterable) {
                    Entity bukkitEntity = NMSUtils.getBukkitEntity(nmsEntity);
                    entityList.add(bukkitEntity);
                }
            }
            return entityList;
        } else {
            return world.getEntities();
        }
    }

    public boolean isGeyserAvailable() {
        if (!geyserClassChecked) {
            geyserClassChecked = true;

            try {
                geyserClass = Class.forName("org.geysermc.connector.GeyserConnector");
                return true;
            } catch (ClassNotFoundException e) {
                return false;
            }
        }
        return geyserClass != null;
    }
}
