package io.github.retrooper.packetevents.packetwrappers.play.out.blockchange;

import io.github.retrooper.packetevents.packettype.PacketTypeClasses;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;
import io.github.retrooper.packetevents.packetwrappers.api.SendableWrapper;
import io.github.retrooper.packetevents.packetwrappers.api.WrapperPacketReader;
import io.github.retrooper.packetevents.utils.nms.NMSUtils;
import io.github.retrooper.packetevents.utils.reflection.Reflection;
import io.github.retrooper.packetevents.utils.vector.Vector3i;
import lombok.ToString;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@ToString
public class WrappedPacketOutBlockChange extends WrappedPacket implements SendableWrapper {

    private static Constructor<?> packetConstructor;
    private static Method getNMSBlockMethodCache;
    private static Method getNMSWorldTypeMethodCache;

    private Vector3i blockPos;
    private World world;
    private Material blockType;

    private WrappedPacketOutBlockChange(NMSPacket packet) {
        super(packet);
    }

    private WrappedPacketOutBlockChange(World world, Vector3i blockPos) {
        this.blockPos = blockPos;
        this.world = world;
    }

    private WrappedPacketOutBlockChange(Location location) {
        blockPos = new Vector3i(location);
        world = location.getWorld();
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

        try {
            packetConstructor = PacketTypeClasses.Play.Server.BLOCK_CHANGE.getConstructor();
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
        }
    }

    private Vector3i getBlockPosition() {
        if (nmsPacket != null) {
            return readBlockPosition(0);
        } else {
            return blockPos;
        }
    }

    private void setBlockPosition(Vector3i blockPos) {
        if (nmsPacket != null) {
            writeBlockPosition(0, blockPos);
        } else {
            this.blockPos = blockPos;
        }
    }

    private Material getBlockType() {
        if (nmsPacket != null) {
            Object nmsBlock = null;
            Object iBlockDataObj = readObject(0, NMSUtils.iBlockDataClass);

            try {
                nmsBlock = getNMSBlockMethodCache.invoke(iBlockDataObj);
            } catch (IllegalAccessException | InvocationTargetException ex) {
                ex.printStackTrace();
            }
            return NMSUtils.getMaterialFromNMSBlock(nmsBlock);
        } else {
            return blockType;
        }
    }

    private void setBlockType(Material blockType) {
        if (nmsPacket != null) {
            Object nmsBlock = NMSUtils.getNMSBlockFromMaterial(blockType);
            WrapperPacketReader nmsBlockWrapper = new WrappedPacket(new NMSPacket(nmsBlock), NMSUtils.blockClass);
            Object iBlockData = nmsBlockWrapper.readObject(0, NMSUtils.iBlockDataClass);
            write(NMSUtils.iBlockDataClass, 0, iBlockData);
        } else {
            this.blockType = blockType;
        }
    }

    @Override
    public Object asNMSPacket() throws InvocationTargetException, InstantiationException, IllegalAccessException {
        Object packetPlayOutBlockChangeInstance = packetConstructor.newInstance();
        WrappedPacketOutBlockChange blockChange = new WrappedPacketOutBlockChange(new NMSPacket(packetPlayOutBlockChangeInstance));
        Vector3i blockPosition = getBlockPosition();
        Material material = getBlockType();

        if (material != null) {
            blockChange.setBlockType(material);
        } else {
            Object nmsBlockPos = NMSUtils.generateNMSBlockPos(blockPosition);
            Object worldServer = NMSUtils.convertBukkitWorldToWorldServer(world);
            Object nmsBlockData = getNMSWorldTypeMethodCache.invoke(worldServer, nmsBlockPos);
            blockChange.write(NMSUtils.iBlockDataClass, 0, nmsBlockData);
        }

        blockChange.setBlockPosition(blockPosition);
        return packetPlayOutBlockChangeInstance;
    }
}
