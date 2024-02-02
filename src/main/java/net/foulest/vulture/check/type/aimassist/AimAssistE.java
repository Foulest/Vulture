package net.foulest.vulture.check.type.aimassist;

import io.github.retrooper.packetevents.packetwrappers.play.in.flying.WrappedPacketInFlying;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.event.RotationEvent;
import net.foulest.vulture.util.MathUtil;
import org.jetbrains.annotations.NotNull;

@CheckInfo(name = "AimAssist (E)", type = CheckType.AIMASSIST)
public class AimAssistE extends Check {

    private double buffer;

    public AimAssistE(PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(@NotNull RotationEvent event, long timestamp) {
        WrappedPacketInFlying to = event.getTo();
        WrappedPacketInFlying from = event.getFrom();

        float angleYaw = MathUtil.getDistanceBetweenAngles(to.getYaw(), from.getYaw());
        float anglePitch = MathUtil.getDistanceBetweenAngles(to.getPitch(), from.getPitch());
        double remainder = anglePitch % angleYaw;

        if (angleYaw > 0.1 && Double.isNaN(remainder)) {
            if (++buffer > 10) {
                flag(false, "angleYaw=" + angleYaw);
            }
        } else {
            buffer = Math.max(buffer - 1, 0);
        }
    }
}
