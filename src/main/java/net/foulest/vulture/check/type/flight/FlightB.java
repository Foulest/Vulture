package net.foulest.vulture.check.type.flight;

import lombok.NonNull;
import net.foulest.vulture.action.ActionType;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.event.MovementEvent;
import net.foulest.vulture.util.MovementUtil;
import net.foulest.vulture.util.block.BlockUtil;
import org.bukkit.GameMode;
import org.bukkit.potion.PotionEffectType;

@CheckInfo(name = "Flight (B)", type = CheckType.FLIGHT,
        description = "Checks for invalid y-axis movement when in water.")
public class FlightB extends Check {

    private double lastDeltaY;
    private double lastVelocity;

    private int ticksInWater;
    private int ticksAboveCombined;

    public FlightB(@NonNull PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(@NonNull MovementEvent event, long timestamp) {
        double deltaY = event.getDeltaY();
        double velocity = player.getVelocity().getY();

        // Checks the player for exemptions.
        if (player.getGameMode() == GameMode.CREATIVE
                || player.getGameMode() == GameMode.SPECTATOR
                || playerData.getTimeSince(ActionType.LOGIN) < 2000L
                || playerData.getTimeSince(ActionType.STEER_VEHICLE) < 500L
                || !playerData.isInLiquid()
                || playerData.isOnClimbable()
                || BlockUtil.isPlayerInUnloadedChunk(player)
                || BlockUtil.isLocationInUnloadedChunk(event.getToLocation())
                || BlockUtil.isLocationInUnloadedChunk(event.getFromLocation())
                || event.isTeleport(playerData)) {
            ticksInWater = 0;
            lastDeltaY = deltaY;
            lastVelocity = velocity;
            return;
        }

        ++ticksInWater;

        // Checks for invalid y-axis movement when in water.
        checkForInvalidY(deltaY, velocity);

        // Checks for invalid combined movement when in water.
        checkForCombined(deltaY, velocity);

        lastDeltaY = deltaY;
        lastVelocity = velocity;
    }

    /**
     * Checks for invalid y-axis movement when in water.
     *
     * @param deltaY The change in y-axis.
     * @param velocity The player's velocity.
     */
    public void checkForInvalidY(double deltaY, double velocity) {
        int jumpBoostLevel = MovementUtil.getPotionEffectLevel(player, PotionEffectType.JUMP);
        double maxDeltaY = 0.45 + jumpBoostLevel * 0.1;

        if (deltaY > maxDeltaY) {
            flag(true, "deltaY=" + deltaY
                    + " velocity=" + velocity
                    + " maxDeltaY=" + maxDeltaY);
        }
    }

    /**
     * Checks for invalid combined movement when in water.
     *
     * @param deltaY The change in y-axis.
     * @param velocity The player's velocity.
     */
    public void checkForCombined(double deltaY, double velocity) {
        boolean nearGround = playerData.isNearGround();

        double deltaYDiff = Math.abs(deltaY - lastDeltaY);
        double velocityDiff = Math.abs(velocity - lastVelocity);
        double combinedDiff = Math.abs(deltaYDiff - velocityDiff);
        double altDiff = Math.abs(deltaY - velocity);

        double combinedThreshold = 0.15;
        int ticksAboveThreshold = (nearGround ? 4 : 2);

        if (combinedDiff >= combinedThreshold) {
            if (++ticksAboveCombined >= ticksAboveThreshold && altDiff > 0.001) {
                flag(true, "deltaY=" + deltaY
                        + " velocity=" + velocity + " |"
                        + " deltaYDiff=" + deltaYDiff
                        + " velocityDiff=" + velocityDiff + " |"
                        + " combinedDiff=" + combinedDiff
                        + " altDiff=" + altDiff + " |"
                        + " ticksInWater=" + ticksInWater
                        + " ticksAboveCombined=" + ticksAboveCombined
                        + " nearGround=" + nearGround
                );
            }
        } else {
            ticksAboveCombined = 0;
        }
    }
}
