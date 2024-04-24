package io.github.retrooper.packetevents.packetwrappers.play.out.animation;

import io.github.retrooper.packetevents.packettype.PacketTypeClasses;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.api.SendableWrapper;
import io.github.retrooper.packetevents.packetwrappers.api.helper.WrappedPacketEntityAbstraction;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;

public final class WrappedPacketOutAnimation extends WrappedPacketEntityAbstraction implements SendableWrapper {

    private static Constructor<?> packetConstructor;
    private EntityAnimationType type;

    public WrappedPacketOutAnimation(NMSPacket packet) {
        super(packet, 0);
    }

    public WrappedPacketOutAnimation(@NotNull Entity target, EntityAnimationType type) {
        super(0);
        entityID = target.getEntityId();
        entity = target;
        this.type = type;
    }

    public WrappedPacketOutAnimation(int entityID, EntityAnimationType type) {
        super(0);
        this.entityID = entityID;
        entity = null;
        this.type = type;
    }

    @Override
    protected void load() {
        try {
            packetConstructor = PacketTypeClasses.Play.Server.ANIMATION.getConstructor();
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
        }
    }

    public EntityAnimationType getAnimationType() {
        if (packet != null) {
            byte id = (byte) readInt(1);
            return EntityAnimationType.values()[id];
        } else {
            return type;
        }
    }

    public void setAnimationType(EntityAnimationType type) {
        if (packet != null) {
            writeInt(1, type.ordinal());
        } else {
            this.type = type;
        }
    }

    @Override
    public @NotNull Object asNMSPacket() throws Exception {
        Object packetInstance = packetConstructor.newInstance();
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
