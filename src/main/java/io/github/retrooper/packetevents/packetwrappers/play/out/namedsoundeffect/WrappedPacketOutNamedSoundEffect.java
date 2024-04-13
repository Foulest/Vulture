package io.github.retrooper.packetevents.packetwrappers.play.out.namedsoundeffect;

import io.github.retrooper.packetevents.packettype.PacketTypeClasses;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;
import io.github.retrooper.packetevents.packetwrappers.api.SendableWrapper;
import io.github.retrooper.packetevents.utils.nms.NMSUtils;
import io.github.retrooper.packetevents.utils.vector.Vector3d;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class WrappedPacketOutNamedSoundEffect extends WrappedPacket implements SendableWrapper {

    private static Constructor<?> packetConstructor;
    private static Constructor<?> soundEffectConstructor;
    private static boolean soundEffectVarExists;
    private static float pitchMultiplier = 63.0F;

    private String soundEffectName;
    private Vector3d effectPosition;
    private float volume;
    private float pitch;

    public WrappedPacketOutNamedSoundEffect(NMSPacket packet) {
        super(packet);
    }

    public WrappedPacketOutNamedSoundEffect(String soundEffectName, Vector3d effectPosition,
                                            float volume, float pitch) {
        this.soundEffectName = soundEffectName;
        this.effectPosition = effectPosition;
        this.volume = volume;
        this.pitch = pitch;
    }

    public WrappedPacketOutNamedSoundEffect(String soundEffectName, double effectPositionX, double effectPositionY,
                                            double effectPositionZ, float volume, float pitch) {
        this.soundEffectName = soundEffectName;
        this.effectPosition = new Vector3d(effectPositionX, effectPositionY, effectPositionZ);
        this.volume = volume;
        this.pitch = pitch;
    }

    @Override
    protected void load() {
        soundEffectVarExists = NMSUtils.soundEffectClass != null;

        if (soundEffectVarExists) {
            try {
                soundEffectConstructor = NMSUtils.soundEffectClass.getConstructor(NMSUtils.minecraftKeyClass);
            } catch (NoSuchMethodException ex) {
                ex.printStackTrace();
            }
        }

        pitchMultiplier = 63.0F;

        try {
            packetConstructor = PacketTypeClasses.Play.Server.NAMED_SOUND_EFFECT.getConstructor();
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
        }
    }

    public String getSoundEffectName() {
        if (packet != null) {
            if (soundEffectVarExists) {
                Object soundEffect = readObject(0, NMSUtils.soundEffectClass);
                WrappedPacket soundEffectWrapper = new WrappedPacket(new NMSPacket(soundEffect));
                return soundEffectWrapper.readMinecraftKey(0);
            } else {
                return readString(0);
            }
        } else {
            return soundEffectName;
        }
    }

    public void setSoundEffectName(String name) {
        if (packet != null) {
            if (soundEffectVarExists) {
                Object minecraftKey = NMSUtils.generateMinecraftKeyNew(name);
                Object soundEffect = null;

                try {
                    soundEffect = soundEffectConstructor.newInstance(minecraftKey);
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException ex) {
                    ex.printStackTrace();
                }

                write(NMSUtils.soundEffectClass, 0, soundEffect);
            } else {
                writeString(0, name);
            }
        } else {
            soundEffectName = name;
        }
    }

    public Vector3d getEffectPosition() {
        if (packet != null) {
            double x = readInt(0) / 8.0D;
            double y = readInt(1) / 8.0D;
            double z = readInt(2) / 8.0D;
            return new Vector3d(x, y, z);
        } else {
            return effectPosition;
        }
    }

    public void setEffectPosition(Vector3d effectPosition) {
        if (packet != null) {
            writeInt(0, (int) (effectPosition.x / 8.0D));
            writeInt(1, (int) (effectPosition.y / 8.0D));
            writeInt(2, (int) (effectPosition.z / 8.0D));
        } else {
            this.effectPosition = effectPosition;
        }
    }

    // Might be more than 1.0 on some older versions. 1 is 100%
    public float getVolume() {
        if (packet != null) {
            return readFloat(0);
        } else {
            return volume;
        }
    }

    public void setVolume(float volume) {
        if (packet != null) {
            writeFloat(0, volume);
        } else {
            this.volume = volume;
        }
    }

    public float getPitch() {
        if (packet != null) {
            return readInt(3) / pitchMultiplier;
        } else {
            return pitch;
        }
    }

    public void setPitch(float pitch) {
        if (packet != null) {
            writeInt(1, (int) (pitch * pitchMultiplier));
        } else {
            this.pitch = pitch;
        }
    }

    @Override
    public Object asNMSPacket() throws Exception {
        Object packetInstance = packetConstructor.newInstance();
        WrappedPacketOutNamedSoundEffect wrappedPacketOutNamedSoundEffect = new WrappedPacketOutNamedSoundEffect(new NMSPacket(packetInstance));
        wrappedPacketOutNamedSoundEffect.setSoundEffectName(getSoundEffectName());
        wrappedPacketOutNamedSoundEffect.setEffectPosition(getEffectPosition());
        wrappedPacketOutNamedSoundEffect.setPitch(getPitch());
        wrappedPacketOutNamedSoundEffect.setVolume(getVolume());
        return packetInstance;
    }
}
