package io.github.retrooper.packetevents.packetwrappers.play.out.spawnentityliving;

import io.github.retrooper.packetevents.packettype.PacketTypeClasses;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.api.SendableWrapper;
import io.github.retrooper.packetevents.packetwrappers.api.helper.WrappedPacketEntityAbstraction;
import io.github.retrooper.packetevents.utils.vector.Vector3d;
import net.foulest.vulture.util.MathUtil;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.lang.reflect.Constructor;

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

    public EntityType getEntityType() {
        if (packet != null) {
            int entityTypeID = readInt(1);
            return EntityType.fromId(entityTypeID);
        } else {
            return entityType;
        }
    }

    public void setEntityType(EntityType entityType) {
        if (packet != null) {
            writeInt(1, entityType.getTypeId());
        } else {
            this.entityType = entityType;
        }
    }

    public Vector3d getPosition() {
        if (packet != null) {
            double x = readInt(2) / 32.0;
            double y = readInt(3) / 32.0;
            double z = readInt(4) / 32.0;
            return new Vector3d(x, y, z);
        } else {
            return position;
        }
    }

    public void setPosition(Vector3d position) {
        if (packet != null) {
            writeInt(2, MathUtil.floorDouble(position.x * 32.0));
            writeInt(3, MathUtil.floorDouble(position.y * 32.0));
            writeInt(4, MathUtil.floorDouble(position.z * 32.0));
        } else {
            this.position = position;
        }
    }

    public Vector3d getVelocity() {
        if (packet != null) {
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

    public void setVelocity(Vector3d velocity) {
        if (packet != null) {
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

    public float getYaw() {
        if (packet != null) {
            byte factoredYaw = readByte(0);
            return factoredYaw / ROTATION_FACTOR;
        } else {
            return yaw;
        }
    }

    public void setYaw(float yaw) {
        if (packet != null) {
            writeByte(0, (byte) ((int) (yaw * ROTATION_FACTOR)));
        } else {
            this.yaw = yaw;
        }
    }

    public float getPitch() {
        if (packet != null) {
            byte factoredPitch = readByte(1);
            return factoredPitch / ROTATION_FACTOR;
        } else {
            return pitch;
        }
    }

    public void setPitch(float pitch) {
        if (packet != null) {
            writeByte(1, (byte) ((int) (pitch * ROTATION_FACTOR)));
        } else {
            this.pitch = pitch;
        }
    }

    public float getHeadPitch() {
        if (packet != null) {
            byte factoredHeadPitch = readByte(2);
            return factoredHeadPitch / ROTATION_FACTOR;
        } else {
            return headPitch;
        }
    }

    public void setHeadPitch(float headPitch) {
        if (packet != null) {
            writeByte(2, (byte) ((int) (headPitch * ROTATION_FACTOR)));
        } else {
            this.headPitch = headPitch;
        }
    }

    @Override
    public Object asNMSPacket() throws Exception {
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
