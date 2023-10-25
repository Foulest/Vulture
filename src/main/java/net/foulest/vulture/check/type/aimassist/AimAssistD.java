package net.foulest.vulture.check.type.aimassist;

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
        // Checks the player for exemptions.
        if (event.isTeleport(playerData)) {
            return;
        }

        double deltaYaw = event.getDeltaYaw();

        if (deltaYaw % 0.5 == 0.0 && deltaYaw > 0) {
            if (++buffer >= 5) {
                flag(false, "deltaYaw=" + deltaYaw);
                buffer = 0;
            }
        } else {
            buffer = Math.max(buffer - 0.9, 0);
        }
    }
}
