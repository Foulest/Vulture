package io.github.retrooper.packetevents.packetwrappers.play.out.position;

import io.github.retrooper.packetevents.packettype.PacketTypeClasses;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;
import io.github.retrooper.packetevents.packetwrappers.api.SendableWrapper;
import io.github.retrooper.packetevents.utils.enums.EnumUtil;
import io.github.retrooper.packetevents.utils.reflection.SubclassUtil;
import io.github.retrooper.packetevents.utils.vector.Vector3d;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.util.HashSet;
import java.util.Set;

public final class WrappedPacketOutPosition extends WrappedPacket implements SendableWrapper {

    private static Constructor<?> packetConstructor;
    private static Class<? extends Enum<?>> enumPlayerTeleportFlagsClass;

    private Vector3d position;
    private float yaw;
    private float pitch;
    private Set<PlayerTeleportFlags> relativeFlags;

    public WrappedPacketOutPosition(NMSPacket packet) {
        super(packet);
        relativeFlags = new HashSet<>();
    }

    public WrappedPacketOutPosition(double x, double y, double z, float yaw, float pitch,
                                    Set<PlayerTeleportFlags> relativeFlags) {
        this.position = new Vector3d(x, y, z);
        this.yaw = yaw;
        this.pitch = pitch;
        this.relativeFlags = relativeFlags;
    }

    public WrappedPacketOutPosition(Vector3d position, float yaw, float pitch,
                                    Set<PlayerTeleportFlags> relativeFlags) {
        this.position = position;
        this.yaw = yaw;
        this.pitch = pitch;
        this.relativeFlags = relativeFlags;
    }

    @Override
    protected void load() {
        enumPlayerTeleportFlagsClass = SubclassUtil.getEnumSubClass(PacketTypeClasses.Play.Server.POSITION, "EnumPlayerTeleportFlags");

        try {
            packetConstructor = PacketTypeClasses.Play.Server.POSITION.getConstructor(double.class, double.class, double.class, float.class, float.class, Set.class);
        } catch (NoSuchMethodException ex) {
            throw new IllegalStateException("Failed to locate a supported constructor of the PacketPlayOutPosition packet class.");
        }
    }

    public byte getRelativeFlagsMask() {
        byte relativeMask = 0;
        Set<PlayerTeleportFlags> flags = getRelativeFlags();

        if (packet != null) {
            for (PlayerTeleportFlags flag : flags) {
                relativeMask |= flag.maskFlag;
            }
        } else {
            for (PlayerTeleportFlags flag : flags) {
                relativeMask |= flag.maskFlag;
            }
        }
        return relativeMask;
    }

    public void setRelativeFlagsMask(byte mask) {
        if (packet != null) {
            Set<Enum<?>> nmsRelativeFlags = new HashSet<>();

            for (PlayerTeleportFlags flag : PlayerTeleportFlags.values()) {
                if ((mask & flag.maskFlag) == flag.maskFlag) {
                    nmsRelativeFlags.add(EnumUtil.valueOf(enumPlayerTeleportFlagsClass, flag.name()));
                }
            }

            write(Set.class, 0, nmsRelativeFlags);
        } else {
            relativeFlags.clear();

            for (PlayerTeleportFlags flag : PlayerTeleportFlags.values()) {
                if ((mask & flag.maskFlag) == flag.maskFlag) {
                    relativeFlags.add(flag);
                }
            }
        }
    }

    public Set<PlayerTeleportFlags> getRelativeFlags() {
        if (packet != null) {
            Set<PlayerTeleportFlags> relativeFlags = new HashSet<>();
            Set<Enum<?>> set = readObject(0, Set.class);

            for (Enum<?> e : set) {
                relativeFlags.add(PlayerTeleportFlags.valueOf(e.name()));
            }
            return relativeFlags;
        } else {
            return relativeFlags;
        }
    }

    public void setRelativeFlags(Set<PlayerTeleportFlags> flags) {
        if (packet != null) {
            Set<Enum<?>> nmsRelativeFlags = new HashSet<>();

            for (PlayerTeleportFlags flag : flags) {
                nmsRelativeFlags.add(EnumUtil.valueOf(enumPlayerTeleportFlagsClass, flag.name()));
            }

            write(Set.class, 0, nmsRelativeFlags);
        } else {
            this.relativeFlags = flags;
        }
    }

    public Vector3d getPosition() {
        if (packet != null) {
            double x = readDouble(0);
            double y = readDouble(1);
            double z = readDouble(2);
            return new Vector3d(x, y, z);
        } else {
            return position;
        }
    }

    public void setPosition(Vector3d position) {
        if (packet != null) {
            writeDouble(0, position.x);
            writeDouble(1, position.y);
            writeDouble(2, position.z);
        } else {
            this.position = position;
        }
    }

    public float getYaw() {
        if (packet != null) {
            return readFloat(0);
        } else {
            return yaw;
        }
    }

    public void setYaw(float yaw) {
        if (packet != null) {
            writeFloat(0, yaw);
        } else {
            this.yaw = yaw;
        }
    }

    public float getPitch() {
        if (packet != null) {
            return readFloat(1);
        } else {
            return pitch;
        }
    }

    public void setPitch(float pitch) {
        if (packet != null) {
            writeFloat(1, pitch);
        } else {
            this.pitch = pitch;
        }
    }

    @Override
    public @NotNull Object asNMSPacket() throws Exception {
        Set<Object> nmsRelativeFlags = new HashSet<>();

        for (PlayerTeleportFlags flag : getRelativeFlags()) {
            nmsRelativeFlags.add(EnumUtil.valueOf(enumPlayerTeleportFlagsClass, flag.name()));
        }

        Vector3d position = getPosition();
        return packetConstructor.newInstance(position.x, position.y, position.z, getYaw(), getPitch(), nmsRelativeFlags);
    }

    public enum PlayerTeleportFlags {
        X(0x01),
        Y(0x02),
        Z(0x04),
        Y_ROT(0x08),
        X_ROT(0x10);

        final byte maskFlag;

        PlayerTeleportFlags(int maskFlag) {
            this.maskFlag = (byte) maskFlag;
        }
    }
}
