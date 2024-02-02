package net.foulest.vulture.check.type.flight;

import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.event.MovementEvent;

@CheckInfo(name = "Flight (D)", type = CheckType.FLIGHT,
        description = "Detects players continually rising.")
public class FlightD extends Check {

    private double lastDeltaY;
    private double buffer;
    private int ticksRising;

    public FlightD(PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(MovementEvent event, long timestamp) {
        // Checks the player for exemptions.
        if (player.isFlying()
                || playerData.isNearClimbable()
                || playerData.isNearLiquid()
                || playerData.getVelocityY() > 0
                || playerData.getLastVelocityY() > 0
                || event.isTeleport(playerData)) {
            buffer = 0;
            return;
        }

        double deltaY = event.getDeltaY();

        // Checks if the player is continually rising for more than 2 ticks.
        if (deltaY > 0 && deltaY >= lastDeltaY) {
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
