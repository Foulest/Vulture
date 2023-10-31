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

@CheckInfo(name = "Flight (A)", type = CheckType.FLIGHT,
        description = "Checks for invalid y-axis movement when falling.")
public class FlightA extends Check {

    private static final double GRAVITY_DECAY = 0.08;
    private static final double GRAVITY_MULTIPLIER = 0.9800000190734863;
    private static final double ON_GROUND_VELOCITY = -0.0784000015258789;

    private double fallingBuffer;

    private double risingInAirBuffer;
    private double risingNearGroundBuffer;

    private double lastDeltaY;
    private double lastVelocity;

    private int fallingTicks;
    private int risingTicks;
    private int nearGroundTicks;
    private int flatDeltaYTicks;

    public FlightA(@NonNull PlayerData playerData) throws ClassNotFoundException {
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
                || playerData.isInLiquid()
                || playerData.isOnClimbable()
                || BlockUtil.isPlayerInUnloadedChunk(player)
                || BlockUtil.isLocationInUnloadedChunk(event.getToLocation())
                || BlockUtil.isLocationInUnloadedChunk(event.getFromLocation())
                || event.isTeleport(playerData)) {
            lastDeltaY = deltaY;
            lastVelocity = velocity;
            return;
        }

        double predictionY = (lastDeltaY - GRAVITY_DECAY) * GRAVITY_MULTIPLIER;

        boolean rising = velocity > ON_GROUND_VELOCITY;
        boolean falling = velocity < ON_GROUND_VELOCITY;
        boolean nearGround = playerData.isNearGround();

        nearGroundTicks = nearGround ? nearGroundTicks + 1 : 0;
        flatDeltaYTicks = deltaY == 0.0 ? flatDeltaYTicks + 1 : 0;

        if (rising) {
            handleRising(nearGround, deltaY, velocity, predictionY);
        } else if (falling) {
            handleFalling(nearGround, deltaY, velocity, predictionY);
        } else if (nearGround) {
            handleOnGround(deltaY, velocity, predictionY);
        }

