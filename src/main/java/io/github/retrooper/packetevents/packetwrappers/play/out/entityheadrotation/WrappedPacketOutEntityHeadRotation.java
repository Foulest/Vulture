package io.github.retrooper.packetevents.packetwrappers.play.out.entityheadrotation;

import io.github.retrooper.packetevents.packettype.PacketTypeClasses;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.api.SendableWrapper;
import io.github.retrooper.packetevents.packetwrappers.api.helper.WrappedPacketEntityAbstraction;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;

public class WrappedPacketOutEntityHeadRotation extends WrappedPacketEntityAbstraction implements SendableWrapper {

    private static Constructor<?> packetConstructor;
    private static final float ROTATION_FACTOR = 256.0F / 360.0F;
    private float yaw;

    public WrappedPacketOutEntityHeadRotation(NMSPacket packet) {
        super(packet);
    }

    public WrappedPacketOutEntityHeadRotation(int entityID, float yaw) {
        this.entityID = entityID;
        this.yaw = yaw;
    }

    public WrappedPacketOutEntityHeadRotation(@NotNull Entity entity, float yaw) {
        this.entityID = entity.getEntityId();
        this.entity = entity;
        this.yaw = yaw;
    }

    @Override
    protected void load() {
        try {
            packetConstructor = PacketTypeClasses.Play.Server.ENTITY_HEAD_ROTATION.getConstructor();
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
        }
    }

    public float getYaw() {
        if (packet != null) {
            return readByte(0) / ROTATION_FACTOR;
        } else {
            return yaw;
        }
    }

    public void setYaw(float yaw) {
        if (packet != null) {
            writeByte(0, (byte) (yaw * ROTATION_FACTOR));
        } else {
            this.yaw = yaw;
        }
    }

    @Override
    public Object asNMSPacket() throws Exception {
        Object packetInstance = packetConstructor.newInstance();
        WrappedPacketOutEntityHeadRotation wrappedPacketOutEntityHeadRotation = new WrappedPacketOutEntityHeadRotation(new NMSPacket(packetInstance));
        wrappedPacketOutEntityHeadRotation.setEntityId(getEntityId());
        wrappedPacketOutEntityHeadRotation.setYaw(getYaw());
        return packetInstance;
    }
}
