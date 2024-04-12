package io.github.retrooper.packetevents.packetwrappers.play.out.entityvelocity;

import io.github.retrooper.packetevents.packettype.PacketTypeClasses;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.api.SendableWrapper;
import io.github.retrooper.packetevents.packetwrappers.api.helper.WrappedPacketEntityAbstraction;
import io.github.retrooper.packetevents.utils.nms.NMSUtils;
import io.github.retrooper.packetevents.utils.vector.Vector3d;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;

public final class WrappedPacketOutEntityVelocity extends WrappedPacketEntityAbstraction implements SendableWrapper {

    private static Constructor<?> velocityConstructor;
    private static boolean isVec3dPresent;
    private double velocityX;
    private double velocityY;
    private double velocityZ;

    public WrappedPacketOutEntityVelocity(NMSPacket packet) {
        super(packet);
    }

    public WrappedPacketOutEntityVelocity(@NotNull Entity entity, double velocityX, double velocityY, double velocityZ) {
        this.entityID = entity.getEntityId();
        this.entity = entity;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.velocityZ = velocityZ;
    }

    public WrappedPacketOutEntityVelocity(int entityID, double velX, double velY, double velZ) {
        this.entityID = entityID;
        this.velocityX = velX;
        this.velocityY = velY;
        this.velocityZ = velZ;
    }

    @Override
    protected void load() {
        Class<?> velocityClass = PacketTypeClasses.Play.Server.ENTITY_VELOCITY;

        try {
            velocityConstructor = velocityClass.getConstructor(int.class, double.class, double.class, double.class);
        } catch (NoSuchMethodException e) {
            // That is fine, just a newer version
            try {
                velocityConstructor = velocityClass.getConstructor(int.class, NMSUtils.vec3DClass);
                isVec3dPresent = true;
                // vec3d constructor
            } catch (NoSuchMethodException e2) {
                e2.printStackTrace();
            }
        }
    }

    @Contract(" -> new")
    public @NotNull Vector3d getVelocity() {
        if (packet != null) {
            double velX = readInt(1) / 8000.0;
            double velY = readInt(2) / 8000.0;
            double velZ = readInt(3) / 8000.0;
            return new Vector3d(velX, velY, velZ);
        } else {
            return new Vector3d(velocityX, velocityY, velocityZ);
        }
    }

    public void setVelocity(Vector3d velocity) {
        if (packet != null) {
            writeInt(1, (int) (velocity.x * 8000.0));
            writeInt(2, (int) (velocity.y * 8000.0));
            writeInt(3, (int) (velocity.z * 8000.0));
        } else {
            this.velocityX = velocity.x;
            this.velocityY = velocity.y;
            this.velocityZ = velocity.z;
        }
    }

    @Override
    public @NotNull Object asNMSPacket() throws Exception {
        if (!isVec3dPresent) {
            return velocityConstructor.newInstance(getEntityId(), velocityX, velocityY, velocityZ);
        } else {
            return velocityConstructor.newInstance(getEntityId(), NMSUtils.generateVec3D(velocityX, velocityY, velocityZ));
        }
    }
}
