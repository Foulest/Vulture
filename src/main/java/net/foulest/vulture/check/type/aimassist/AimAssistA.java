package net.foulest.vulture.check.type.aimassist;

import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.event.RotationEvent;
import org.jetbrains.annotations.NotNull;

@CheckInfo(name = "AimAssist (A)", type = CheckType.AIMASSIST,
        description = "Detects basic AimAssist.", experimental = true)
public class AimAssistA extends Check {

    private double lastDeltaYaw;
    private double lastDeltaPitch;

    public AimAssistA(PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(@NotNull RotationEvent event, long timestamp) {
        double deltaYaw = event.getDeltaYaw();
        double deltaPitch = event.getDeltaPitch();

        if (deltaYaw != 0.0 && Math.abs(deltaYaw) < 0.0001) {
            flag(false, "(Impossibly Low) deltaYaw=" + deltaYaw);
        }

        if (deltaPitch != 0.0 && Math.abs(deltaPitch) < 0.0001) {
            flag(false, "(Impossibly Low) deltaPitch=" + deltaPitch);
        }

        lastDeltaYaw = deltaYaw;
        lastDeltaPitch = deltaPitch;
    }
}
