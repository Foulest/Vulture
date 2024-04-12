package io.github.retrooper.packetevents.packetwrappers.play.out.entitystatus;

import io.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.packettype.PacketTypeClasses;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.api.SendableWrapper;
import io.github.retrooper.packetevents.packetwrappers.api.helper.WrappedPacketEntityAbstraction;
import io.github.retrooper.packetevents.utils.nms.NMSUtils;
import io.github.retrooper.packetevents.utils.server.ServerVersion;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;

public class WrappedPacketOutEntityStatus extends WrappedPacketEntityAbstraction implements SendableWrapper {

    private static boolean v_1_17;
    private static Constructor<?> packetConstructor;
    private byte status;

    public WrappedPacketOutEntityStatus(NMSPacket packet) {
        super(packet);
    }

    public WrappedPacketOutEntityStatus(@NotNull Entity entity, byte status) {
        this.entityID = entity.getEntityId();
        this.entity = entity;
        this.status = status;
    }

    public WrappedPacketOutEntityStatus(int entityID, byte status) {
        this.entityID = entityID;
        this.status = status;
    }

    @Override
    protected void load() {
        v_1_17 = version.isNewerThanOrEquals(ServerVersion.v_1_17);

        try {
            if (v_1_17) {
                packetConstructor = PacketTypeClasses.Play.Server.ENTITY_STATUS.getConstructor(NMSUtils.packetDataSerializerClass);
            } else {
                packetConstructor = PacketTypeClasses.Play.Server.ENTITY_STATUS.getConstructor();
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
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
        Object packetInstance;

        if (v_1_17) {
            Object packetDataSerializer = NMSUtils.generatePacketDataSerializer(PacketEvents.get().getByteBufUtil().newByteBuf(new byte[]{0, 0, 0, 0, 0}));
            packetInstance = packetConstructor.newInstance(packetDataSerializer);
        } else {
            packetInstance = packetConstructor.newInstance();
        }

        WrappedPacketOutEntityStatus entityStatus = new WrappedPacketOutEntityStatus(new NMSPacket(packetInstance));
        entityStatus.setEntityId(getEntityId());
        entityStatus.setEntityStatus(getEntityStatus());
        return packetInstance;
    }
}
