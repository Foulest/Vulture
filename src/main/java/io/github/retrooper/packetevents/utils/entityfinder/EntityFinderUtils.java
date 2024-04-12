package io.github.retrooper.packetevents.utils.entityfinder;

import io.github.retrooper.packetevents.utils.nms.NMSUtils;
import io.github.retrooper.packetevents.utils.server.ServerVersion;
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
public final class EntityFinderUtils {

    public static ServerVersion version;
    private static Class<?> worldServerClass;
    private static Method getEntityByIdMethod;
    private static Method craftWorldGetHandle;

    public static void load() {
        worldServerClass = NMSUtils.getNMSClassWithoutException("WorldServer");

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
    @Nullable
    public static Entity getEntityByIdUnsafe(World origin, int id) {
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
    public static Entity getEntityByIdWithWorldUnsafe(World world, int id) {
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
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }
}
