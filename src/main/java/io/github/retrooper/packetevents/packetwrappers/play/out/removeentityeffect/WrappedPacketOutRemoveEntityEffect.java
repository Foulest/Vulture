package io.github.retrooper.packetevents.packetwrappers.play.out.removeentityeffect;

import io.github.retrooper.packetevents.packettype.PacketTypeClasses;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.api.SendableWrapper;
import io.github.retrooper.packetevents.packetwrappers.api.helper.WrappedPacketEntityAbstraction;
import io.github.retrooper.packetevents.utils.nms.NMSUtils;
import io.github.retrooper.packetevents.utils.server.ServerVersion;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;

public class WrappedPacketOutRemoveEntityEffect extends WrappedPacketEntityAbstraction implements SendableWrapper {

    private static boolean v_1_8_x;
    private static boolean v_1_17;
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
        v_1_8_x = version.isOlderThan(ServerVersion.v_1_9);
        v_1_17 = version.isNewerThanOrEquals(ServerVersion.v_1_17);

        try {
            if (v_1_17) {
                packetConstructor = PacketTypeClasses.Play.Server.REMOVE_ENTITY_EFFECT.getConstructor(int.class, NMSUtils.mobEffectListClass);
            } else {
                packetConstructor = PacketTypeClasses.Play.Server.REMOVE_ENTITY_EFFECT.getConstructor();
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public int getEffectId() {
        if (packet != null) {
            if (v_1_8_x) {
                // 1.7 and 1.8
                return readInt(1);
            } else {
                // 1.9+
                Object nmsMobEffectList = readObject(0, NMSUtils.mobEffectListClass);
                return NMSUtils.getEffectId(nmsMobEffectList);
            }
        } else {
            return effectID;
        }
    }

    public void setEffectId(int effectID) {
        if (packet != null) {
            if (v_1_8_x) {
                // 1.7 and 1.8
                writeInt(1, effectID);
            } else {
                // 1.9+
                Object nmsMobEffectList = NMSUtils.getMobEffectListById(effectID);
                write(NMSUtils.mobEffectListClass, 0, nmsMobEffectList);
            }
        } else {
            this.effectID = effectID;
        }
    }

    @Override
    public Object asNMSPacket() throws Exception {
        Object packetInstance;

        if (v_1_17) {
            Object nmsMobEffectList = NMSUtils.getMobEffectListById(getEffectId());
            packetInstance = packetConstructor.newInstance(getEntityId(), nmsMobEffectList);
        } else {
            packetInstance = packetConstructor.newInstance();
            WrappedPacketOutRemoveEntityEffect wrappedPacketOutRemoveEntityEffect =
                    new WrappedPacketOutRemoveEntityEffect(new NMSPacket(packetInstance));
            wrappedPacketOutRemoveEntityEffect.setEntityId(getEntityId());
            wrappedPacketOutRemoveEntityEffect.setEffectId(getEffectId());
        }
        return packetInstance;
    }
}
