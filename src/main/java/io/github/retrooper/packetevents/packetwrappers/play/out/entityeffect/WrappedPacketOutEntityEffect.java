package io.github.retrooper.packetevents.packetwrappers.play.out.entityeffect;

import io.github.retrooper.packetevents.packettype.PacketTypeClasses;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.api.SendableWrapper;
import io.github.retrooper.packetevents.packetwrappers.api.helper.WrappedPacketEntityAbstraction;
import org.bukkit.Effect;
import org.bukkit.entity.Entity;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.util.Optional;

public class WrappedPacketOutEntityEffect extends WrappedPacketEntityAbstraction implements SendableWrapper {

    private static Constructor<?> packetConstructor;
    private int effectID;
    private int amplifier;
    private int duration;
    private byte byteMask;
    private boolean byteMaskInitialized = false;

    public WrappedPacketOutEntityEffect(NMSPacket packet) {
        super(packet, 0);
    }

    public WrappedPacketOutEntityEffect(@NotNull Entity entity, int effectID, int amplifier, int duration) {
        super(0);
        this.entityID = entity.getEntityId();
        this.entity = entity;
        this.effectID = effectID;
        this.amplifier = amplifier;
        this.duration = duration;
        this.byteMaskInitialized = true;
    }

    public WrappedPacketOutEntityEffect(int entityID, int effectID, int amplifier, int duration, boolean hideParticles) {
        super(0);
        this.entityID = entityID;
        this.effectID = effectID;
        this.amplifier = amplifier;
        this.duration = duration;
        this.byteMaskInitialized = true;
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

    public int getEffectId() {
        if (packet != null) {
            return readByte(0);
        } else {
            return effectID;
        }
    }

    public void setEffectId(int effectID) {
        if (packet != null) {
            writeByte(0, (byte) effectID);
        } else {
            this.effectID = effectID;
        }
    }

    public int getAmplifier() {
        if (packet != null) {
            return readByte(1);
        } else {
            return amplifier;
        }
    }

    public void setAmplifier(int amplifier) {
        if (packet != null) {
            writeByte(1, (byte) amplifier);
        } else {
            this.amplifier = amplifier;
        }
    }

    public int getDuration() {
        if (packet != null) {
            return readInt(1);
        } else {
            return duration;
        }
    }

    public void setDuration(int duration) {
        if (packet != null) {
            this.duration = duration;
            writeInt(1, duration);
        } else {
            this.duration = duration;
        }
    }

    private @NotNull Optional<Byte> getByteMask() {
        if (packet != null && !byteMaskInitialized) {
            return Optional.of(byteMask = readByte(2));
        } else {
            return Optional.of(byteMask);
        }
    }

    private void setByteMask(byte byteMask) {
        this.byteMask = byteMask;

        if (packet != null) {
            writeByte(2, byteMask);
        }
    }

    public Optional<Boolean> shouldHideParticles() {
        Optional<Byte> byteMaskOptional = getByteMask();

        if (!byteMaskOptional.isPresent()) {
            return Optional.empty();
        }

        byte byteMask = byteMaskOptional.get();
        return Optional.of(byteMask == 1);
    }

    public void setShouldHideParticles(boolean hideParticles) {
        Optional<Byte> byteMaskOptional = getByteMask();

        if (byteMaskOptional.isPresent()) {
            byte byteMask = byteMaskOptional.get();
            boolean currentHideParticles = shouldHideParticles().get();

            if (hideParticles) {
                byteMask |= 2;
            } else {
                if (currentHideParticles) {
                    byteMask -= 2;
                }
            }

            setByteMask(byteMask);
        }
    }

    @Override
    public Object asNMSPacket() throws Exception {
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
