package net.foulest.packetevents.packetwrappers.play.in.difficultychange;

import net.foulest.packetevents.packetwrappers.NMSPacket;
import net.foulest.packetevents.packetwrappers.WrappedPacket;
import org.bukkit.Difficulty;

/**
 * Wrapper for the DifficultyChange packet.
 *
 * @author Tecnio
 * @since 1.8
 */
final class WrappedPacketInDifficultyChange extends WrappedPacket {

    WrappedPacketInDifficultyChange(NMSPacket packet) {
        super(packet);
    }

    public Difficulty getDifficulty() {
        return readDifficulty();
    }

    public void setDifficulty(Difficulty difficulty) {
        writeDifficulty(difficulty);
    }
}
