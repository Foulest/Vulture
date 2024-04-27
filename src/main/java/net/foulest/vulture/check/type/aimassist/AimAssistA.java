package net.foulest.vulture.check.type.aimassist;

import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.event.RotationEvent;
import org.jetbrains.annotations.NotNull;

@CheckInfo(name = "AimAssist (A)", type = CheckType.AIMASSIST,
        description = "Detects impossibly low pitch & yaw values.", experimental = true)
public class AimAssistA extends Check {

    public AimAssistA(PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(@NotNull RotationEvent event, long timestamp) {
        float deltaYaw = event.getDeltaYaw();
        float deltaPitch = event.getDeltaPitch();

        if (deltaYaw != 0.0 && Math.abs(deltaYaw) < 0.0001) {
            flag(false, "(Impossibly Low) deltaYaw=" + deltaYaw);
        }

        if (deltaPitch != 0.0 && Math.abs(deltaPitch) < 0.0001) {
            flag(false, "(Impossibly Low) deltaPitch=" + deltaPitch);
        }
    }
}
