package net.foulest.vulture.check.type.invalid;

import lombok.NonNull;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.event.MovementEvent;

@CheckInfo(name = "Invalid (B)", type = CheckType.INVALID,
        description = "Detects players continually rising.")
public class InvalidB extends Check {

    private double lastDeltaY;
    private double buffer;
    private int ticksRising;

    public InvalidB(@NonNull PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(@NonNull MovementEvent event, long timestamp) {
        // Checks the player for exemptions.
        if (player.isFlying()
                || playerData.isOnClimbable()
                || playerData.isNearLiquid()
                || playerData.getVelocityY() > 0
                || playerData.getLastVelocityY() > 0
                || event.isTeleport(playerData)) {
            buffer = 0;
            return;
        }

        double deltaY = event.getDeltaY();
        boolean rising = deltaY > 0 && deltaY >= lastDeltaY;

        if (rising) {
            if (++ticksRising > 2) {
                if (++buffer >= 2) {
                    flag(true, "deltaY=" + deltaY
                            + " lastDeltaY=" + lastDeltaY
                            + " ticksRising=" + ticksRising);
                }
            } else {
                buffer = Math.max(buffer - 0.25, 0);
            }
        } else {
            ticksRising = 0;
        }

        lastDeltaY = deltaY;
    }
}
