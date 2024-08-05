package io.github.retrooper.packetevents.packetwrappers.play.out.spawnentityliving;

import io.github.retrooper.packetevents.packettype.PacketTypeClasses;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.api.SendableWrapper;
import io.github.retrooper.packetevents.packetwrappers.api.helper.WrappedPacketEntityAbstraction;
import io.github.retrooper.packetevents.utils.vector.Vector3d;
import lombok.ToString;
import net.foulest.vulture.util.MathUtil;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

@ToString
public class WrappedPacketOutSpawnEntityLiving extends WrappedPacketEntityAbstraction implements SendableWrapper {

    private static final float ROTATION_FACTOR = 256.0F / 360.0F;
    private static final double VELOCITY_FACTOR = 8000.0;
    private static Constructor<?> packetConstructor;
    private Vector3d position;
    private Vector3d velocity;
    private EntityType entityType;
    private float yaw;
    private float pitch;
    private float headPitch;

    public WrappedPacketOutSpawnEntityLiving(NMSPacket packet) {
        super(packet);
    }

    public WrappedPacketOutSpawnEntityLiving(Entity entity, EntityType entityType, Vector3d position,
                                             Vector3d velocity, float yaw, float pitch, float headPitch) {
        setEntity(entity);
        this.entityType = entityType;
        this.position = position;
        this.velocity = velocity;
        this.yaw = yaw;
        this.pitch = pitch;
        this.headPitch = headPitch;
    }

    public WrappedPacketOutSpawnEntityLiving(int entityID, EntityType entityType, Vector3d position,
                                             Vector3d velocity, float yaw, float pitch, float headPitch) {
        setEntityId(entityID);
        this.entityType = entityType;
        this.position = position;
        this.velocity = velocity;
        this.yaw = yaw;
        this.pitch = pitch;
        this.headPitch = headPitch;
    }

    @Override
    protected void load() {
        try {
            packetConstructor = PacketTypeClasses.Play.Server.SPAWN_ENTITY_LIVING.getConstructor();
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
        }
    }

    private EntityType getEntityType() {
        if (nmsPacket != null) {
            int entityTypeID = readInt(1);
            return EntityType.fromId(entityTypeID);
        } else {
            return entityType;
        }
    }

    private void setEntityType(EntityType entityType) {
        if (nmsPacket != null) {
            writeInt(1, entityType.getTypeId());
        } else {
            this.entityType = entityType;
        }
    }

    public Vector3d getPosition() {
        if (nmsPacket != null) {
            double x = readInt(2) / 32.0;
            double y = readInt(3) / 32.0;
            double z = readInt(4) / 32.0;
            return new Vector3d(x, y, z);
        } else {
            return position;
        }
    }

    private void setPosition(Vector3d position) {
        if (nmsPacket != null) {
            writeInt(2, MathUtil.floorDouble(position.x * 32.0));
            writeInt(3, MathUtil.floorDouble(position.y * 32.0));
            writeInt(4, MathUtil.floorDouble(position.z * 32.0));
        } else {
            this.position = position;
        }
    }

    private Vector3d getVelocity() {
        if (nmsPacket != null) {
            int factoredVelX = readInt(5);
            int factoredVelY = readInt(6);
            int factoredVelZ = readInt(7);
            return new Vector3d(factoredVelX / VELOCITY_FACTOR,
                    factoredVelY / VELOCITY_FACTOR,
                    factoredVelZ / VELOCITY_FACTOR);
        } else {
            return velocity;
        }
    }

    private void setVelocity(Vector3d velocity) {
        if (nmsPacket != null) {
            int factoredVelX = (int) (velocity.x * VELOCITY_FACTOR);
            int factoredVelY = (int) (velocity.y * VELOCITY_FACTOR);
            int factoredVelZ = (int) (velocity.z * VELOCITY_FACTOR);

            writeInt(5, factoredVelX);
            writeInt(6, factoredVelY);
            writeInt(7, factoredVelZ);
        } else {
            this.velocity = velocity;
        }
    }

    private float getYaw() {
        if (nmsPacket != null) {
            byte factoredYaw = readByte(0);
            return factoredYaw / ROTATION_FACTOR;
        } else {
            return yaw;
        }
    }

    private void setYaw(float yaw) {
        if (nmsPacket != null) {
            writeByte(0, (byte) ((int) (yaw * ROTATION_FACTOR)));
        } else {
            this.yaw = yaw;
        }
    }

    private float getPitch() {
        if (nmsPacket != null) {
            byte factoredPitch = readByte(1);
            return factoredPitch / ROTATION_FACTOR;
        } else {
            return pitch;
        }
    }

    private void setPitch(float pitch) {
        if (nmsPacket != null) {
            writeByte(1, (byte) ((int) (pitch * ROTATION_FACTOR)));
        } else {
            this.pitch = pitch;
        }
    }

    private float getHeadPitch() {
        if (nmsPacket != null) {
            byte factoredHeadPitch = readByte(2);
            return factoredHeadPitch / ROTATION_FACTOR;
        } else {
            return headPitch;
        }
    }

    private void setHeadPitch(float headPitch) {
        if (nmsPacket != null) {
            writeByte(2, (byte) ((int) (headPitch * ROTATION_FACTOR)));
        } else {
            this.headPitch = headPitch;
        }
    }

    @Override
    public Object asNMSPacket() throws InvocationTargetException, InstantiationException, IllegalAccessException {
        Object packetInstance = packetConstructor.newInstance();
        WrappedPacketOutSpawnEntityLiving wrappedPacketOutSpawnEntityLiving = new WrappedPacketOutSpawnEntityLiving(new NMSPacket(packetInstance));
        wrappedPacketOutSpawnEntityLiving.setEntityId(getEntityId());
        wrappedPacketOutSpawnEntityLiving.setEntityType(getEntityType());
        wrappedPacketOutSpawnEntityLiving.setPosition(getPosition());
        wrappedPacketOutSpawnEntityLiving.setVelocity(getVelocity());
        wrappedPacketOutSpawnEntityLiving.setYaw(getYaw());
        wrappedPacketOutSpawnEntityLiving.setPitch(getPitch());
        wrappedPacketOutSpawnEntityLiving.setHeadPitch(getHeadPitch());
        return packetInstance;
    }
}
