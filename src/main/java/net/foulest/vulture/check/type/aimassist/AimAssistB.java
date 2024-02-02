package net.foulest.vulture.check.type.aimassist;

import io.github.retrooper.packetevents.packetwrappers.play.in.flying.WrappedPacketInFlying;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.event.RotationEvent;
import org.jetbrains.annotations.NotNull;

@CheckInfo(name = "AimAssist (B)", type = CheckType.AIMASSIST)
public class AimAssistB extends Check {

    private int buffer;

    public AimAssistB(PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(@NotNull RotationEvent event, long timestamp) {
        WrappedPacketInFlying to = event.getTo();
        WrappedPacketInFlying from = event.getFrom();

        // Checks the player for exemptions.
        if (to.getYaw() == from.getYaw()) {
            return;
        }

        float fromYaw = (from.getYaw() - 90) % 360;
        float toYaw = (to.getYaw() - 90) % 360;

        if (fromYaw < 0) {
            fromYaw += 360;
        }

        if (toYaw < 0) {
            toYaw += 360;
        }

        double deltaYaw = Math.abs(toYaw - fromYaw);

        if (deltaYaw % 1 == 0 && deltaYaw != 0) {
            if ((buffer += 12) > 35) {
                flag(false, "deltaYaw=" + deltaYaw
                        + " fromYaw=" + fromYaw);
            }
        } else {
            buffer = Math.max(buffer - 2, 0);
        }
    }
}
