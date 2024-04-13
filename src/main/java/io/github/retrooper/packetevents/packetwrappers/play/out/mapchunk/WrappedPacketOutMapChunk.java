package io.github.retrooper.packetevents.packetwrappers.play.out.mapchunk;

import io.github.retrooper.packetevents.packettype.PacketTypeClasses;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;
import io.github.retrooper.packetevents.utils.reflection.SubclassUtil;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.BitSet;
import java.util.Optional;

public class WrappedPacketOutMapChunk extends WrappedPacket {

    private static Class<?> chunkMapClass;
    private Constructor<?> chunkMapConstructor;
    private Object nmsChunkMap;

    public WrappedPacketOutMapChunk(NMSPacket packet) {
        super(packet);
    }

    @Override
    protected void load() {
        chunkMapClass = SubclassUtil.getSubClass(PacketTypeClasses.Play.Server.MAP_CHUNK, 0);

        try {
            chunkMapConstructor = chunkMapClass.getConstructor();
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
        }
    }

    public int getChunkX() {
        return readInt(0);
    }

    public void setChunkX(int chunkX) {
        writeInt(0, chunkX);
    }

    public int getChunkZ() {
        return readInt(1);
    }

    public void setChunkZ(int chunkZ) {
        writeInt(1, chunkZ);
    }

    public Optional<BitSet> getBitSet() {
        if (nmsChunkMap == null) {
            nmsChunkMap = readObject(0, chunkMapClass);
        }

        WrappedPacket nmsChunkMapWrapper = new WrappedPacket(new NMSPacket(nmsChunkMap));
        return Optional.of(BitSet.valueOf(new long[]{nmsChunkMapWrapper.readInt(0)}));
    }

    public void setPrimaryBitMask(@NotNull BitSet bitSet) {
        setPrimaryBitMask((int) bitSet.toLongArray()[0]);
    }

    /**
     * @param primaryBitMask Integer that determines which chunk sections the server is sending
     * @deprecated Possible lossy conversion on 1.17 servers that could result in the client only reading 32
     * out of the total possible 127 chunk sections. Safe to use on 1.16 and below servers
     */
    @Deprecated
    public void setPrimaryBitMask(int primaryBitMask) {
        if (nmsChunkMap == null) {
            try {
                nmsChunkMap = chunkMapConstructor.newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException ex) {
                ex.printStackTrace();
            }
        }

        WrappedPacket nmsChunkMapWrapper = new WrappedPacket(new NMSPacket(nmsChunkMap));
        nmsChunkMapWrapper.writeInt(0, primaryBitMask);
        write(chunkMapClass, 0, nmsChunkMap);
    }

    /**
     * @return Whether the packet overwrites the entire chunk column or just the sections being sent
     */
    public Optional<Boolean> isGroundUpContinuous() {
        return Optional.of(readBoolean(0));
    }

    /**
     * @param groundUpContinuous Whether the packet overwrites the entire chunk column or just the sections being sent
     */
    public void setGroundUpContinuous(boolean groundUpContinuous) {
        writeBoolean(0, groundUpContinuous);
    }

    public byte[] getCompressedData() {
        if (nmsChunkMap == null) {
            nmsChunkMap = readObject(0, chunkMapClass);
        }

        WrappedPacket nmsChunkMapWrapper = new WrappedPacket(new NMSPacket(nmsChunkMap));
        return nmsChunkMapWrapper.readByteArray(0);
    }

    public void setCompressedData(byte[] data) {
        if (nmsChunkMap == null) {
            try {
                nmsChunkMap = chunkMapConstructor.newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        WrappedPacket nmsChunkMapWrapper = new WrappedPacket(new NMSPacket(nmsChunkMap));
        nmsChunkMapWrapper.writeByteArray(0, data);
        write(chunkMapClass, 0, nmsChunkMap);
    }
}
