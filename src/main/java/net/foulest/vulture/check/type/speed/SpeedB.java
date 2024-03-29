package net.foulest.vulture.check.type.speed;

import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.event.MovementEvent;
import org.jetbrains.annotations.NotNull;

@CheckInfo(name = "Speed (B)", type = CheckType.SPEED)
public class SpeedB extends Check {

    private double buffer;
    private double lastDeltaXZ;

    public SpeedB(PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(@NotNull MovementEvent event, long timestamp) {
        double deltaXZ = event.getDeltaXZ();

        // Checks the player for exemptions.
        if (player.getAllowFlight()
                || playerData.isNearLiquid()
                || playerData.isOnGround()
                || playerData.isOnClimbable()
                || player.getWalkSpeed() != 0.2
                || playerData.isInWeb()
                || deltaXZ <= 0.005) {
            lastDeltaXZ = deltaXZ;
            return;
        }

        double diff = lastDeltaXZ * 0.91F + 0.02;

        if (playerData.isSprinting()) {
            diff += 0.0063;
        }

        double deltaXZDiff = deltaXZ - diff;

        if (deltaXZDiff > 0.0 && diff > 0.08 && deltaXZ > 0.15) {
            if (++buffer > 8) {
                flag(true, "deltaXZ=" + deltaXZ
                        + " diff=" + diff
                        + " buffer=" + buffer);
                buffer /= 2;
            }
        } else {
            buffer = Math.max(buffer - 0.25, 0);
        }

        lastDeltaXZ = deltaXZ;
    }
}
