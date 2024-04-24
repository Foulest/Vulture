package net.foulest.vulture.event;

import io.github.retrooper.packetevents.packetwrappers.play.in.flying.WrappedPacketInFlying;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.foulest.vulture.data.PlayerData;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
@AllArgsConstructor
public class RotationEvent {

    public final WrappedPacketInFlying to;
    public final WrappedPacketInFlying from;

    /**
     * Gets the change in yaw.
     *
     * @return The change in yaw.
     */
    public double getDeltaYaw() {
        return Math.abs(to.getYaw() - from.getYaw());
    }

    /**
     * Gets the change in pitch.
     *
     * @return The change in pitch.
     */
    public double getDeltaPitch() {
        return Math.abs(to.getPitch() - from.getPitch());
    }

    /**
     * Checks if the player is teleporting.
     *
     * @return Whether the player is teleporting.
     */
    public boolean isTeleport(@NotNull PlayerData playerData) {
        return playerData.isTeleporting(to.getPosition());
    }
}
