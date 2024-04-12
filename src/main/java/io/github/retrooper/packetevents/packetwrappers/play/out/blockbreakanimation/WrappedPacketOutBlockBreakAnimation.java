package io.github.retrooper.packetevents.packetwrappers.play.out.blockbreakanimation;

import io.github.retrooper.packetevents.packettype.PacketTypeClasses;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.api.SendableWrapper;
import io.github.retrooper.packetevents.packetwrappers.api.helper.WrappedPacketEntityAbstraction;
import io.github.retrooper.packetevents.utils.nms.NMSUtils;
import io.github.retrooper.packetevents.utils.vector.Vector3i;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;

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
        this.entityID = entity.getEntityId();
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

    public Vector3i getBlockPosition() {
        if (packet != null) {
            return readBlockPosition(0);
        } else {
            return this.blockPosition;
        }
    }

    public void setBlockPosition(Vector3i blockPosition) {
        if (packet != null) {
            writeBlockPosition(0, blockPosition);
        } else {
            this.blockPosition = blockPosition;
        }
    }

    public int getDestroyStage() {
        if (packet != null) {
            return readInt(1);
        } else {
            return this.destroyStage;
        }
    }

    public void setDestroyStage(int destroyStage) {
        if (packet != null) {
            writeInt(1, destroyStage);
        } else {
            this.destroyStage = destroyStage;
        }
    }

    @Override
    public Object asNMSPacket() throws Exception {
        Vector3i blockPosition = getBlockPosition();
        Object nmsBlockPos = NMSUtils.generateNMSBlockPos(blockPosition);
        return packetConstructor.newInstance(getEntityId(), nmsBlockPos, getDestroyStage());
    }
}
