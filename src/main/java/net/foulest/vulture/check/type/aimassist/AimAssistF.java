package net.foulest.vulture.check.type.aimassist;

import com.google.common.collect.Lists;
import lombok.NonNull;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.event.RotationEvent;

import java.util.Deque;

@CheckInfo(name = "AimAssist (F)", type = CheckType.AIMASSIST)
public class AimAssistF extends Check {

    private final Deque<Double> samples = Lists.newLinkedList();
    private double bufferA;
    private double bufferB;

    public AimAssistF(@NonNull PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(@NonNull RotationEvent event, long timestamp) {
        double deltaYaw = event.getDeltaYaw();
        double deltaPitch = event.getDeltaPitch();

        if (deltaYaw > 0.0 && deltaYaw < 30.0 && deltaPitch > 0.0 && deltaPitch < 30.0) {
            samples.add(deltaPitch);
        }

        if (samples.size() == 120) {
            int distinct = (int) (samples.stream().distinct().count());
            int duplicates = samples.size() - distinct;
            double average = samples.stream().mapToDouble(d -> d).average().orElse(0.0);

            if (duplicates >= 10 && average > 5.66 && average < 12.0) {
                if (++bufferA > 5.25) {
                    flag(false, "A"
                            + " (duplicates=" + duplicates
                            + " average=" + average + ")");
                }
            } else {
                bufferA = Math.max(bufferA - 1.25, 0);
            }

            if (duplicates > 6 && duplicates <= 16 && average > 7.5 && average < 25.0) {
                if (++bufferB > 1) {
                    flag(false, "B"
                            + " (duplicates=" + duplicates
                            + " average=" + average + ")");
                }
            } else {
                bufferB = Math.max(bufferB - 1, 0);
            }

            samples.clear();
        }
    }
}
