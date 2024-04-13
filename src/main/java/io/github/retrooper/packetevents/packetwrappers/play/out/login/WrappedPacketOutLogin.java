package io.github.retrooper.packetevents.packetwrappers.play.out.login;

import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.api.helper.WrappedPacketEntityAbstraction;
import org.bukkit.GameMode;

public class WrappedPacketOutLogin extends WrappedPacketEntityAbstraction {

    public WrappedPacketOutLogin(NMSPacket packet) {
        super(packet);
    }

    public boolean isHardcore() {
        if (packet != null) {
            return readBoolean(0);
        } else {
            return false;
        }
    }

    public void setHardcore(boolean value) {
        if (packet != null) {
            writeBoolean(0, value);
        }
    }

    public GameMode getGameMode() {
        if (packet != null) {
            return readGameMode(0);
        } else {
            return null;
        }
    }

    public void setGameMode(GameMode gameMode) {
        if (packet != null) {
            writeGameMode(0, gameMode);
        }
    }

    public int getMaxPlayers() {
        if (packet != null) {
            return readInt(2);
        } else {
            return -1;
        }
    }
}
