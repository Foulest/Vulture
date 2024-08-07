package io.github.retrooper.packetevents.packetwrappers.play.out.position;

import io.github.retrooper.packetevents.packettype.PacketTypeClasses;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;
import io.github.retrooper.packetevents.packetwrappers.api.SendableWrapper;
import io.github.retrooper.packetevents.utils.enums.EnumUtil;
import io.github.retrooper.packetevents.utils.reflection.SubclassUtil;
import io.github.retrooper.packetevents.utils.vector.Vector3d;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

@ToString
public final class WrappedPacketOutPosition extends WrappedPacket implements SendableWrapper {

    private static Constructor<?> packetConstructor;
    private static Class<? extends Enum<?>> enumPlayerTeleportFlagsClass;

    private Vector3d position;
    private float yaw;
    private float pitch;
    private Set<PlayerTeleportFlags> relativeFlags;

    public WrappedPacketOutPosition(NMSPacket packet) {
        super(packet);
        relativeFlags = EnumSet.noneOf(PlayerTeleportFlags.class);
    }

    public WrappedPacketOutPosition(double x, double y, double z, float yaw, float pitch,
                                    Set<PlayerTeleportFlags> relativeFlags) {
        position = new Vector3d(x, y, z);
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

        for (PlayerTeleportFlags flag : flags) {
            relativeMask |= flag.maskFlag;
        }
        return relativeMask;
    }

    public void setRelativeFlagsMask(byte mask) {
        if (nmsPacket != null) {
            Collection<Enum<?>> nmsRelativeFlags = new HashSet<>();

            for (PlayerTeleportFlags flag : PlayerTeleportFlags.values()) {
                if ((mask & flag.maskFlag) == flag.maskFlag) {
                    nmsRelativeFlags.add(EnumUtil.valueOf(enumPlayerTeleportFlagsClass.asSubclass(Enum.class), flag.name()));
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

    @SuppressWarnings("unchecked")
    private Set<PlayerTeleportFlags> getRelativeFlags() {
        if (nmsPacket != null) {
            Set<PlayerTeleportFlags> teleportFlags = EnumSet.noneOf(PlayerTeleportFlags.class);
            Set<Enum<?>> set = readObject(0, Set.class);

            for (Enum<?> e : set) {
                teleportFlags.add(PlayerTeleportFlags.valueOf(e.name()));
            }
            return teleportFlags;
        } else {
            return relativeFlags;
        }
    }

    public void setRelativeFlags(Set<PlayerTeleportFlags> flags) {
        if (nmsPacket != null) {
            Collection<Enum<?>> nmsRelativeFlags = new HashSet<>();

            for (PlayerTeleportFlags flag : flags) {
                nmsRelativeFlags.add(EnumUtil.valueOf(enumPlayerTeleportFlagsClass.asSubclass(Enum.class), flag.name()));
            }

            write(Set.class, 0, nmsRelativeFlags);
        } else {
            relativeFlags = flags;
        }
    }

    public Vector3d getPosition() {
        if (nmsPacket != null) {
            double x = readDouble(0);
            double y = readDouble(1);
            double z = readDouble(2);
            return new Vector3d(x, y, z);
        } else {
            return position;
        }
    }

    public void setPosition(Vector3d position) {
        if (nmsPacket != null) {
            writeDouble(0, position.x);
            writeDouble(1, position.y);
            writeDouble(2, position.z);
        } else {
            this.position = position;
        }
    }

    public float getYaw() {
        if (nmsPacket != null) {
            return readFloat(0);
        } else {
            return yaw;
        }
    }

    public void setYaw(float yaw) {
        if (nmsPacket != null) {
            writeFloat(0, yaw);
        } else {
            this.yaw = yaw;
        }
    }

    public float getPitch() {
        if (nmsPacket != null) {
            return readFloat(1);
        } else {
            return pitch;
        }
    }

    public void setPitch(float pitch) {
        if (nmsPacket != null) {
            writeFloat(1, pitch);
        } else {
            this.pitch = pitch;
        }
    }

    @Override
    public @NotNull Object asNMSPacket() throws InvocationTargetException, InstantiationException, IllegalAccessException {
        Set<Object> nmsRelativeFlags = new HashSet<>();

        for (PlayerTeleportFlags flag : getRelativeFlags()) {
            nmsRelativeFlags.add(EnumUtil.valueOf(enumPlayerTeleportFlagsClass.asSubclass(Enum.class), flag.name()));
        }

        Vector3d pos = getPosition();
        return packetConstructor.newInstance(pos.x, pos.y, pos.z, getYaw(), getPitch(), nmsRelativeFlags);
    }

    @ToString
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
