package net.foulest.vulture.check.type.aimassist;

import com.google.common.collect.Lists;
import lombok.NonNull;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.event.RotationEvent;

import java.util.Deque;

@CheckInfo(name = "AimAssist (D)", type = CheckType.AIMASSIST)
public class AimAssistD extends Check {

    private final Deque<Double> yawSamples = Lists.newLinkedList();
    private final Deque<Double> pitchSamples = Lists.newLinkedList();
    private double buffer;
    private double lastAverageYaw;
    private double lastAveragePitch;

    public AimAssistD(@NonNull PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(@NonNull RotationEvent event, long timestamp) {
        double deltaYaw = event.getDeltaYaw();
        double deltaPitch = event.getDeltaPitch();

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
                    flag(false, " finalYaw=" + finalYaw
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
