package io.github.retrooper.packetevents.utils.nms;

import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;
import io.github.retrooper.packetevents.utils.reflection.Reflection;
import io.github.retrooper.packetevents.utils.reflection.SubclassUtil;
import io.github.retrooper.packetevents.utils.server.ServerVersion;
import io.github.retrooper.packetevents.utils.vector.Vector3d;
import io.github.retrooper.packetevents.utils.vector.Vector3f;
import io.github.retrooper.packetevents.utils.vector.Vector3i;
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

public final class NMSUtils {

    public static final String NMS_DIR = ServerVersion.getNMSDirectory() + ".";
    public static final String OBC_DIR = ServerVersion.getOBCDirectory() + ".";

    private static final ThreadLocal<Random> randomThreadLocal = ThreadLocal.withInitial(Random::new);

    public static Constructor<?> blockPosConstructor;
    public static Constructor<?> minecraftKeyConstructor;
    public static Constructor<?> vec3DConstructor;
    public static Constructor<?> dataWatcherConstructor;
    public static Constructor<?> packetDataSerializerConstructor;
    public static Constructor<?> itemStackConstructor;

    public static Class<?> mobEffectListClass;
    public static Class<?> nmsEntityClass;
    public static Class<?> minecraftServerClass;
    public static Class<?> craftWorldClass;
    public static Class<?> playerInteractManagerClass;
    public static Class<?> entityPlayerClass;
    public static Class<?> playerConnectionClass;
    public static Class<?> craftServerClass;
    public static Class<?> craftPlayerClass;
    public static Class<?> serverConnectionClass;
    public static Class<?> craftEntityClass;
    public static Class<?> nmsItemStackClass;
    public static Class<?> networkManagerClass;
    public static Class<?> nettyChannelClass;
    public static Class<?> gameProfileClass;
    public static Class<?> iChatBaseComponentClass;
    public static Class<?> blockPosClass;
    public static Class<?> sectionPositionClass;
    public static Class<?> vec3DClass;
    public static Class<?> channelFutureClass;
    public static Class<?> blockClass;
    public static Class<?> iBlockDataClass;
    public static Class<?> nmsWorldClass;
    public static Class<?> craftItemStackClass;
    public static Class<?> soundEffectClass;
    public static Class<?> minecraftKeyClass;
    public static Class<?> chatSerializerClass;
    public static Class<?> craftMagicNumbersClass;
    public static Class<?> worldSettingsClass;
    public static Class<?> worldServerClass;
    public static Class<?> dataWatcherClass;
    public static Class<?> dedicatedServerClass;
    public static Class<?> entityHumanClass;
    public static Class<?> packetDataSerializerClass;
    public static Class<?> byteBufClass;
    public static Class<?> dimensionManagerClass;
    public static Class<?> nmsItemClass;
    public static Class<?> iMaterialClass;
    public static Class<?> movingObjectPositionBlockClass;
    public static Class<?> boundingBoxClass;
    public static Class<?> tileEntityCommandClass;

