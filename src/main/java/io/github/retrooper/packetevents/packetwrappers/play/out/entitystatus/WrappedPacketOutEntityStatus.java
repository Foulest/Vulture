package io.github.retrooper.packetevents.packetwrappers.play.out.entitystatus;

import io.github.retrooper.packetevents.packettype.PacketTypeClasses;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.api.SendableWrapper;
import io.github.retrooper.packetevents.packetwrappers.api.helper.WrappedPacketEntityAbstraction;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;

public class WrappedPacketOutEntityStatus extends WrappedPacketEntityAbstraction implements SendableWrapper {

    private static Constructor<?> packetConstructor;
    private byte status;

    public WrappedPacketOutEntityStatus(NMSPacket packet) {
        super(packet);
    }

    public WrappedPacketOutEntityStatus(@NotNull Entity entity, byte status) {
        entityID = entity.getEntityId();
        this.entity = entity;
        this.status = status;
    }

    public WrappedPacketOutEntityStatus(int entityID, byte status) {
        this.entityID = entityID;
        this.status = status;
    }

    @Override
    protected void load() {
        try {
            packetConstructor = PacketTypeClasses.Play.Server.ENTITY_STATUS.getConstructor();
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
        }
    }

    public byte getEntityStatus() {
        if (packet != null) {
            return readByte(0);
        } else {
            return status;
        }
    }

    public void setEntityStatus(byte status) {
        if (packet != null) {
            writeByte(0, status);
        } else {
            this.status = status;
        }
    }

    @Override
    public Object asNMSPacket() throws Exception {
        Object packetInstance = packetConstructor.newInstance();
        WrappedPacketOutEntityStatus entityStatus = new WrappedPacketOutEntityStatus(new NMSPacket(packetInstance));
        entityStatus.setEntityId(getEntityId());
        entityStatus.setEntityStatus(getEntityStatus());
        return packetInstance;
    }
}
