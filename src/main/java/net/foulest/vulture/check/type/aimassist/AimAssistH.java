package net.foulest.vulture.check.type.aimassist;

import com.google.common.collect.Lists;
import lombok.NonNull;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.event.RotationEvent;

import java.util.Deque;

@CheckInfo(name = "AimAssist (H)", type = CheckType.AIMASSIST)
public class AimAssistH extends Check {

    private final Deque<Double> samples = Lists.newLinkedList();
    private double buffer;

    public AimAssistH(@NonNull PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(@NonNull RotationEvent event, long timestamp) {
        double deltaPitch = event.getDeltaPitch();

        if (deltaPitch > 0.0 && deltaPitch < 40.0) {
            samples.add(deltaPitch);
        }

        if (samples.size() == 40) {
            int distinct = (int) (samples.stream().distinct().count());
            int duplicates = samples.size() - distinct;
            double average = samples.stream().mapToDouble(d -> d).average().orElse(0.0);

            if (average > 19.5 && average < 26.5 && duplicates >= 2) {
                if (++buffer > 2) {
                    flag(false, "average=" + average
                            + " duplicates=" + duplicates);
                }
            } else {
                buffer = Math.max(buffer - 0.5, 0);
            }

            samples.clear();
        }
    }
}
