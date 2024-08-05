package io.github.retrooper.packetevents.packetwrappers.play.out.blockbreakanimation;

import io.github.retrooper.packetevents.packettype.PacketTypeClasses;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.api.SendableWrapper;
import io.github.retrooper.packetevents.packetwrappers.api.helper.WrappedPacketEntityAbstraction;
import io.github.retrooper.packetevents.utils.nms.NMSUtils;
import io.github.retrooper.packetevents.utils.vector.Vector3i;
import lombok.ToString;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

@ToString
public class WrappedPacketOutBlockBreakAnimation extends WrappedPacketEntityAbstraction implements SendableWrapper {

    private static Constructor<?> packetConstructor;
    private Vector3i blockPosition;
    private int destroyStage;

    public WrappedPacketOutBlockBreakAnimation(NMSPacket packet) {
        super(packet);
    }

    public WrappedPacketOutBlockBreakAnimation(int entityID, Vector3i blockPosition, int destroyStage) {
        this.entityID = entityID;
        this.blockPosition = blockPosition;
        this.destroyStage = destroyStage;
    }

    public WrappedPacketOutBlockBreakAnimation(@NotNull Entity entity, Vector3i blockPosition, int destroyStage) {
        entityID = entity.getEntityId();
        this.entity = entity;
        this.blockPosition = blockPosition;
        this.destroyStage = destroyStage;
    }

    @Override
    protected void load() {
        try {
            packetConstructor = PacketTypeClasses.Play.Server.BLOCK_BREAK_ANIMATION.getConstructor(int.class,
                    int.class, int.class, int.class, int.class);
        } catch (NoSuchMethodException e) {
            try {
                packetConstructor = PacketTypeClasses.Play.Server.BLOCK_BREAK_ANIMATION.getConstructor(int.class,
                        NMSUtils.blockPosClass, int.class);
            } catch (NoSuchMethodException e2) {
                e2.printStackTrace();
            }
        }
    }

    private Vector3i getBlockPosition() {
        if (nmsPacket != null) {
            return readBlockPosition(0);
        } else {
            return blockPosition;
        }
    }

    public void setBlockPosition(Vector3i blockPosition) {
        if (nmsPacket != null) {
            writeBlockPosition(0, blockPosition);
        } else {
            this.blockPosition = blockPosition;
        }
    }

    private int getDestroyStage() {
        if (nmsPacket != null) {
            return readInt(1);
        } else {
            return destroyStage;
        }
    }

    public void setDestroyStage(int destroyStage) {
        if (nmsPacket != null) {
            writeInt(1, destroyStage);
        } else {
            this.destroyStage = destroyStage;
        }
    }

    @Override
    public Object asNMSPacket() throws InvocationTargetException, InstantiationException, IllegalAccessException {
        Vector3i blockPos = getBlockPosition();
        Object nmsBlockPos = NMSUtils.generateNMSBlockPos(blockPos);
        return packetConstructor.newInstance(getEntityId(), nmsBlockPos, getDestroyStage());
    }
}
