package net.foulest.vulture.check.type.aimassist;

import com.google.common.collect.Lists;
import io.github.retrooper.packetevents.packetwrappers.play.in.flying.WrappedPacketInFlying;
import lombok.NonNull;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.event.RotationEvent;

import java.util.Deque;

@CheckInfo(name = "AimAssist (I)", type = CheckType.AIMASSIST)
public class AimAssistI extends Check {

    private final Deque<Double> yawSamples = Lists.newLinkedList();
    private final Deque<Double> pitchSamples = Lists.newLinkedList();
    private double buffer;
    private double lastAverageYaw;
    private double lastAveragePitch;

    public AimAssistI(@NonNull PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(@NonNull RotationEvent event, long timestamp) {
        WrappedPacketInFlying to = event.getTo();
        WrappedPacketInFlying from = event.getFrom();

        double deltaYaw = Math.abs(to.getYaw() - from.getYaw());
        double deltaPitch = Math.abs(to.getPitch() - from.getPitch());

        if (deltaYaw > 0.0 && deltaYaw < 30.0 && deltaPitch > 0.0 && deltaPitch < 30.0) {
            yawSamples.add(deltaYaw);
            pitchSamples.add(deltaPitch);
        }

        if (yawSamples.size() >= 20 && pitchSamples.size() >= 20) {
            double averageYaw = yawSamples.stream().mapToDouble(d -> d).average().orElse(0.0);
            double averagePitch = pitchSamples.stream().mapToDouble(d -> d).average().orElse(0.0);

            double finalYaw = averageYaw - lastAverageYaw;
            double finalPitch = averagePitch - lastAveragePitch;

            if (finalYaw > 3.859 && finalYaw < 5.2 && finalPitch > 0.827 && finalPitch < 1.186) {
                if (++buffer > 3) {
                    flag(" finalYaw=" + finalYaw
                            + "finalPitch=" + finalPitch);
                }
            } else {
                buffer = Math.max(buffer - 1.25, 0);
            }

            yawSamples.clear();
            pitchSamples.clear();

            lastAverageYaw = averageYaw;
            lastAveragePitch = averagePitch;
        }
    }
}
