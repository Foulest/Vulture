package io.github.retrooper.packetevents.packetwrappers.play.out.respawn;

import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;
import lombok.ToString;
import org.bukkit.GameMode;
import org.bukkit.World;

@ToString
class WrappedPacketOutRespawn extends WrappedPacket {

    private World.Environment dimension;
    private GameMode gameMode;

    WrappedPacketOutRespawn(NMSPacket packet) {
        super(packet);
    }

    public World.Environment getDimension() {
        if (nmsPacket != null) {
            return readDimension();
        } else {
            return dimension;
        }
    }

    public void setDimension(World.Environment dimension) {
        if (nmsPacket != null) {
            writeDimension(dimension);
        } else {
            this.dimension = dimension;
        }
    }

    public GameMode getGameMode() {
        if (nmsPacket != null) {
            return readGameMode(0);
        } else {
            return gameMode;
        }
    }

    public void setGameMode(GameMode gameMode) {
        if (nmsPacket != null) {
            writeGameMode(gameMode);
        } else {
            this.gameMode = gameMode;
        }
    }
}
