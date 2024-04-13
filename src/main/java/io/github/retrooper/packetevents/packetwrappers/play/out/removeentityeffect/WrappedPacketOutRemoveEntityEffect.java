package io.github.retrooper.packetevents.packetwrappers.play.out.removeentityeffect;

import io.github.retrooper.packetevents.packettype.PacketTypeClasses;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.api.SendableWrapper;
import io.github.retrooper.packetevents.packetwrappers.api.helper.WrappedPacketEntityAbstraction;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;

public class WrappedPacketOutRemoveEntityEffect extends WrappedPacketEntityAbstraction implements SendableWrapper {

    private static Constructor<?> packetConstructor;
    private int effectID;

    public WrappedPacketOutRemoveEntityEffect(NMSPacket packet) {
        super(packet);
    }

    public WrappedPacketOutRemoveEntityEffect(@NotNull Entity entity, int effectID) {
        this.entityID = entity.getEntityId();
        this.entity = entity;
        this.effectID = effectID;
    }

    public WrappedPacketOutRemoveEntityEffect(int entityID, int effectID) {
        this.entityID = entityID;
        this.effectID = effectID;
    }

    @Override
    protected void load() {
        try {
            packetConstructor = PacketTypeClasses.Play.Server.REMOVE_ENTITY_EFFECT.getConstructor();
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
        }
    }

    public int getEffectId() {
        if (packet != null) {
            return readInt(1);
        } else {
            return effectID;
        }
    }

    public void setEffectId(int effectID) {
        if (packet != null) {
            writeInt(1, effectID);
        } else {
            this.effectID = effectID;
        }
    }

    @Override
    public Object asNMSPacket() throws Exception {
        Object packetInstance = packetConstructor.newInstance();
        WrappedPacketOutRemoveEntityEffect wrappedPacketOutRemoveEntityEffect = new WrappedPacketOutRemoveEntityEffect(new NMSPacket(packetInstance));
        wrappedPacketOutRemoveEntityEffect.setEntityId(getEntityId());
        wrappedPacketOutRemoveEntityEffect.setEffectId(getEffectId());
        return packetInstance;
    }
}
