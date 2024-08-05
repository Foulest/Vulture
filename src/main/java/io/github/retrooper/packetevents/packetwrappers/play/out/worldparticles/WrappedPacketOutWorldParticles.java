package io.github.retrooper.packetevents.packetwrappers.play.out.worldparticles;

import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;
import io.github.retrooper.packetevents.utils.nms.NMSUtils;
import lombok.ToString;

import java.util.Optional;

// TODO: finish this wrapper and test
@ToString
public class WrappedPacketOutWorldParticles extends WrappedPacket {

    private static Class<? extends Enum<?>> particleEnumClass;
    private String particleName;
    private boolean longDistance;
    private float x;
    private float y;
    private float z;
    private float offsetX;
    private float offsetY;
    private float offsetZ;
    private float particleData;
    private int particleCount;
    private int[] data;

    public WrappedPacketOutWorldParticles(NMSPacket packet) {
        super(packet);
    }

    @Override
    protected void load() {
        try {
            particleEnumClass = NMSUtils.getNMSEnumClass("EnumParticle");
        } catch (ClassNotFoundException ignored) {
        }
    }

    protected String getParticleName() {
        if (nmsPacket != null) {
            Enum<?> enumConst = readEnumConstant(0, particleEnumClass);
            return enumConst.name(); // inconsistent
        } else {
            return particleName;
        }
    }

    public float getX() {
        if (nmsPacket != null) {
            return readFloat(0);
        } else {
            return x;
        }
    }

    public void setX(float x) {
        if (nmsPacket != null) {
            writeFloat(0, x);
        } else {
            this.x = x;
        }
    }

    public float getY() {
        if (nmsPacket != null) {
            return readFloat(1);
        } else {
            return y;
        }
    }

    public void setY(float y) {
        if (nmsPacket != null) {
            writeFloat(1, y);
        } else {
            this.y = y;
        }
    }

    public float getZ() {
        if (nmsPacket != null) {
            return readFloat(2);
        } else {
            return z;
        }
    }

    public void setZ(float z) {
        if (nmsPacket != null) {
            writeFloat(2, z);
        } else {
            this.z = z;
        }
    }

    public float getOffsetX() {
        if (nmsPacket != null) {
            return readFloat(3);
        } else {
            return offsetX;
        }
    }

    public void setOffsetX(float offsetX) {
        if (nmsPacket != null) {
            writeFloat(3, offsetX);
        } else {
            this.offsetX = offsetX;
        }
    }

    public float getOffsetY() {
        if (nmsPacket != null) {
            return readFloat(4);
        } else {
            return offsetY;
        }
    }

    public void setOffsetY(float offsetY) {
        if (nmsPacket != null) {
            writeFloat(4, offsetY);
        } else {
            this.offsetY = offsetY;
        }
    }

    public float getOffsetZ() {
        if (nmsPacket != null) {
            return readFloat(5);
        } else {
            return offsetZ;
        }
    }

    public void setOffsetZ(float offsetZ) {
        if (nmsPacket != null) {
            writeFloat(5, offsetZ);
        } else {
            this.offsetZ = offsetZ;
        }
    }

    public float getParticleData() {
        if (nmsPacket != null) {
            return readFloat(6);
        } else {
            return particleData;
        }
    }

    public void setParticleData(float particleData) {
        if (nmsPacket != null) {
            writeFloat(6, particleData);
        } else {
            this.particleData = particleData;
        }
    }

    public int getParticleCount() {
        if (nmsPacket != null) {
            return readInt(0);
        } else {
            return particleCount;
        }
    }

    public void setParticleCount(int particleCount) {
        if (nmsPacket != null) {
            writeInt(0, particleCount);
        } else {
            this.particleCount = particleCount;
        }
    }

    public Optional<int[]> getData() {
        if (nmsPacket != null) {
            return Optional.of(readIntArray(0));
        } else {
            return Optional.of(data);
        }
    }

    public void setData(int[] data) {
        if (nmsPacket != null) {
            writeIntArray(0, data);
        } else {
            this.data = data;
        }
    }

    public Optional<Boolean> isLongDistance() {
        if (nmsPacket != null) {
            return Optional.of(readBoolean(0));
        } else {
            return Optional.of(longDistance);
        }
    }

    public void setLongDistance(boolean longDistance) {
        if (nmsPacket != null) {
            writeBoolean(0, longDistance);
        } else {
            this.longDistance = longDistance;
        }
    }
}
