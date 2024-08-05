package io.github.retrooper.packetevents.packetwrappers.play.out.entityeffect;

import io.github.retrooper.packetevents.packettype.PacketTypeClasses;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.api.SendableWrapper;
import io.github.retrooper.packetevents.packetwrappers.api.helper.WrappedPacketEntityAbstraction;
import lombok.ToString;
import org.bukkit.Effect;
import org.bukkit.entity.Entity;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

@ToString
public class WrappedPacketOutEntityEffect extends WrappedPacketEntityAbstraction implements SendableWrapper {

    private static Constructor<?> packetConstructor;
    private int effectID;
    private int amplifier;
    private int duration;
    private byte byteMask;
    private boolean byteMaskInitialized;

    private WrappedPacketOutEntityEffect(NMSPacket packet) {
        super(packet, 0);
    }

    private WrappedPacketOutEntityEffect(@NotNull Entity entity, int effectID, int amplifier, int duration) {
        super(0);
        entityID = entity.getEntityId();
        this.entity = entity;
        this.effectID = effectID;
        this.amplifier = amplifier;
        this.duration = duration;
        byteMaskInitialized = true;
    }

    private WrappedPacketOutEntityEffect(int entityID, int effectID, int amplifier, int duration, boolean hideParticles) {
        super(0);
        this.entityID = entityID;
        this.effectID = effectID;
        this.amplifier = amplifier;
        this.duration = duration;
        byteMaskInitialized = true;
        byteMask = hideParticles ? (byte) 1 : (byte) 0;
    }

    public WrappedPacketOutEntityEffect(Entity entity, @NotNull PotionEffectType effectType, int amplifier, int duration) {
        this(entity, effectType.getId(), amplifier, duration);
    }

    public WrappedPacketOutEntityEffect(int entityID, @NotNull PotionEffectType effectType, int amplifier, int duration, boolean hideParticles) {
        this(entityID, effectType.getId(), amplifier, duration, hideParticles);
    }

    public WrappedPacketOutEntityEffect(Entity entity, @NotNull Effect effect, int amplifier, int duration) {
        this(entity, effect.getId(), amplifier, duration);
    }

    public WrappedPacketOutEntityEffect(int entityID, @NotNull Effect effect, int amplifier, int duration, boolean hideParticles) {
        this(entityID, effect.getId(), amplifier, duration, hideParticles);
    }

    @Override
    protected void load() {
        try {
            packetConstructor = PacketTypeClasses.Play.Server.ENTITY_EFFECT.getConstructor();
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
        }
    }

    private int getEffectId() {
        if (nmsPacket != null) {
            return readByte(0);
        } else {
            return effectID;
        }
    }

    private void setEffectId(int effectID) {
        if (nmsPacket != null) {
            writeByte(0, (byte) effectID);
        } else {
            this.effectID = effectID;
        }
    }

    private int getAmplifier() {
        if (nmsPacket != null) {
            return readByte(1);
        } else {
            return amplifier;
        }
    }

    private void setAmplifier(int amplifier) {
        if (nmsPacket != null) {
            writeByte(1, (byte) amplifier);
        } else {
            this.amplifier = amplifier;
        }
    }

    private int getDuration() {
        if (nmsPacket != null) {
            return readInt(1);
        } else {
            return duration;
        }
    }

    private void setDuration(int duration) {
        if (nmsPacket != null) {
            this.duration = duration;
            writeInt(1, duration);
        } else {
            this.duration = duration;
        }
    }

    private @NotNull Optional<Byte> getByteMask() {
        if (nmsPacket != null && !byteMaskInitialized) {
            byteMask = readByte(2);
        }
        return Optional.of(byteMask);
    }

    private void setByteMask(byte byteMask) {
        this.byteMask = byteMask;

        if (nmsPacket != null) {
            writeByte(2, byteMask);
        }
    }

    private Optional<Boolean> shouldHideParticles() {
        Optional<Byte> byteMaskOptional = getByteMask();

        if (!byteMaskOptional.isPresent()) {
            return Optional.empty();
        }

        byte mask = byteMaskOptional.get();
        return Optional.of(mask == 1);
    }

    public void setShouldHideParticles(boolean hideParticles) {
        Optional<Byte> byteMaskOptional = getByteMask();

        if (byteMaskOptional.isPresent()) {
            byte mask = byteMaskOptional.get();

            if (hideParticles) {
                mask |= 2;
            } else {
                if (shouldHideParticles().isPresent()
                        && shouldHideParticles().get()) {
                    mask -= 2;
                }
            }

            setByteMask(mask);
        }
    }

    @Override
    public Object asNMSPacket() throws InvocationTargetException, InstantiationException, IllegalAccessException {
        Object packetInstance = packetConstructor.newInstance();
        WrappedPacketOutEntityEffect wrappedPacketOutEntityEffect = new WrappedPacketOutEntityEffect(new NMSPacket(packetInstance));
        wrappedPacketOutEntityEffect.setEntityId(getEntityId());
        wrappedPacketOutEntityEffect.setEffectId(getEffectId());
        wrappedPacketOutEntityEffect.setAmplifier(getAmplifier());
        wrappedPacketOutEntityEffect.setDuration(getDuration());

        Optional<Byte> optionalByteMask = getByteMask();
        optionalByteMask.ifPresent(wrappedPacketOutEntityEffect::setByteMask);
        return packetInstance;
    }
}
