package net.foulest.vulture.check.type.aimassist;

import io.github.retrooper.packetevents.packetwrappers.play.in.flying.WrappedPacketInFlying;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.event.RotationEvent;
import net.foulest.vulture.util.MathUtil;
import org.jetbrains.annotations.NotNull;

@CheckInfo(name = "AimAssist (C)", type = CheckType.AIMASSIST)
public class AimAssistC extends Check {

    private int buffer;

    public AimAssistC(PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(@NotNull RotationEvent event, long timestamp) {
        WrappedPacketInFlying to = event.getTo();
        WrappedPacketInFlying from = event.getFrom();

        float yawChange = MathUtil.getDistanceBetweenAngles(to.getYaw(), from.getYaw());
        double yawChangeDiff = Math.abs(Math.floor(yawChange) - yawChange);

        if (yawChange > 0 && yawChangeDiff < 1.0E-10) {
            if (++buffer > 2) {
                flag(false, "yawChange=" + yawChange
                        + " yawChangeDiff=" + yawChangeDiff
                        + " fromYaw=" + from.getYaw());
            } else {
                buffer = 0;
            }
        }
    }
}
