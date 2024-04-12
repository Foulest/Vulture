package io.github.retrooper.packetevents.packetwrappers.play.out.respawn;

import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.WorldType;

// TODO: finish and test
class WrappedPacketOutRespawn extends WrappedPacket {

    private World.Environment dimension;
    private Difficulty difficulty;
    private GameMode gameMode;
    private WorldType levelType;

    public WrappedPacketOutRespawn(NMSPacket packet) {
        super(packet);
    }

    public World.Environment getDimension() {
        if (packet != null) {
            return readDimension(0, 0);
        } else {
            return dimension;
        }
    }

    public void setDimension(World.Environment dimension) {
        if (packet != null) {
            writeDimension(0, 0, dimension);
        } else {
            this.dimension = dimension;
        }
    }

    public GameMode getGameMode() {
        if (packet != null) {
            return readGameMode(0);
        } else {
            return gameMode;
        }
    }

    public void setGameMode(GameMode gameMode) {
        if (packet != null) {
            writeGameMode(0, gameMode);
        } else {
            this.gameMode = gameMode;
        }
    }
}