        lastDeltaY = deltaY;
        lastVelocity = velocity;
    }

    /**
     * Handles when the player is rising.
     *
     * @param nearGround  Whether the player is near the ground.
     * @param deltaY      The change in y-axis.
     * @param velocity    The player's velocity.
     * @param predictionY The predicted y-axis.
     */
    private void handleRising(boolean nearGround, double deltaY, double velocity, double predictionY) {
        fallingTicks = 0;
        risingTicks = nearGround ? 0 : risingTicks + 1;

        // Calculate the predicted velocity without having intermediate variables
        double predictionVel = (lastDeltaY - GRAVITY_DECAY * risingTicks) * Math.pow(GRAVITY_MULTIPLIER, risingTicks);
        double predictionNextVel = (predictionVel - GRAVITY_DECAY) * GRAVITY_MULTIPLIER;

        // Calculate differences once
        double predictionDiffY = Math.abs(deltaY - predictionY);
        double predictionDiffNextVel = Math.abs(deltaY - predictionNextVel);

        if (predictionDiffY > 0.001 && predictionDiffNextVel > 0.15) {
            if (nearGround) {
                risingInAirBuffer = 0;

                if (predictionDiffY > 0.65 || predictionDiffNextVel > 0.6) {
                    if (++risingNearGroundBuffer >= 2) {
                        flag(true, "Rising, Near Ground"
                                + " deltaY=" + deltaY
                                + " velocity=" + velocity + " |"
                                + " predictionY=" + predictionY
                                + " predictionVel=" + predictionVel + " |"
                                + " predictionDiffY=" + predictionDiffY
                                + " predictionDiffNextVel=" + predictionDiffNextVel + " |"
                                + " risingTicks=" + risingTicks + ")");
                    }
                }
            } else {
                risingNearGroundBuffer = 0;

                // Fixes false flags when colliding with boats.
                if (playerData.isNearbyBoat(0.6, 0.6, 0.6)) {
                    if (deltaY == 0.0) {
                        return;
                    }
                }

                if (++risingInAirBuffer >= 3 && risingTicks >= 2) {
                    flag(true, "Rising, In Air"
                            + " (deltaY=" + deltaY
                            + " velocity=" + velocity + " |"
                            + " predictionY=" + predictionY
                            + " predictionVel=" + predictionVel + " |"
                            + " predictionDiffY=" + predictionDiffY
                            + " predictionDiffNextVel=" + predictionDiffNextVel + " |"
                            + " risingTicks=" + risingTicks
                            + " nearGroundTicks=" + nearGroundTicks + ")");
                }
            }
        } else {
            risingInAirBuffer = Math.max(risingInAirBuffer - 0.75, 0);
            risingNearGroundBuffer = Math.max(risingNearGroundBuffer - 0.75, 0);
        }
    }

    /**
     * Handles when the player is falling.
     *
     * @param nearGround  Whether the player is near the ground.
     * @param deltaY      The change in y-axis.
     * @param velocity    The player's velocity.
     * @param predictionY The predicted y-axis.
     */
    private void handleFalling(boolean nearGround, double deltaY, double velocity, double predictionY) {
        risingTicks = 0;
        fallingTicks = nearGround ? 0 : fallingTicks + 1;

        // Calculate the predicted velocity without having intermediate variables
        double predictionVel = (ON_GROUND_VELOCITY - GRAVITY_DECAY * fallingTicks) * Math.pow(GRAVITY_MULTIPLIER, fallingTicks);
        double predictionNextVel = (predictionVel - GRAVITY_DECAY) * GRAVITY_MULTIPLIER;

        // Calculate differences once
        double predictionDiffY = Math.abs(deltaY - predictionY);
        double predictionDiffVel = Math.abs(deltaY - predictionVel);
        double predictionDiffLastVel = Math.abs(deltaY - lastVelocity);
        double predictionDiffNextVel = Math.abs(deltaY - predictionNextVel);

        long timeSinceTeleport = playerData.getTimeSince(ActionType.TELEPORT);

        if (fallingTicks >= 1 && ((predictionDiffY > 0.005 && (predictionDiffVel > 0.005
                && predictionDiffLastVel > 0.005 && predictionDiffNextVel > 0.005))
                || predictionDiffVel > 1.00 || predictionDiffLastVel > 0.60 || predictionDiffNextVel > 1.05)) {

            if (predictionDiffVel > 0.25 || predictionDiffLastVel > 0.25 || predictionDiffNextVel > 0.25) {
                // Fixes false flags when colliding with boats.
                if (playerData.isNearbyBoat(0.6, 0.6, 0.6)) {
                    if (deltaY == 0.0) {
                        return;
                    }
                }

                // Fixes false flags when near climables.
                if (playerData.isNearClimbable()) {
                    return;
                }

                // Fixes false flags when teleporting.
                if (deltaY == -0.09800000190735147 && timeSinceTeleport <= 1000) {
                    return;
                }

                if (++fallingBuffer >= 2 || predictionDiffVel > 1.00
                        || predictionDiffLastVel > 0.60 || predictionDiffNextVel > 1.05) {
                    flag(true, "Falling"
                            + " deltaY=" + deltaY
                            + " velocity=" + velocity + " |"
                            + " predictionY=" + predictionY
                            + " predictionVel=" + predictionVel + " |"
                            + " predictionDiffY=" + predictionDiffY
                            + " predictionDiffVel=" + predictionDiffVel
                            + " predictionDiffLastVel=" + predictionDiffLastVel
                            + " predictionDiffNextVel=" + predictionDiffNextVel + " |"
                            + " timeSinceTeleport=" + timeSinceTeleport
                            + " fallingTicks=" + fallingTicks
                            + " flatDeltaYTicks=" + flatDeltaYTicks
                            + " nearGroundTicks=" + nearGroundTicks
                            + " fallingBuffer=" + fallingBuffer
                            + ")");
                }
            }
        } else {
            fallingBuffer = 0;
        }
    }

    /**
     * Handles when the player is on the ground.
     *
     * @param deltaY      The change in y-axis.
     * @param velocity    The player's velocity.
     * @param predictionY The predicted y-axis.
     */
    private void handleOnGround(double deltaY, double velocity, double predictionY) {
        risingTicks = 0;
        fallingTicks = 0;

        int jumpBoostLevel = MovementUtil.getPotionEffectLevel(player, PotionEffectType.JUMP);
        double maxDeltaY = 0.42 + jumpBoostLevel * 0.1;

        if ((deltaY > maxDeltaY || deltaY < -0.7171) && Math.abs(deltaY - predictionY) > 0.001) {
            // Fixes false flags when colliding with boats.
            if (playerData.isNearbyBoat(0.6, 0.6, 0.6)) {
                if (deltaY == 0.0 || deltaY == 0.6000000238418579) {
                    return;
                }
            }

            flag(true, "Rising, Near Ground"
                    + " (deltaY=" + deltaY
                    + " velocity=" + velocity + " |"
                    + " predictionY=" + predictionY
                    + " maxDeltaY=" + maxDeltaY + ")");
        }
    }
}
