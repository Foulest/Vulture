package io.github.retrooper.packetevents.packetwrappers.play.out.unloadchunk;

import io.github.retrooper.packetevents.packettype.PacketTypeClasses;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;
import io.github.retrooper.packetevents.packetwrappers.api.SendableWrapper;
import io.github.retrooper.packetevents.utils.server.ServerVersion;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;

/**
 * Wrapper for the UnloadChunk packet.
 *
 * @author retrooper, Tecnio
 * @since 1.8
 */
public final class WrappedPacketOutUnloadChunk extends WrappedPacket implements SendableWrapper {

    private static Constructor<?> packetConstructor;
    private int chunkX;
    private int chunkZ;

    public WrappedPacketOutUnloadChunk(NMSPacket packet) {
        super(packet);
    }

    public WrappedPacketOutUnloadChunk(int chunkX, int chunkZ) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
    }

    @Override
    protected void load() {
        try {
            packetConstructor = PacketTypeClasses.Play.Server.UNLOAD_CHUNK.getConstructor(int.class, int.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public int getChunkX() {
        if (packet != null) {
            return readInt(0);
        } else {
            return chunkX;
        }
    }

    public void setChunkX(int chunkX) {
        if (packet != null) {
            writeInt(0, chunkX);
        } else {
            this.chunkX = chunkX;
        }
    }

    public int getChunkZ() {
        if (packet != null) {
            return readInt(1);
        } else {
            return chunkZ;
        }
    }

    public void setChunkZ(int chunkZ) {
        if (packet != null) {
            writeInt(1, chunkZ);
        } else {
            this.chunkZ = chunkZ;
        }
    }

    @Override
    public boolean isSupported() {
        return version.isNewerThan(ServerVersion.v_1_8_8);
    }

    @Override
    public @NotNull Object asNMSPacket() throws Exception {
        return packetConstructor.newInstance(getChunkX(), getChunkZ());
    }
}