    public static Class<? extends Enum<?>> enumDirectionClass;
    public static Class<? extends Enum<?>> enumHandClass;
    public static Class<? extends Enum<?>> enumGameModeClass;
    public static Class<? extends Enum<?>> enumDifficultyClass;
    public static Class<? extends Enum<?>> tileEntityCommandTypeClass;

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
        } catch (Exception ex) {
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
                packetDataSerializerConstructor = packetDataSerializerClass.getConstructor(NMSUtils.byteBufClass);
            } catch (NoSuchMethodException ex) {
                ex.printStackTrace();
            }
        }

        dimensionManagerClass = NMSUtils.getNMSClassWithoutException("DimensionManager");
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

        iChatBaseComponentClass = NMSUtils.getNMSClassWithoutException("IChatBaseComponent");
        if (iChatBaseComponentClass == null) {
            iChatBaseComponentClass = getNMClassWithoutException("network.chat.IChatBaseComponent");
        }

        tileEntityCommandClass = NMSUtils.getNMSClassWithoutException("TileEntityCommand");
        if (tileEntityCommandClass == null) {
            tileEntityCommandClass = NMSUtils.getNMClassWithoutException("world.level.block.entity.TileEntityCommand");
        }

        tileEntityCommandTypeClass = SubclassUtil.getEnumSubClass(tileEntityCommandClass, 0);

        vec3DClass = NMSUtils.getNMSClassWithoutException("Vec3D");
        if (vec3DClass == null) {
            vec3DClass = getNMClassWithoutException("world.phys.Vec3D");
        }

        blockPosClass = NMSUtils.getNMSClassWithoutException("BlockPosition");
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
                vec3DConstructor = NMSUtils.vec3DClass.getDeclaredConstructor(double.class, double.class, double.class);
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
            getItemId = NMSUtils.nmsItemClass.getMethod("getId", NMSUtils.nmsItemClass);
            getItemById = NMSUtils.nmsItemClass.getMethod("getById", int.class);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        enumDirectionClass = NMSUtils.getNMSEnumClassWithoutException("EnumDirection");
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
                chatSerializerClass = NMSUtils.getNMSClass("ChatSerializer");
            } catch (ClassNotFoundException e) {
                // That is fine, it is probably a subclass
                chatSerializerClass = SubclassUtil.getSubClass(iChatBaseComponentClass, "ChatSerializer");
            }

            craftMagicNumbersClass = NMSUtils.getOBCClass("util.CraftMagicNumbers");
            chatFromStringMethod = Reflection.getMethod(chatSerializerClass, 0, String.class);
            getMaterialFromNMSBlock = Reflection.getMethod(craftMagicNumbersClass, "getMaterial", Material.class, NMSUtils.blockClass);
            getNMSBlockFromMaterial = Reflection.getMethod(craftMagicNumbersClass, "getBlock", NMSUtils.blockClass, Material.class);

            if (minecraftKeyClass != null) {
                minecraftKeyConstructor = minecraftKeyClass.getConstructor(String.class);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            if (mobEffectListClass != null) {
                getMobEffectListId = Reflection.getMethod(mobEffectListClass, 0, mobEffectListClass);
                getMobEffectListById = Reflection.getMethod(mobEffectListClass, 0, int.class);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            entityPlayerPingField = entityPlayerClass.getField("ping");
        } catch (NoSuchFieldException ignored) {
            // Ignore on 1.17, we will use API (Player#getPing) to access ping
        }

        // In case its null, these methods are not needed and would cause errors
        if (blockPosClass != null) {
            getBlockPosX = Reflection.getMethod(NMSUtils.blockPosClass, "getX", int.class);
            getBlockPosY = Reflection.getMethod(NMSUtils.blockPosClass, "getY", int.class);
            getBlockPosZ = Reflection.getMethod(NMSUtils.blockPosClass, "getZ", int.class);

            // Mappings changed with 1.18
            if (getBlockPosX == null) {
                getBlockPosX = Reflection.getMethod(NMSUtils.blockPosClass, "u", int.class);
            }
            if (getBlockPosY == null) {
                getBlockPosY = Reflection.getMethod(NMSUtils.blockPosClass, "v", int.class);
            }
            if (getBlockPosZ == null) {
                getBlockPosZ = Reflection.getMethod(NMSUtils.blockPosClass, "w", int.class);
            }
        }

        worldSettingsClass = NMSUtils.getNMSClassWithoutException("WorldSettings");
        if (worldServerClass == null) {
            worldServerClass = getNMClassWithoutException("world.level.WorldSettings");
        }

        enumHandClass = getNMSEnumClassWithoutException("EnumHand");
        if (enumHandClass == null) {
            enumHandClass = getNMEnumClassWithoutException("world.EnumHand");
        }

        enumDifficultyClass = NMSUtils.getNMSEnumClassWithoutException("EnumDifficulty");
        if (enumDifficultyClass == null) {
            enumDifficultyClass = getNMEnumClassWithoutException("world.EnumDifficulty");
        }

        enumGameModeClass = NMSUtils.getNMSEnumClassWithoutException("EnumGamemode");
        if (enumGameModeClass == null) {
            enumGameModeClass = SubclassUtil.getEnumSubClass(worldSettingsClass, "EnumGamemode");
        }
        if (enumGameModeClass == null) {
            enumGameModeClass = getNMEnumClassWithoutException("world.level.EnumGamemode");
        }
    }

    public static Object getMinecraftServerInstance(Server server) {
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
        WrappedPacket minecraftServerInstanceReader = new WrappedPacket(
                new NMSPacket(getMinecraftServerInstance(Bukkit.getServer())),
                minecraftServerClass);
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

    public static @Nullable Class<? extends Enum<?>> getNMEnumClassWithoutException(String name) {
        try {
            return (Class<? extends Enum<?>>) Class.forName("net.minecraft." + name);
        } catch (ClassNotFoundException ex) {
            return null;
        }
    }

    public static @NotNull Class<? extends Enum<?>> getNMSEnumClass(String name) throws ClassNotFoundException {
        return (Class<? extends Enum<?>>) Class.forName(NMS_DIR + name);
    }

    public static @Nullable Class<? extends Enum<?>> getNMSEnumClassWithoutException(String name) {
        try {
            return (Class<? extends Enum<?>>) getNMSClass(name);
        } catch (Exception ex) {
            return null;
        }
    }

    public static @Nullable Class<?> getNMSClassWithoutException(String name) {
        try {
            return getNMSClass(name);
        } catch (Exception ex) {
            return null;
        }
    }

    public static @NotNull Class<?> getOBCClass(String name) throws ClassNotFoundException {
        return Class.forName(OBC_DIR + name);
    }

    public static @NotNull Class<?> getNettyClass(String name) throws ClassNotFoundException {
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

    public static Object getCraftPlayer(Player player) {
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

    public static @Nullable Object getPlayerConnection(Player player) {
        Object entityPlayer = getEntityPlayer(player);

        if (entityPlayer == null) {
            return null;
        }

        WrappedPacket wrappedEntityPlayer = new WrappedPacket(new NMSPacket(entityPlayer));
        return wrappedEntityPlayer.readObject(0, NMSUtils.playerConnectionClass);
    }

    public static Object getGameProfile(Player player) {
        Object entityPlayer = getEntityPlayer(player);
        WrappedPacket entityHumanWrapper = new WrappedPacket(new NMSPacket(entityPlayer), NMSUtils.entityHumanClass);
        return entityHumanWrapper.readObject(0, NMSUtils.gameProfileClass);
    }

    public static @Nullable Object getNetworkManager(Player player) {
        Object playerConnection = getPlayerConnection(player);

        if (playerConnection == null) {
            return null;
        }

        WrappedPacket wrapper = new WrappedPacket(new NMSPacket(playerConnection), playerConnectionClass);

        try {
            return wrapper.readObject(0, networkManagerClass);
        } catch (Exception ex) {
            wrapper = new WrappedPacket(new NMSPacket(playerConnection));

            try {
                return wrapper.readObject(0, networkManagerClass);
            } catch (Exception ex2) {
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

        WrappedPacket wrapper = new WrappedPacket(new NMSPacket(networkManager), networkManagerClass);
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

    public static List<Object> getNetworkManagers() {
        WrappedPacket serverConnectionWrapper = new WrappedPacket(new NMSPacket(getMinecraftServerConnection()));

        for (int i = 0; true; i++) {
            try {
                List<?> list = (List<?>) serverConnectionWrapper.readObject(i, List.class);

                for (Object obj : list) {
                    if (obj.getClass().isAssignableFrom(networkManagerClass)) {
                        return (List<Object>) list;
                    }
                }
            } catch (Exception ex) {
                break;
            }
        }
        return (List<Object>) serverConnectionWrapper.readObject(1, List.class);
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
        WrappedPacket wrapper = new WrappedPacket(new NMSPacket(craftServer));

        try {
            return wrapper.readObject(0, minecraftServerClass);
        } catch (Exception ex) {
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

    public static Object generateIChatBaseComponent(String text) {
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

    public static String readIChatBaseComponent(Object iChatBaseComponent) {
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

    public static String fromStringToJSON(String message) {
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
        WrappedPacket minecraftKeyWrapper = new WrappedPacket(new NMSPacket(minecraftKey));
        return minecraftKeyWrapper.readString(1);
    }

    public static String @NotNull [] splitMinecraftKey(@NotNull String var0, char var1) {
        String[] array = new String[]{"minecraft", var0};
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
        return new UUID(var1, var3);
    }

    public static int generateEntityId() {
        Field field = Reflection.getField(nmsEntityClass, "entityCount");

        if (field == null) {
            field = Reflection.getField(nmsEntityClass, AtomicInteger.class, 0);
        }

        try {
            if (field.getType().equals(AtomicInteger.class)) {
                // Newer versions
                AtomicInteger atomicInteger = (AtomicInteger) field.get(null);
                return atomicInteger.incrementAndGet();
            } else {
                int id = field.getInt(null) + 1;
                field.set(null, id);
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
