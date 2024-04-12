package io.github.retrooper.packetevents.packetwrappers.play.out.login;

import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.api.helper.WrappedPacketEntityAbstraction;
import io.github.retrooper.packetevents.utils.server.ServerVersion;
import org.bukkit.GameMode;

// TODO: Make sendable and finish
public class WrappedPacketOutLogin extends WrappedPacketEntityAbstraction {

    private static boolean v_1_13_2;
    private static boolean v_1_17;

    public WrappedPacketOutLogin(NMSPacket packet) {
        super(packet);
    }

    @Override
    protected void load() {
        v_1_13_2 = version.isNewerThanOrEquals(ServerVersion.v_1_13_2);
        v_1_17 = version.isNewerThanOrEquals(ServerVersion.v_1_17);
    }

    public boolean isHardcore() {
        if (packet != null) {
            return readBoolean(0);
        } else {
            // TODO: finish
            return false;
        }
    }

    public void setHardcore(boolean value) {
        if (packet != null) {
            writeBoolean(0, value);
        } else {
            // TODO: finish
        }
    }

    public GameMode getGameMode() {
        if (packet != null) {
            return readGameMode(0);
        } else {
            // TODO: finish
            return null;
        }
    }

    public void setGameMode(GameMode gameMode) {
        if (packet != null) {
            writeGameMode(0, gameMode);
        } else {
            // TODO: finish
        }
    }

    public int getMaxPlayers() {
        if (packet != null) {
            int index = v_1_13_2 && !v_1_17 ? 1 : 2;
            return readInt(index);
        } else {
            // TODO: Finish
            return -1;
        }
    }
}
