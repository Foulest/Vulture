package io.github.retrooper.packetevents.packetwrappers.play.out.animation;

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

public final class WrappedPacketOutAnimation extends WrappedPacketEntityAbstraction implements SendableWrapper {

    private static boolean v_1_17;
    private static Constructor<?> packetConstructor;
    private EntityAnimationType type;

    public WrappedPacketOutAnimation(NMSPacket packet) {
        super(packet, v_1_17 ? 6 : 0);
    }

    public WrappedPacketOutAnimation(@NotNull Entity target, EntityAnimationType type) {
        super(v_1_17 ? 6 : 0);
        this.entityID = target.getEntityId();
        this.entity = target;
        this.type = type;
    }

    public WrappedPacketOutAnimation(int entityID, EntityAnimationType type) {
        super(v_1_17 ? 6 : 0);
        this.entityID = entityID;
        this.entity = null;
        this.type = type;
    }

    @Override
    protected void load() {
        v_1_17 = version.isNewerThanOrEquals(ServerVersion.v_1_17);

        try {
            if (v_1_17) {
                packetConstructor = PacketTypeClasses.Play.Server.ANIMATION.getConstructor(NMSUtils.packetDataSerializerClass);
            } else {
                packetConstructor = PacketTypeClasses.Play.Server.ANIMATION.getConstructor();
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public EntityAnimationType getAnimationType() {
        if (packet != null) {
            byte id = (byte) readInt(v_1_17 ? 7 : 1);
            return EntityAnimationType.values()[id];
        } else {
            return type;
        }
    }

    public void setAnimationType(EntityAnimationType type) {
        if (packet != null) {
            writeInt(v_1_17 ? 7 : 1, type.ordinal());
        } else {
            this.type = type;
        }
    }

    @Override
    public @NotNull Object asNMSPacket() throws Exception {
        Object packetInstance;

        if (v_1_17) {
            Object packetDataSerializer = NMSUtils.generatePacketDataSerializer(PacketEvents.get()
                    .getByteBufUtil().newByteBuf(new byte[]{0, 0, 0, 0, 0, 0, 0, 0}));
            packetInstance = packetConstructor.newInstance(packetDataSerializer);
        } else {
            packetInstance = packetConstructor.newInstance();
        }

        WrappedPacketOutAnimation animation = new WrappedPacketOutAnimation(new NMSPacket(packetInstance));
        animation.setEntityId(getEntityId());
        animation.setAnimationType(getAnimationType());
        return packetInstance;
    }

    public enum EntityAnimationType {
        SWING_MAIN_ARM,
        TAKE_DAMAGE,
        LEAVE_BED,
        SWING_OFFHAND,
        CRITICAL_EFFECT,
        MAGIC_CRITICAL_EFFECT
    }
}
