package io.github.retrooper.packetevents.packetwrappers.play.out.blockchange;

import io.github.retrooper.packetevents.packettype.PacketTypeClasses;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;
import io.github.retrooper.packetevents.packetwrappers.api.SendableWrapper;
import io.github.retrooper.packetevents.utils.nms.NMSUtils;
import io.github.retrooper.packetevents.utils.reflection.Reflection;
import io.github.retrooper.packetevents.utils.server.ServerVersion;
import io.github.retrooper.packetevents.utils.vector.Vector3i;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class WrappedPacketOutBlockChange extends WrappedPacket implements SendableWrapper {

    private static boolean v_1_17;
    private static Constructor<?> packetConstructor;
    private static Method getNMSBlockMethodCache = null;
    private static Method getNMSWorldTypeMethodCache = null;

    private Vector3i blockPos;
    private World world;
    private Material blockType;

    public WrappedPacketOutBlockChange(NMSPacket packet) {
        super(packet);
    }

    public WrappedPacketOutBlockChange(World world, Vector3i blockPos) {
        this.blockPos = blockPos;
        this.world = world;
    }

    public WrappedPacketOutBlockChange(Location location) {
        this.blockPos = new Vector3i(location);
        this.world = location.getWorld();
    }

    public WrappedPacketOutBlockChange(World world, Vector3i blockPos, Material blockType) {
        this(world, blockPos);
        this.blockType = blockType;
    }

    public WrappedPacketOutBlockChange(Location location, Material blockType) {
        this(location);
        this.blockType = blockType;
    }

    @Override
    protected void load() {
        getNMSBlockMethodCache = Reflection.getMethod(NMSUtils.iBlockDataClass, "getBlock", 0);

        if (getNMSBlockMethodCache == null) {
            Class<?> blockDataClass = NMSUtils.iBlockDataClass.getSuperclass();
            getNMSBlockMethodCache = Reflection.getMethod(blockDataClass, NMSUtils.blockClass, 0);
        }

        getNMSWorldTypeMethodCache = Reflection.getMethod(NMSUtils.nmsWorldClass, "getType", 0);

        if (getNMSWorldTypeMethodCache == null) {
            getNMSWorldTypeMethodCache = Reflection.getMethod(NMSUtils.nmsWorldClass,
                    "getBlockStateIfLoaded", 0);
        }

        v_1_17 = version.isNewerThanOrEquals(ServerVersion.v_1_17);

        if (v_1_17) {
            try {
                packetConstructor = PacketTypeClasses.Play.Server.BLOCK_CHANGE.getConstructor(NMSUtils.blockPosClass, NMSUtils.iBlockDataClass);
            } catch (NoSuchMethodException ex) {
                ex.printStackTrace();
            }
        } else {
            try {
                packetConstructor = PacketTypeClasses.Play.Server.BLOCK_CHANGE.getConstructor();
            } catch (NoSuchMethodException ex) {
                ex.printStackTrace();
            }
        }
    }

    public Vector3i getBlockPosition() {
        if (packet != null) {
            return readBlockPosition(0);
        } else {
            return blockPos;
        }
    }

    public void setBlockPosition(Vector3i blockPos) {
        if (packet != null) {
            writeBlockPosition(0, blockPos);
        } else {
            this.blockPos = blockPos;
        }
    }

    public Material getBlockType() {
        if (packet != null) {
            Object nmsBlock = null;
            Object iBlockDataObj = readObject(0, NMSUtils.iBlockDataClass);

            try {
                nmsBlock = getNMSBlockMethodCache.invoke(iBlockDataObj);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
            return NMSUtils.getMaterialFromNMSBlock(nmsBlock);
        } else {
            return blockType;
        }
    }

    public void setBlockType(Material blockType) {
        if (packet != null) {
            Object nmsBlock = NMSUtils.getNMSBlockFromMaterial(blockType);
            WrappedPacket nmsBlockWrapper = new WrappedPacket(new NMSPacket(nmsBlock), NMSUtils.blockClass);
            Object iBlockData = nmsBlockWrapper.readObject(0, NMSUtils.iBlockDataClass);
            write(NMSUtils.iBlockDataClass, 0, iBlockData);
        } else {
            this.blockType = blockType;
        }
    }

    @Override
    public Object asNMSPacket() throws Exception {
        Object packetPlayOutBlockChangeInstance;
        WrappedPacketOutBlockChange blockChange;
        Vector3i blockPosition = getBlockPosition();

        if (v_1_17) {
            Object nmsBlockPos = NMSUtils.generateNMSBlockPos(blockPosition);
            Object nmsBlock = NMSUtils.getNMSBlockFromMaterial(getBlockType());
            WrappedPacket nmsBlockWrapper = new WrappedPacket(new NMSPacket(nmsBlock), NMSUtils.blockClass);
            Object nmsIBlockData = nmsBlockWrapper.readObject(0, NMSUtils.iBlockDataClass);
            packetPlayOutBlockChangeInstance = packetConstructor.newInstance(nmsBlockPos, nmsIBlockData);

        } else {
            packetPlayOutBlockChangeInstance = packetConstructor.newInstance();
            blockChange = new WrappedPacketOutBlockChange(new NMSPacket(packetPlayOutBlockChangeInstance));
            Material bt = getBlockType();

            if (bt != null) {
                blockChange.setBlockType(bt);
            } else {
                Object nmsBlockPos = NMSUtils.generateNMSBlockPos(blockPosition);
                Object worldServer = NMSUtils.convertBukkitWorldToWorldServer(world);
                Object nmsBlockData = getNMSWorldTypeMethodCache.invoke(worldServer, nmsBlockPos);
                blockChange.write(NMSUtils.iBlockDataClass, 0, nmsBlockData);
            }

            blockChange.setBlockPosition(blockPosition);
        }
        return packetPlayOutBlockChangeInstance;
    }
}
