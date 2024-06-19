package io.github.retrooper.packetevents.packetwrappers.play.out.spawnentity;

import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.api.helper.WrappedPacketEntityAbstraction;
import io.github.retrooper.packetevents.utils.vector.Vector3d;
import net.foulest.vulture.util.MathUtil;

// TODO: Make this sendable
public class WrappedPacketOutSpawnEntity extends WrappedPacketEntityAbstraction {

    private static final float ROTATION_FACTOR = 256.0F / 360.0F;
    private Vector3d position;
    private Vector3d velocity;
    private float pitch;
    private float yaw;

    public WrappedPacketOutSpawnEntity(NMSPacket packet) {
        super(packet);
    }

    public Vector3d getPosition() {
        if (packet != null) {
            double x = readInt(1) / 32.0;
            double y = readInt(2) / 32.0;
            double z = readInt(3) / 32.0;
            return new Vector3d(x, y, z);
        } else {
            return position;
        }
    }

    public void setPosition(Vector3d position) {
        if (packet != null) {
            writeInt(1, MathUtil.floorDouble(position.x * 32.0));
            writeInt(2, MathUtil.floorDouble(position.y * 32.0));
            writeInt(3, MathUtil.floorDouble(position.z * 32.0));
        } else {
            this.position = position;
        }
    }

    public Vector3d getVelocity() {
        if (packet != null) {
            double velX = readInt(4) / 8000.0;
            double velY = readInt(5) / 8000.0;
            double velZ = readInt(6) / 8000.0;
            return new Vector3d(velX, velY, velZ);
        } else {
            return velocity;
        }
    }

    public void setVelocity(Vector3d velocity) {
        if (packet != null) {
            int velX = (int) (velocity.x * 8000.0);
            int velY = (int) (velocity.y * 8000.0);
            int velZ = (int) (velocity.z * 8000.0);

            writeInt(4, velX);
            writeInt(5, velY);
            writeInt(6, velZ);
        } else {
            this.velocity = velocity;
        }
    }

    public float getPitch() {
        if (packet != null) {
            int factoredPitch = readInt(7);
            return factoredPitch / ROTATION_FACTOR;
        } else {
            return pitch;
        }
    }

    public void setPitch(float pitch) {
        if (packet != null) {
            writeInt(7, MathUtil.floorDouble(pitch * ROTATION_FACTOR));
        } else {
            this.pitch = pitch;
        }
    }

    public float getYaw() {
        if (packet != null) {
            int factoredYaw = readInt(8);
            return factoredYaw / ROTATION_FACTOR;
        } else {
            return yaw;
        }
    }

    public void setYaw(float yaw) {
        if (packet != null) {
            writeInt(8, MathUtil.floorDouble(yaw * ROTATION_FACTOR));
        } else {
            this.yaw = yaw;
        }
    }
}
