package net.foulest.vulture.check.type.aimassist;

import lombok.NonNull;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.event.RotationEvent;

@CheckInfo(name = "AimAssist (G)", type = CheckType.AIMASSIST)
public class AimAssistG extends Check {

    private int buffer;

    public AimAssistG(@NonNull PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(@NonNull RotationEvent event, long timestamp) {
        double deltaYaw = event.getDeltaYaw();
        double deltaPitch = event.getDeltaPitch();

        if (deltaYaw > 1.1 && deltaYaw <= 2.992 && deltaPitch > 5.78 && deltaPitch < 5.84) {
            if (++buffer > 4) {
                flag(false, "deltaYaw=" + deltaYaw
                        + " deltaPitch=" + deltaPitch);
            }
        } else {
            buffer = 0;
        }
    }
}
