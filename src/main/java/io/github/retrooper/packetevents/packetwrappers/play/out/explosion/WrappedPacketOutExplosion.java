package io.github.retrooper.packetevents.packetwrappers.play.out.explosion;

import io.github.retrooper.packetevents.packettype.PacketTypeClasses;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;
import io.github.retrooper.packetevents.packetwrappers.api.SendableWrapper;
import io.github.retrooper.packetevents.packetwrappers.api.WrapperPacketReader;
import io.github.retrooper.packetevents.utils.nms.NMSUtils;
import io.github.retrooper.packetevents.utils.vector.Vector3d;
import io.github.retrooper.packetevents.utils.vector.Vector3f;
import io.github.retrooper.packetevents.utils.vector.Vector3i;
import lombok.ToString;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@ToString
public class WrappedPacketOutExplosion extends WrappedPacket implements SendableWrapper {

    private static Constructor<?> packetConstructor;
    private double x;
    private double y;
    private double z;
    private float strength;
    private List<Vector3i> records;
    private float playerVelocityX;
    private float playerVelocityY;
    private float playerVelocityZ;

    public WrappedPacketOutExplosion(NMSPacket packet) {
        super(packet);
    }

    public WrappedPacketOutExplosion(@NotNull Vector3d position, float strength, List<Vector3i> records,
                                     @NotNull Vector3f playerVelocity) {
        x = position.x;
        y = position.y;
        z = position.z;
        this.strength = strength;
        this.records = records;
        playerVelocityX = playerVelocity.x;
        playerVelocityY = playerVelocity.y;
        playerVelocityZ = playerVelocity.z;
    }

    public WrappedPacketOutExplosion(double x, double y, double z, float strength, List<Vector3i> records,
                                     float playerVelocityX, float playerVelocityY, float playerVelocityZ) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.strength = strength;
        this.records = records;
        this.playerVelocityX = playerVelocityX;
        this.playerVelocityY = playerVelocityY;
        this.playerVelocityZ = playerVelocityZ;
    }

    @Override
    protected void load() {
        try {
            packetConstructor = PacketTypeClasses.Play.Server.EXPLOSION.getConstructor(double.class,
                    double.class, double.class, float.class, List.class, NMSUtils.vec3DClass);
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
        }
    }

    public Vector3d getPosition() {
        if (nmsPacket != null) {
            return new Vector3d(readDouble(0), readDouble(1), readDouble(2));
        } else {
            return new Vector3d(x, y, z);
        }
    }

    public void setPosition(Vector3d position) {
        if (nmsPacket != null) {
            writeDouble(0, position.x);
            writeDouble(1, position.y);
            writeDouble(2, position.z);
        } else {
            x = position.x;
            y = position.y;
            z = position.z;
        }
    }

    private float getStrength() {
        if (nmsPacket != null) {
            return readFloat(0);
        } else {
            return strength;
        }
    }

    public void setStrength(float strength) {
        if (nmsPacket != null) {
            writeFloat(0, strength);
        } else {
            this.strength = strength;
        }
    }

    private List<Vector3i> getRecords() {
        if (nmsPacket != null) {
            List<Vector3i> recordsList = new ArrayList<>();
            List<?> rawRecordsList = readObject(0, List.class);

            if (rawRecordsList.isEmpty()) {
                return new ArrayList<>();
            }

            for (Object position : rawRecordsList) {
                WrapperPacketReader posWrapper = new WrappedPacket(new NMSPacket(position));
                int xPos = posWrapper.readInt(0);
                int yPos = posWrapper.readInt(1);
                int zPos = posWrapper.readInt(2);
                recordsList.add(new Vector3i(xPos, yPos, zPos));
            }
            return recordsList;
        } else {
            return records;
        }
    }

    public void setRecords(List<Vector3i> records) {
        if (nmsPacket != null) {
            Collection<Object> nmsRecordsList = new ArrayList<>();

            for (Vector3i vector3i : records) {
                Object position = NMSUtils.generateNMSBlockPos(vector3i);
                nmsRecordsList.add(position);
            }

            write(List.class, 0, nmsRecordsList);
        } else {
            this.records = records;
        }
    }

    @Contract(" -> new")
    private @NotNull Vector3f getPlayerVelocity() {
        if (nmsPacket != null) {
            return new Vector3f(readFloat(1), readFloat(2), readFloat(3));
        } else {
            return new Vector3f(playerVelocityX, playerVelocityY, playerVelocityZ);
        }
    }

    public void setPlayerVelocity(Vector3f playerVelocity) {
        if (nmsPacket != null) {
            writeFloat(1, playerVelocity.x);
            writeFloat(2, playerVelocity.y);
            writeFloat(3, playerVelocity.z);
        } else {
            playerVelocityX = playerVelocity.x;
            playerVelocityY = playerVelocity.y;
            playerVelocityZ = playerVelocity.z;
        }
    }

    @Override
    public Object asNMSPacket() throws InvocationTargetException, InstantiationException, IllegalAccessException {
        List<Object> positions = new ArrayList<>();

        for (Vector3i vector3i : getRecords()) {
            Object position = NMSUtils.generateNMSBlockPos(vector3i);
            positions.add(position);
        }

        Vector3f velocity = getPlayerVelocity();
        Vector3f pos = getPlayerVelocity();
        Object vec = NMSUtils.generateVec3D(velocity);
        return packetConstructor.newInstance(pos.x, pos.y, pos.z, getStrength(), positions, vec);
    }
}
