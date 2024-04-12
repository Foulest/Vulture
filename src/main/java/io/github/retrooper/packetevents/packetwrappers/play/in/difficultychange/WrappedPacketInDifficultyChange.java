package io.github.retrooper.packetevents.packetwrappers.play.in.difficultychange;

import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;
import org.bukkit.Difficulty;

/**
 * Wrapper for the DifficultyChange packet.
 *
 * @author Tecnio
 * @since 1.8
 */
public final class WrappedPacketInDifficultyChange extends WrappedPacket {

    public WrappedPacketInDifficultyChange(NMSPacket packet) {
        super(packet);
    }

    public Difficulty getDifficulty() {
        return readDifficulty(0);
    }

    public void setDifficulty(Difficulty difficulty) {
        writeDifficulty(0, difficulty);
    }
}
