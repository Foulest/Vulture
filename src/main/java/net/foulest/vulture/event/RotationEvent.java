package net.foulest.vulture.event;

import io.github.retrooper.packetevents.packetwrappers.play.in.flying.WrappedPacketInFlying;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.foulest.vulture.data.PlayerData;

@Getter
@Setter
@AllArgsConstructor
public class RotationEvent {

    public final WrappedPacketInFlying to;
    public final WrappedPacketInFlying from;

    public double getDeltaYaw() {
        return Math.abs(to.getYaw() - from.getYaw());
    }

    public double getDeltaPitch() {
        return Math.abs(to.getPitch() - from.getPitch());
    }

    public boolean isTeleport(PlayerData playerData) {
        return playerData.isTeleporting(to.getPosition());
    }
}
