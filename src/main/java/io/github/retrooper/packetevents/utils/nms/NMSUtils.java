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
package io.github.retrooper.packetevents.utils.nms;

import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;
import io.github.retrooper.packetevents.packetwrappers.api.WrapperPacketReader;
import io.github.retrooper.packetevents.utils.reflection.Reflection;
import io.github.retrooper.packetevents.utils.reflection.SubclassUtil;
import io.github.retrooper.packetevents.utils.server.ServerVersion;
import io.github.retrooper.packetevents.utils.vector.Vector3d;
import io.github.retrooper.packetevents.utils.vector.Vector3f;
import io.github.retrooper.packetevents.utils.vector.Vector3i;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class NMSUtils {

    private static final String NMS_DIR = ServerVersion.getNMSDirectory() + ".";
    private static final String OBC_DIR = ServerVersion.getOBCDirectory() + ".";

    private static final ThreadLocal<Random> randomThreadLocal = ThreadLocal.withInitial(Random::new);

    private static Constructor<?> blockPosConstructor;
    public static Constructor<?> minecraftKeyConstructor;
    private static Constructor<?> vec3DConstructor;
    private static Constructor<?> dataWatcherConstructor;
    private static Constructor<?> packetDataSerializerConstructor;
    private static Constructor<?> itemStackConstructor;

    private static Class<?> mobEffectListClass;
    private static Class<?> nmsEntityClass;
    private static Class<?> minecraftServerClass;
    public static Class<?> craftWorldClass;
    private static Class<?> playerInteractManagerClass;
    private static Class<?> entityPlayerClass;
    private static Class<?> playerConnectionClass;
    private static Class<?> craftServerClass;
    private static Class<?> craftPlayerClass;
    private static Class<?> serverConnectionClass;
    private static Class<?> craftEntityClass;
    public static Class<?> nmsItemStackClass;
    public static Class<?> networkManagerClass;
    public static Class<?> nettyChannelClass;
    public static Class<?> gameProfileClass;
    public static Class<?> iChatBaseComponentClass;
    public static Class<?> blockPosClass;
    public static Class<?> sectionPositionClass;
    public static Class<?> vec3DClass;
    private static Class<?> channelFutureClass;
    public static Class<?> blockClass;
    public static Class<?> iBlockDataClass;
    public static Class<?> nmsWorldClass;
    private static Class<?> craftItemStackClass;
    public static Class<?> soundEffectClass;
    public static Class<?> minecraftKeyClass;
    private static Class<?> chatSerializerClass;
    private static Class<?> craftMagicNumbersClass;
    private static Class<?> worldSettingsClass;
    private static Class<?> worldServerClass;
    public static Class<?> dataWatcherClass;
    private static Class<?> dedicatedServerClass;
    public static Class<?> entityHumanClass;
    public static Class<?> packetDataSerializerClass;
    public static Class<?> byteBufClass;
    private static Class<?> dimensionManagerClass;
    private static Class<?> nmsItemClass;
    private static Class<?> iMaterialClass;
    public static Class<?> movingObjectPositionBlockClass;
    private static Class<?> boundingBoxClass;
    private static Class<?> tileEntityCommandClass;

    public static Class<? extends Enum<?>> enumDirectionClass;
    private static Class<? extends Enum<?>> enumHandClass;
    public static Class<? extends Enum<?>> enumGameModeClass;
    public static Class<? extends Enum<?>> enumDifficultyClass;
    private static Class<? extends Enum<?>> tileEntityCommandTypeClass;

    public static Method getBlockPosX;
    public static Method getBlockPosY;
    public static Method getBlockPosZ;

    private static String nettyPrefix;

    private static Method getCraftPlayerHandle;
    private static Method getCraftEntityHandle;
    private static Method getCraftWorldHandle;
    private static Method asBukkitCopy;
    private static Method asNMSCopy;
    private static Method getMessageMethod;
    private static Method chatFromStringMethod;
    private static Method getMaterialFromNMSBlock;
    private static Method getNMSBlockFromMaterial;
    private static Method getMobEffectListId;
    private static Method getMobEffectListById;
    private static Method getItemId;
    private static Method getItemById;
    private static Method getBukkitEntity;

    private static Field entityPlayerPingField;
    private static Field entityBoundingBoxField;

    private static Object minecraftServer;
    private static Object minecraftServerConnection;

    public static void load() {
        // 1.8.8 is the only version supported.
        if (Bukkit.getVersion().contains("(MC: 1.8.8)")) {
            nettyPrefix = "io.netty.";
        } else {
            Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + "[packetevents] Your server version is not supported by PacketEvents. Shutting down server...");
            Bukkit.getServer().shutdown();
            return;
        }

        try {
            // Test if the selected netty location is valid
            getNettyClass("channel.Channel");
        } catch (ClassNotFoundException ex) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + "[packetevents] Failed to locate the netty package location for your server version. Searching...");
        }

        try {
            byteBufClass = getNettyClass("buffer.ByteBuf");
            nettyChannelClass = getNettyClass("channel.Channel");
            channelFutureClass = getNettyClass("channel.ChannelFuture");
            craftWorldClass = getOBCClass("CraftWorld");
            craftPlayerClass = getOBCClass("entity.CraftPlayer");
            craftServerClass = getOBCClass("CraftServer");
            craftEntityClass = getOBCClass("entity.CraftEntity");
            craftItemStackClass = getOBCClass("inventory.CraftItemStack");
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }

        nmsEntityClass = getNMSClassWithoutException("Entity");
        if (nmsEntityClass == null) {
            nmsEntityClass = getNMClassWithoutException("world.entity.Entity");
        }

        boundingBoxClass = getNMSClassWithoutException("AxisAlignedBB");
        if (boundingBoxClass == null) {
            boundingBoxClass = getNMClassWithoutException("world.phys.AxisAlignedBB");
        }

        entityBoundingBoxField = Reflection.getField(nmsEntityClass, boundingBoxClass, 0, true);

        if (nmsEntityClass != null) {
            getBukkitEntity = Reflection.getMethod(nmsEntityClass, craftEntityClass, 0);
        }

        minecraftServerClass = getNMSClassWithoutException("MinecraftServer");
        if (minecraftServerClass == null) {
            minecraftServerClass = getNMClassWithoutException("server.MinecraftServer");
        }

        entityPlayerClass = getNMSClassWithoutException("EntityPlayer");
        if (entityPlayerClass == null) {
            entityPlayerClass = getNMClassWithoutException("server.level.EntityPlayer");
        }

        entityHumanClass = getNMSClassWithoutException("EntityHuman");
        if (entityHumanClass == null) {
            entityHumanClass = getNMClassWithoutException("world.entity.player.EntityHuman");
        }

        playerConnectionClass = getNMSClassWithoutException("PlayerConnection");
        if (playerConnectionClass == null) {
            playerConnectionClass = getNMClassWithoutException("server.network.PlayerConnection");
        }

        serverConnectionClass = getNMSClassWithoutException("ServerConnection");
        if (serverConnectionClass == null) {
            serverConnectionClass = getNMClassWithoutException("server.network.ServerConnection");
        }

        nmsItemStackClass = getNMSClassWithoutException("ItemStack");
        if (nmsItemStackClass == null) {
            nmsItemStackClass = getNMClassWithoutException("world.item.ItemStack");
        }

        networkManagerClass = getNMSClassWithoutException("NetworkManager");
        if (networkManagerClass == null) {
            networkManagerClass = getNMClassWithoutException("network.NetworkManager");
        }

        mobEffectListClass = getNMSClassWithoutException("MobEffectList");
        if (mobEffectListClass == null) {
            mobEffectListClass = getNMClassWithoutException("world.effect.MobEffectList");
        }

        playerInteractManagerClass = getNMSClassWithoutException("PlayerInteractManager");
        if (playerInteractManagerClass == null) {
            playerInteractManagerClass = getNMClassWithoutException("server.level.PlayerInteractManager");
        }

        blockClass = getNMSClassWithoutException("Block");
        if (blockClass == null) {
            blockClass = getNMClassWithoutException("world.level.block.Block");
        }

        // IBlockData doesn't exist on 1.7.10
        iBlockDataClass = getNMSClassWithoutException("IBlockData");
        if (iBlockDataClass == null) {
            iBlockDataClass = getNMClassWithoutException("world.level.block.state.IBlockData");
        }

        nmsWorldClass = getNMSClassWithoutException("World");
        if (nmsWorldClass == null) {
            nmsWorldClass = getNMClassWithoutException("world.level.World");
        }

        soundEffectClass = getNMSClassWithoutException("SoundEffect");
        if (soundEffectClass == null) {
            soundEffectClass = getNMClassWithoutException("sounds.SoundEffect");
        }

        minecraftKeyClass = getNMSClassWithoutException("MinecraftKey");
        if (minecraftKeyClass == null) {
            minecraftKeyClass = getNMClassWithoutException("resources.MinecraftKey");
        }

        worldServerClass = getNMSClassWithoutException("WorldServer");
        if (worldServerClass == null) {
            worldServerClass = getNMClassWithoutException("server.level.WorldServer");
        }

        dataWatcherClass = getNMSClassWithoutException("DataWatcher");
        if (dataWatcherClass == null) {
            dataWatcherClass = getNMClassWithoutException("network.syncher.DataWatcher");
        }

        nmsItemClass = getNMSClassWithoutException("Item");
        if (nmsItemClass == null) {
            nmsItemClass = getNMClassWithoutException("world.item.Item");
        }

        iMaterialClass = getNMSClassWithoutException("IMaterial");
        if (iMaterialClass == null) {
            iMaterialClass = getNMClassWithoutException("world.level.IMaterial");
        }

        dedicatedServerClass = getNMSClassWithoutException("DedicatedServer");
        if (dedicatedServerClass == null) {
            dedicatedServerClass = getNMClassWithoutException("server.dedicated.DedicatedServer");
        }

        packetDataSerializerClass = getNMSClassWithoutException("PacketDataSerializer");
        if (packetDataSerializerClass == null) {
            packetDataSerializerClass = getNMClassWithoutException("network.PacketDataSerializer");
        }

        if (packetDataSerializerClass != null) {
            try {
                packetDataSerializerConstructor = packetDataSerializerClass.getConstructor(byteBufClass);
            } catch (NoSuchMethodException ex) {
                ex.printStackTrace();
            }
        }

        dimensionManagerClass = getNMSClassWithoutException("DimensionManager");
        if (dimensionManagerClass == null) {
            dimensionManagerClass = getNMClassWithoutException("world.level.dimension.DimensionManager");
        }

        try {
            gameProfileClass = Class.forName("net.minecraft.util.com.mojang.authlib.GameProfile");
        } catch (ClassNotFoundException e) {
            try {
                gameProfileClass = Class.forName("com.mojang.authlib.GameProfile");
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
            }
        }

        iChatBaseComponentClass = getNMSClassWithoutException("IChatBaseComponent");
        if (iChatBaseComponentClass == null) {
            iChatBaseComponentClass = getNMClassWithoutException("network.chat.IChatBaseComponent");
        }

        tileEntityCommandClass = getNMSClassWithoutException("TileEntityCommand");
        if (tileEntityCommandClass == null) {
            tileEntityCommandClass = getNMClassWithoutException("world.level.block.entity.TileEntityCommand");
        }

        tileEntityCommandTypeClass = SubclassUtil.getEnumSubClass(tileEntityCommandClass, 0);

        vec3DClass = getNMSClassWithoutException("Vec3D");
        if (vec3DClass == null) {
            vec3DClass = getNMClassWithoutException("world.phys.Vec3D");
        }

        blockPosClass = getNMSClassWithoutException("BlockPosition");
        if (blockPosClass == null) {
            blockPosClass = getNMClassWithoutException("core.BlockPosition");
        }

        sectionPositionClass = getNMSClassWithoutException("SectionPosition");
        if (sectionPositionClass == null) {
            sectionPositionClass = getNMClassWithoutException("core.SectionPosition");
        }

        try {
            // If null, it is 1.7.10
            if (blockPosClass != null) {
                blockPosConstructor = blockPosClass.getConstructor(int.class, int.class, int.class);
            }

            if (vec3DClass != null) {
                vec3DConstructor = vec3DClass.getDeclaredConstructor(double.class, double.class, double.class);
                vec3DConstructor.setAccessible(true);
            }

            if (dataWatcherClass != null) {
                dataWatcherConstructor = dataWatcherClass.getConstructor(nmsEntityClass);
            }

            if (nmsItemStackClass != null && iMaterialClass != null) {
                itemStackConstructor = nmsItemStackClass.getDeclaredConstructor(iMaterialClass);
            }
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
        }

        try {
            getItemId = nmsItemClass.getMethod("getId", nmsItemClass);
            getItemById = nmsItemClass.getMethod("getById", int.class);
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
        }

        enumDirectionClass = getNMSEnumClassWithoutException("EnumDirection");
        if (enumDirectionClass == null) {
            enumDirectionClass = getNMEnumClassWithoutException("core.EnumDirection");
        }

        // METHODS

        try {
            getCraftPlayerHandle = craftPlayerClass.getMethod("getHandle");
            getCraftEntityHandle = craftEntityClass.getMethod("getHandle");
            getCraftWorldHandle = craftWorldClass.getMethod("getHandle");
            asBukkitCopy = craftItemStackClass.getMethod("asBukkitCopy", nmsItemStackClass);
            asNMSCopy = craftItemStackClass.getMethod("asNMSCopy", ItemStack.class);
            getMessageMethod = Reflection.getMethodCheckContainsString(iChatBaseComponentClass, "c", String.class);

            if (getMessageMethod == null) {
                getMessageMethod = Reflection.getMethodCheckContainsString(iChatBaseComponentClass, "Plain", String.class);

                if (getMessageMethod == null) {
                    getMessageMethod = Reflection.getMethodCheckContainsString(iChatBaseComponentClass, "String", String.class);
                }
            }

            // In 1.8.3+ the ChatSerializer class is declared in the IChatBaseComponent class, so we have to handle that
            try {
                chatSerializerClass = getNMSClass("ChatSerializer");
            } catch (ClassNotFoundException e) {
                // That is fine, it is probably a subclass
                chatSerializerClass = SubclassUtil.getSubClass(iChatBaseComponentClass, "ChatSerializer");
            }

            craftMagicNumbersClass = getOBCClass("util.CraftMagicNumbers");
            chatFromStringMethod = Reflection.getMethod(chatSerializerClass, 0, String.class);
            getMaterialFromNMSBlock = Reflection.getMethod(craftMagicNumbersClass, "getMaterial", Material.class, blockClass);
            getNMSBlockFromMaterial = Reflection.getMethod(craftMagicNumbersClass, "getBlock", blockClass, Material.class);

            if (minecraftKeyClass != null) {
                minecraftKeyConstructor = minecraftKeyClass.getConstructor(String.class);
            }
        } catch (ClassNotFoundException | NoSuchMethodException ex) {
            ex.printStackTrace();
        }

        try {
            if (mobEffectListClass != null) {
                getMobEffectListId = Reflection.getMethod(mobEffectListClass, 0, mobEffectListClass);
                getMobEffectListById = Reflection.getMethod(mobEffectListClass, 0, int.class);
            }
        } catch (RuntimeException ex) {
            ex.printStackTrace();
        }

        try {
            entityPlayerPingField = entityPlayerClass.getField("ping");
        } catch (NoSuchFieldException ignored) {
            // Ignore on 1.17, we will use API (Player#getPing) to access ping
        }

        // In case its null, these methods are not needed and would cause errors
        if (blockPosClass != null) {
            getBlockPosX = Reflection.getMethod(blockPosClass, "getX", int.class);
            getBlockPosY = Reflection.getMethod(blockPosClass, "getY", int.class);
            getBlockPosZ = Reflection.getMethod(blockPosClass, "getZ", int.class);

            // Mappings changed with 1.18
            if (getBlockPosX == null) {
                getBlockPosX = Reflection.getMethod(blockPosClass, "u", int.class);
            }
            if (getBlockPosY == null) {
                getBlockPosY = Reflection.getMethod(blockPosClass, "v", int.class);
            }
            if (getBlockPosZ == null) {
                getBlockPosZ = Reflection.getMethod(blockPosClass, "w", int.class);
            }
        }

        worldSettingsClass = getNMSClassWithoutException("WorldSettings");
        if (worldServerClass == null) {
            worldServerClass = getNMClassWithoutException("world.level.WorldSettings");
        }

        enumHandClass = getNMSEnumClassWithoutException("EnumHand");
        if (enumHandClass == null) {
            enumHandClass = getNMEnumClassWithoutException("world.EnumHand");
        }

        enumDifficultyClass = getNMSEnumClassWithoutException("EnumDifficulty");
        if (enumDifficultyClass == null) {
            enumDifficultyClass = getNMEnumClassWithoutException("world.EnumDifficulty");
        }

        enumGameModeClass = getNMSEnumClassWithoutException("EnumGamemode");
        if (enumGameModeClass == null) {
            enumGameModeClass = SubclassUtil.getEnumSubClass(worldSettingsClass, "EnumGamemode");
        }
        if (enumGameModeClass == null) {
            enumGameModeClass = getNMEnumClassWithoutException("world.level.EnumGamemode");
        }
    }

    private static Object getMinecraftServerInstance(Server server) {
        if (minecraftServer == null) {
            try {
                minecraftServer = Reflection.getField(craftServerClass, minecraftServerClass, 0).get(server);
            } catch (IllegalAccessException ex) {
                ex.printStackTrace();
            }
        }
        return minecraftServer;
    }

    public static Object getMinecraftServerConnection() {
        if (minecraftServerConnection == null) {
            try {
                minecraftServerConnection = Reflection.getField(minecraftServerClass, serverConnectionClass, 0).get(getMinecraftServerInstance(Bukkit.getServer()));
            } catch (IllegalAccessException ex) {
                ex.printStackTrace();
            }
        }
        return minecraftServerConnection;
    }

    public static double[] recentTPS() {
        WrapperPacketReader minecraftServerInstanceReader = new WrappedPacket(
                new NMSPacket(getMinecraftServerInstance(Bukkit.getServer())), minecraftServerClass);
        return minecraftServerInstanceReader.readDoubleArray(0);
    }

    public static @NotNull Class<?> getNMSClass(String name) throws ClassNotFoundException {
        return Class.forName(NMS_DIR + name);
    }

    public static @NotNull Class<?> getNMClass(String name) throws ClassNotFoundException {
        return Class.forName("net.minecraft." + name);
    }

    public static @Nullable Class<?> getNMClassWithoutException(String name) {
        try {
            return Class.forName("net.minecraft." + name);
        } catch (ClassNotFoundException ex) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public static @Nullable <E extends Enum<E>> Class<E> getNMEnumClassWithoutException(String name) {
        try {
            return (Class<E>) Class.forName("net.minecraft." + name);
        } catch (ClassNotFoundException ex) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public static @NotNull <E extends Enum<E>> Class<E> getNMSEnumClass(String name) throws ClassNotFoundException {
        return (Class<E>) Class.forName(NMS_DIR + name);
    }

    @SuppressWarnings("unchecked")
    public static @Nullable <E extends Enum<E>> Class<E> getNMSEnumClassWithoutException(String name) {
        try {
            return (Class<E>) getNMSClass(name);
        } catch (ClassNotFoundException ex) {
            return null;
        }
    }

    public static @Nullable Class<?> getNMSClassWithoutException(String name) {
        try {
            return getNMSClass(name);
        } catch (ClassNotFoundException ex) {
            return null;
        }
    }

    private static @NotNull Class<?> getOBCClass(String name) throws ClassNotFoundException {
        return Class.forName(OBC_DIR + name);
    }

    private static @NotNull Class<?> getNettyClass(String name) throws ClassNotFoundException {
        return Class.forName(nettyPrefix + name);
    }

    public static Entity getBukkitEntity(Object nmsEntity) {
        Object craftEntity = null;

        try {
            craftEntity = getBukkitEntity.invoke(nmsEntity);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            ex.printStackTrace();
        }
        return (Entity) craftEntity;
    }

    public static @Nullable Object getNMSEntity(Entity entity) {
        Object craftEntity = craftEntityClass.cast(entity);

        try {
            return getCraftEntityHandle.invoke(craftEntity);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static @Nullable Object getNMSAxisAlignedBoundingBox(Object nmsEntity) {
        try {
            return entityBoundingBoxField.get(nmsEntityClass.cast(nmsEntity));
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private static Object getCraftPlayer(Player player) {
        return craftPlayerClass.cast(player);
    }

    public static @Nullable Object getEntityPlayer(Player player) {
        Object craftPlayer = getCraftPlayer(player);

        try {
            return getCraftPlayerHandle.invoke(craftPlayer);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private static @Nullable Object getPlayerConnection(Player player) {
        Object entityPlayer = getEntityPlayer(player);

        if (entityPlayer == null) {
            return null;
        }

        WrapperPacketReader wrappedEntityPlayer = new WrappedPacket(new NMSPacket(entityPlayer));
        return wrappedEntityPlayer.readObject(0, playerConnectionClass);
    }

    public static Object getGameProfile(Player player) {
        Object entityPlayer = getEntityPlayer(player);
        WrapperPacketReader entityHumanWrapper = new WrappedPacket(new NMSPacket(entityPlayer), entityHumanClass);
        return entityHumanWrapper.readObject(0, gameProfileClass);
    }

    private static @Nullable Object getNetworkManager(Player player) {
        Object playerConnection = getPlayerConnection(player);

        if (playerConnection == null) {
            return null;
        }

        WrappedPacket wrapper = new WrappedPacket(new NMSPacket(playerConnection), playerConnectionClass);

        try {
            return wrapper.readObject(0, networkManagerClass);
        } catch (RuntimeException ex) {
            wrapper = new WrappedPacket(new NMSPacket(playerConnection));

            try {
                return wrapper.readObject(0, networkManagerClass);
            } catch (RuntimeException ex2) {
                // Support for some custom plugins.
                playerConnection = wrapper.read(0, playerConnectionClass);
                wrapper = new WrappedPacket(new NMSPacket(playerConnection), playerConnectionClass);
                return wrapper.readObject(0, networkManagerClass);
            }
        }
    }

    public static @Nullable Object getChannel(Player player) {
        Object networkManager = getNetworkManager(player);

        if (networkManager == null) {
            return null;
        }

        WrapperPacketReader wrapper = new WrappedPacket(new NMSPacket(networkManager), networkManagerClass);
        return wrapper.readObject(0, nettyChannelClass);
    }

    public static int getPlayerPing(Player player) {
        if (entityPlayerPingField == null) {
            return -1;
        }

        Object entityPlayer = getEntityPlayer(player);

        try {
            return entityPlayerPingField.getInt(entityPlayer);
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        }
        return -1;
    }

    @SuppressWarnings("unchecked")
    public static List<Object> getNetworkManagers() {
        WrappedPacket serverConnectionWrapper = new WrappedPacket(new NMSPacket(getMinecraftServerConnection()));

        for (int i = 0; true; i++) {
            try {
                List<Object> list = serverConnectionWrapper.readObject(i, List.class);

                for (Object obj : list) {
                    if (obj.getClass().isAssignableFrom(networkManagerClass)) {
                        return list;
                    }
                }
            } catch (RuntimeException ex) {
                break;
            }
        }
        return serverConnectionWrapper.readObject(1, List.class);
    }

    public static @Nullable ItemStack toBukkitItemStack(Object nmsItemStack) {
        try {
            return (ItemStack) asBukkitCopy.invoke(null, nmsItemStack);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static @Nullable Object toNMSItemStack(ItemStack stack) {
        try {
            return asNMSCopy.invoke(null, stack);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static @Nullable Object convertBukkitServerToNMSServer(Server server) {
        Object craftServer = craftServerClass.cast(server);
        WrapperPacketReader wrapper = new WrappedPacket(new NMSPacket(craftServer));

        try {
            return wrapper.readObject(0, minecraftServerClass);
        } catch (RuntimeException ex) {
            wrapper.readObject(0, dedicatedServerClass);
        }
        return null;
    }

    public static @Nullable Object convertBukkitWorldToWorldServer(World world) {
        Object craftWorld = craftWorldClass.cast(world);

        try {
            return getCraftWorldHandle.invoke(craftWorld);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static @Nullable Object generateIChatBaseComponent(String text) {
        if (text == null) {
            return null;
        }

        try {
            return chatFromStringMethod.invoke(null, text);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static Object @NotNull [] generateIChatBaseComponents(String @NotNull ... texts) {
        Object[] components = new Object[texts.length];

        for (int i = 0; i < components.length; i++) {
            components[i] = generateIChatBaseComponent(texts[i]);
        }
        return components;
    }

    public static @Nullable String readIChatBaseComponent(Object iChatBaseComponent) {
        if (iChatBaseComponent == null) {
            return null;
        }

        try {
            return getMessageMethod.invoke(iChatBaseComponent).toString();
        } catch (IllegalAccessException | InvocationTargetException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static String @NotNull [] readIChatBaseComponents(Object @NotNull ... components) {
        String[] texts = new String[components.length];

        for (int i = 0; i < texts.length; i++) {
            texts[i] = readIChatBaseComponent(components[i]);
        }
        return texts;
    }

    public static @Nullable String fromStringToJSON(String message) {
        if (message == null) {
            return null;
        }
        return "{\"text\": \"" + message + "\"}";
    }

    public static @Nullable Object generateNMSBlockPos(@NotNull Vector3i blockPosition) {
        try {
            return blockPosConstructor.newInstance(blockPosition.x, blockPosition.y, blockPosition.z);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static String getStringFromMinecraftKey(Object minecraftKey) {
        WrapperPacketReader minecraftKeyWrapper = new WrappedPacket(new NMSPacket(minecraftKey));
        return minecraftKeyWrapper.readString(1);
    }

    public static String @NotNull [] splitMinecraftKey(@NotNull String var0, char var1) {
        String[] array = {"minecraft", var0};
        int index = var0.indexOf(var1);

        if (index >= 0) {
            array[1] = var0.substring(index + 1);

            if (index >= 1) {
                array[0] = var0.substring(0, index);
            }
        }
        return array;
    }

    public static @Nullable Object generateMinecraftKeyNew(String text) {
        try {
            return minecraftKeyConstructor.newInstance(text);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static Object generateVec3D(@NotNull Vector3f vector) {
        return generateVec3D(vector.x, vector.y, vector.z);
    }

    public static Object generateVec3D(@NotNull Vector3d vector) {
        return generateVec3D(vector.x, vector.y, vector.z);
    }

    public static @Nullable Object generateVec3D(double x, double y, double z) {
        try {
            return vec3DConstructor.newInstance(x, y, z);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static @Nullable Material getMaterialFromNMSBlock(Object nmsBlock) {
        try {
            return (Material) getMaterialFromNMSBlock.invoke(null, nmsBlock);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static @Nullable Object getNMSBlockFromMaterial(Material material) {
        try {
            return getNMSBlockFromMaterial.invoke(null, material);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static @Nullable Object generateDataWatcher(Object nmsEntity) {
        try {
            return dataWatcherConstructor.newInstance(nmsEntity);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static @NotNull UUID generateUUID() {
        long var1 = randomThreadLocal.get().nextLong() & -61441L | 16384L;
        long var3 = randomThreadLocal.get().nextLong() & 4611686018427387903L | -9223372036854775808L;
        UUID uuid = new UUID(var1, var3);
        randomThreadLocal.remove();
        return uuid;
    }

    public static int generateEntityId() {
        Field field = Reflection.getField(nmsEntityClass, "entityCount");

        if (field == null) {
            field = Reflection.getField(nmsEntityClass, AtomicInteger.class, 0);
        }

        try {
            if (field != null && field.getType().equals(AtomicInteger.class)) {
                // Newer versions
                AtomicInteger atomicInteger = (AtomicInteger) field.get(null);
                return atomicInteger.incrementAndGet();
            } else {
                int id = 0;

                if (field != null) {
                    id = field.getInt(null) + 1;
                    field.set(null, id);
                }
                return id;
            }
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        }
        throw new IllegalStateException("Failed to generate a new unique entity ID!");
    }

    public static int getEffectId(Object nmsMobEffectList) {
        try {
            return (int) getMobEffectListId.invoke(null, nmsMobEffectList);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            ex.printStackTrace();
        }
        return -1;
    }

    public static @Nullable Object getMobEffectListById(int effectID) {
        try {
            return getMobEffectListById.invoke(null, effectID);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static int getNMSItemId(Object nmsItem) {
        try {
            return (int) getItemId.invoke(null, nmsItem);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            ex.printStackTrace();
        }
        return -1;
    }

    public static @Nullable Object getNMSItemById(int id) {
        try {
            return getItemById.invoke(null, id);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static @Nullable Object generatePacketDataSerializer(Object byteBuf) {
        try {
            return packetDataSerializerConstructor.newInstance(byteBuf);
        } catch (InstantiationException | InvocationTargetException | IllegalAccessException ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
