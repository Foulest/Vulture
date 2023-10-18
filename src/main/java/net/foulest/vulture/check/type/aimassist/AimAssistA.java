package net.foulest.vulture.check.type.aimassist;

import io.github.retrooper.packetevents.packetwrappers.play.in.flying.WrappedPacketInFlying;
import lombok.NonNull;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.event.RotationEvent;

@CheckInfo(name = "AimAssist (A)", type = CheckType.AIMASSIST)
public class AimAssistA extends Check {

    private double lastYaw;
    private double lastPitch;
    private double buffer;
    private double streak;

    public AimAssistA(@NonNull PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(@NonNull RotationEvent event, long timestamp) {
        WrappedPacketInFlying to = event.getTo();
        WrappedPacketInFlying from = event.getFrom();

        double deltaYaw = Math.abs(to.getYaw() - from.getYaw());
        double deltaPitch = Math.abs(to.getPitch() - from.getPitch());

        double yawAccel = Math.abs(deltaYaw - lastYaw);
        double pitchAccel = Math.abs(deltaPitch - lastPitch);

        if (yawAccel > 1.5 && pitchAccel > 1.4252 && pitchAccel < 2.209 && deltaPitch > 1.354 && deltaPitch < 1.4) {
            if (++buffer > 3) {
                buffer = 0;

                if (++streak > 2) {
                    flag("pitchAccel=" + pitchAccel
                            + " deltaPitch=" + deltaPitch);
                }
            }
        } else {
            buffer = Math.max(buffer - 0.5, 0);
            streak = Math.max(streak - 0.25, 0);
        }

        lastYaw = deltaYaw;
        lastPitch = deltaPitch;
    }
}
