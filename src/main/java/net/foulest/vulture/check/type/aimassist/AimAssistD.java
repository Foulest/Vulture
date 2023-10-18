package net.foulest.vulture.check.type.aimassist;

import io.github.retrooper.packetevents.packetwrappers.play.in.flying.WrappedPacketInFlying;
import io.github.retrooper.packetevents.utils.vector.Vector3d;
import lombok.NonNull;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.event.RotationEvent;

@CheckInfo(name = "AimAssist (D)", type = CheckType.AIMASSIST)
public class AimAssistD extends Check {

    private double buffer;

    public AimAssistD(@NonNull PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(@NonNull RotationEvent event, long timestamp) {
        WrappedPacketInFlying to = event.getTo();
        WrappedPacketInFlying from = event.getFrom();

        Vector3d toPosition = to.getPosition();

        if (playerData.isTeleporting(toPosition)) {
            return;
        }

        double deltaYaw = Math.abs(to.getYaw() - from.getYaw());

        if (deltaYaw % 0.5 == 0.0 && deltaYaw > 0) {
            if (++buffer >= 5) {
                flag("deltaYaw=" + deltaYaw);
                buffer = 0;
            }
        } else {
            buffer = Math.max(buffer - 0.9, 0);
        }
    }
}
