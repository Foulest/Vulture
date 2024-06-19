package io.github.retrooper.packetevents.packetwrappers.play.out.namedentityspawn;

import io.github.retrooper.packetevents.packettype.PacketTypeClasses;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.api.SendableWrapper;
import io.github.retrooper.packetevents.packetwrappers.api.helper.WrappedPacketEntityAbstraction;
import io.github.retrooper.packetevents.utils.nms.NMSUtils;
import io.github.retrooper.packetevents.utils.reflection.Reflection;
import io.github.retrooper.packetevents.utils.vector.Vector3d;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.util.UUID;

public class WrappedPacketOutNamedEntitySpawn extends WrappedPacketEntityAbstraction implements SendableWrapper {

    private static final float ROTATION_DIVIDEND = 256.0F / 360.0F;
    private static boolean doublesPresent;
    private static boolean dataWatcherPresent;
    private static Constructor<?> packetConstructor;

    private UUID uuid;
    private Vector3d position;
    private float yaw;
    private float pitch;

    public WrappedPacketOutNamedEntitySpawn(NMSPacket packet) {
        super(packet);
    }

    public WrappedPacketOutNamedEntitySpawn(int entityID, UUID uuid, @NotNull Location location) {
        this.entityID = entityID;
        this.uuid = uuid;
        position = new Vector3d(location.getX(), location.getY(), location.getZ());
        yaw = location.getYaw();
        pitch = location.getPitch();
    }

    public WrappedPacketOutNamedEntitySpawn(@NotNull Entity entity, UUID uuid, @NotNull Location location) {
        entityID = entity.getEntityId();
        this.entity = entity;
        this.uuid = uuid;
        position = new Vector3d(location.getX(), location.getY(), location.getZ());
        yaw = location.getYaw();
        pitch = location.getPitch();
    }

    public WrappedPacketOutNamedEntitySpawn(@NotNull Entity entity, @NotNull Location location) {
        entityID = entity.getEntityId();
        this.entity = entity;
        uuid = entity.getUniqueId();
        position = new Vector3d(location.getX(), location.getY(), location.getZ());
        yaw = location.getYaw();
        pitch = location.getPitch();
    }

    public WrappedPacketOutNamedEntitySpawn(@NotNull Entity entity) {
        entityID = entity.getEntityId();
        this.entity = entity;
        uuid = entity.getUniqueId();
        Location location = entity.getLocation();
        position = new Vector3d(location.getX(), location.getY(), location.getZ());
        yaw = location.getYaw();
        pitch = location.getPitch();
    }

    public WrappedPacketOutNamedEntitySpawn(int entityID, UUID uuid, Vector3d position, float yaw, float pitch) {
        this.entityID = entityID;
        this.uuid = uuid;
        this.position = position;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public WrappedPacketOutNamedEntitySpawn(@NotNull Entity entity, UUID uuid, Vector3d position, float yaw, float pitch) {
        entityID = entity.getEntityId();
        this.entity = entity;
        this.uuid = uuid;
        this.position = position;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    @Override
    protected void load() {
        doublesPresent = Reflection.getField(PacketTypeClasses.Play.Server.NAMED_ENTITY_SPAWN, double.class, 1) != null;
        dataWatcherPresent = Reflection.getField(PacketTypeClasses.Play.Server.NAMED_ENTITY_SPAWN, NMSUtils.dataWatcherClass, 0) != null;

        try {
            packetConstructor = PacketTypeClasses.Play.Server.NAMED_ENTITY_SPAWN.getConstructor();
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
        }
    }

    public UUID getUUID() {
        if (packet != null) {
            return readObject(0, UUID.class);
        } else {
            return uuid;
        }
    }

    public void setUUID(UUID uuid) {
        if (packet != null) {
            write(UUID.class, 0, uuid);
        } else {
            this.uuid = uuid;
        }
    }

    public Vector3d getPosition() {
        if (packet != null) {
            double x;
            double y;
            double z;

            if (doublesPresent) {
                x = readDouble(0);
                y = readDouble(1);
                z = readDouble(2);
            } else {
                x = readInt(1) / 32.0D;
                y = readInt(2) / 32.0D;
                z = readInt(3) / 32.0D;
            }
            return new Vector3d(x, y, z);
        } else {
            return position;
        }
    }

    public void setPosition(Vector3d position) {
        if (packet != null) {
            if (doublesPresent) {
                writeDouble(0, position.x);
                writeDouble(1, position.y);
                writeDouble(2, position.z);
            } else {
                writeInt(1, (int) (position.x * 32.0D));
                writeInt(2, (int) (position.y * 32.0D));
                writeInt(3, (int) (position.z * 32.0D));
            }

            writeByte(0, (byte) (yaw * ROTATION_DIVIDEND));
            writeByte(1, (byte) (pitch * ROTATION_DIVIDEND));
        } else {
            this.position = position;
        }
    }

    public float getYaw() {
        if (packet != null) {
            return readByte(0) / ROTATION_DIVIDEND;
        } else {
            return yaw;
        }
    }

    public void setYaw(float yaw) {
        if (packet != null) {
            writeByte(0, (byte) (yaw * ROTATION_DIVIDEND));
        } else {
            this.yaw = yaw;
        }
    }

    public float getPitch() {
        if (packet != null) {
            return readByte(1) / ROTATION_DIVIDEND;
        } else {
            return pitch;
        }
    }

    public void setPitch(float pitch) {
        if (packet != null) {
            writeByte(1, (byte) (pitch * ROTATION_DIVIDEND));
        } else {
            this.pitch = pitch;
        }
    }

    @Override
    public Object asNMSPacket() throws Exception {
        Object packetInstance = packetConstructor.newInstance();
        WrappedPacketOutNamedEntitySpawn wrappedPacketOutNamedEntitySpawn = new WrappedPacketOutNamedEntitySpawn(new NMSPacket(packetInstance));
        wrappedPacketOutNamedEntitySpawn.setEntityId(getEntityId());
        wrappedPacketOutNamedEntitySpawn.setUUID(getUUID());
        wrappedPacketOutNamedEntitySpawn.setPosition(getPosition());
        wrappedPacketOutNamedEntitySpawn.setYaw(getYaw());
        wrappedPacketOutNamedEntitySpawn.setPitch(getPitch());

        if (dataWatcherPresent) {
            wrappedPacketOutNamedEntitySpawn.write(NMSUtils.dataWatcherClass, 0, NMSUtils.generateDataWatcher(null));
        }
        return packetInstance;
    }
}
