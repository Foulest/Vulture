package io.github.retrooper.packetevents.packetwrappers.play.out.login;

import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.api.helper.WrappedPacketEntityAbstraction;
import org.bukkit.GameMode;
import org.jetbrains.annotations.Nullable;

class WrappedPacketOutLogin extends WrappedPacketEntityAbstraction {

    WrappedPacketOutLogin(NMSPacket packet) {
        super(packet);
    }

    public boolean isHardcore() {
        if (nmsPacket != null) {
            return readBoolean(0);
        } else {
            return false;
        }
    }

    public void setHardcore(boolean value) {
        if (nmsPacket != null) {
            writeBoolean(0, value);
        }
    }

    public @Nullable GameMode getGameMode() {
        if (nmsPacket != null) {
            return readGameMode(0);
        } else {
            return null;
        }
    }

    public void setGameMode(GameMode gameMode) {
        if (nmsPacket != null) {
            writeGameMode(gameMode);
        }
    }

    public int getMaxPlayers() {
        if (nmsPacket != null) {
            return readInt(2);
        } else {
            return -1;
        }
    }
}
