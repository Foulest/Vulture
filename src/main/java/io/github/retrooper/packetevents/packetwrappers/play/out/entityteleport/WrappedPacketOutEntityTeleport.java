package io.github.retrooper.packetevents.packetwrappers.play.out.entityteleport;

import io.github.retrooper.packetevents.packettype.PacketTypeClasses;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.api.SendableWrapper;
import io.github.retrooper.packetevents.packetwrappers.api.helper.WrappedPacketEntityAbstraction;
import io.github.retrooper.packetevents.utils.vector.Vector3d;
import net.foulest.vulture.util.MathUtil;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;

public class WrappedPacketOutEntityTeleport extends WrappedPacketEntityAbstraction implements SendableWrapper {

    private static final float ROTATION_MULTIPLIER = 256.0F / 360.0F;
    private static Constructor<?> constructor;

    private Vector3d position;
    private float yaw;
    private float pitch;
    private boolean onGround;

    public WrappedPacketOutEntityTeleport(NMSPacket packet) {
        super(packet);
    }

    public WrappedPacketOutEntityTeleport(int entityID, @NotNull Location loc, boolean onGround) {
        this(entityID, loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch(), onGround);
    }

    public WrappedPacketOutEntityTeleport(Entity entity, @NotNull Location loc, boolean onGround) {
        this(entity, loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch(), onGround);
    }

    public WrappedPacketOutEntityTeleport(int entityID, Vector3d position, float yaw, float pitch, boolean onGround) {
        this.entityID = entityID;
        this.position = position;
        this.yaw = yaw;
        this.pitch = pitch;
        this.onGround = onGround;
    }

    public WrappedPacketOutEntityTeleport(@NotNull Entity entity, Vector3d position,
                                          float yaw, float pitch, boolean onGround) {
        entityID = entity.getEntityId();
        this.entity = entity;
        this.position = position;
        this.yaw = yaw;
        this.pitch = pitch;
        this.onGround = onGround;
    }

    public WrappedPacketOutEntityTeleport(int entityID, double x, double y, double z,
                                          float yaw, float pitch, boolean onGround) {
        this.entityID = entityID;
        position = new Vector3d(x, y, z);
        this.yaw = yaw;
        this.pitch = pitch;
        this.onGround = onGround;
    }

    public WrappedPacketOutEntityTeleport(@NotNull Entity entity, double x, double y, double z,
                                          float yaw, float pitch, boolean onGround) {
        entityID = entity.getEntityId();
        this.entity = entity;
        position = new Vector3d(x, y, z);
        this.yaw = yaw;
        this.pitch = pitch;
        this.onGround = onGround;
    }

    private static int floor(double value) {
        return MathUtil.floorDouble(value);
    }

    @Override
    protected void load() {
        Class<?> packetClass = PacketTypeClasses.Play.Server.ENTITY_TELEPORT;

        try {
            constructor = packetClass.getConstructor(int.class, int.class, int.class, int.class, byte.class, byte.class, boolean.class);
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
        }
    }


    public Vector3d getPosition() {
        if (packet != null) {
            double x = readInt(1) / 32.0D;
            double y = readInt(2) / 32.0D;
            double z = readInt(3) / 32.0D;
            return new Vector3d(x, y, z);
        } else {
            return position;
        }
    }

    public void setPosition(Vector3d position) {
        if (packet != null) {
            writeInt(1, floor(position.x * 32.0D));
            writeInt(2, floor(position.y * 32.0D));
            writeInt(3, floor(position.z * 32.0D));
        } else {
            this.position = position;
        }
    }

    public float getYaw() {
        if (packet != null) {
            return (readByte(0) / ROTATION_MULTIPLIER);
        } else {
            return yaw;
        }
    }

    public void setYaw(float yaw) {
        if (packet != null) {
            writeByte(0, (byte) (yaw * ROTATION_MULTIPLIER));
        } else {
            this.yaw = yaw;
        }
    }

    public float getPitch() {
        if (packet != null) {
            return (readByte(1) / ROTATION_MULTIPLIER);
        } else {
            return pitch;
        }
    }

    public void setPitch(float pitch) {
        if (packet != null) {
            writeByte(1, (byte) (pitch * ROTATION_MULTIPLIER));
        } else {
            this.pitch = pitch;
        }
    }

    public boolean isOnGround() {
        if (packet != null) {
            return readBoolean(0);
        } else {
            return onGround;
        }
    }

    public void setOnGround(boolean onGround) {
        if (packet != null) {
            writeBoolean(0, onGround);
        } else {
            this.onGround = onGround;
        }
    }

    @Override
    public Object asNMSPacket() throws Exception {
        Vector3d pos = getPosition();
        return constructor.newInstance(entityID, floor(pos.x * 32.0D), floor(pos.y * 32.0D), floor(pos.z * 32.0D),
                (byte) ((int) getYaw() * ROTATION_MULTIPLIER), (byte) (int) (getPitch() * ROTATION_MULTIPLIER), false);
    }
}
