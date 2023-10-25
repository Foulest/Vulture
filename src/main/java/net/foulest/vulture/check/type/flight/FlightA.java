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

@CheckInfo(name = "Flight (A)", type = CheckType.FLIGHT, maxViolations = 25,
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
                || event.isTeleport(playerData)) {
            lastDeltaY = deltaY;
            lastVelocity = velocity;
            return;
        }

        double predictionY = (lastDeltaY - GRAVITY_DECAY) * GRAVITY_MULTIPLIER;

        boolean rising = velocity > ON_GROUND_VELOCITY;
        boolean falling = velocity < ON_GROUND_VELOCITY;
        boolean onGround = BlockUtil.isOnGroundOffset(player, 0.105);

        if (rising) {
            handleRising(onGround, deltaY, velocity, predictionY);
        } else if (falling) {
            handleFalling(onGround, deltaY, velocity, predictionY);
        } else if (onGround) {
            handleOnGround(deltaY, velocity, predictionY);
        }

        lastDeltaY = deltaY;
        lastVelocity = velocity;
    }

    /**
     * Handles when the player is falling.
     *
     * @param onGround    Whether the player is on the ground.
     * @param deltaY      The change in y-axis.
     * @param velocity    The player's velocity.
     * @param predictionY The predicted y-axis.
     */
    private void handleFalling(boolean onGround, double deltaY, double velocity, double predictionY) {
        risingTicks = 0;

        if (onGround) {
            fallingTicks = 0;
        } else {
            ++fallingTicks;
        }

        double predictionVel = ON_GROUND_VELOCITY;

        // Optimize this loop to avoid unnecessary calculations
        double subtractedValue = GRAVITY_DECAY * fallingTicks;
        double multiplier = Math.pow(GRAVITY_MULTIPLIER, fallingTicks);
        predictionVel = (predictionVel - subtractedValue) * multiplier;

        double predictionNextVel = (predictionVel - GRAVITY_DECAY) * GRAVITY_MULTIPLIER;
        double predictionDiffY = Math.abs(deltaY - predictionY);
        double predictionDiffVel = Math.abs(deltaY - predictionVel);
        double predictionDiffLastVel = Math.abs(deltaY - lastVelocity);
        double predictionDiffNextVel = Math.abs(deltaY - predictionNextVel);

        debug("Falling"
                + " deltaY=" + deltaY
                + " velocity=" + velocity + " |"
                + " predictionY=" + predictionY
                + " predictionVel=" + predictionVel + " |"
                + " predictionDiffY=" + predictionDiffY
                + " predictionDiffVel=" + predictionDiffVel
                + " predictionDiffLastVel=" + predictionDiffLastVel
                + " predictionDiffNextVel=" + predictionDiffNextVel + " |"
                + " fallingTicks=" + fallingTicks + ")");

        if (predictionDiffY > 0.001 && (predictionDiffVel > 0.001
                && predictionDiffLastVel > 0.001 && predictionDiffNextVel > 0.001)) {

            if (predictionDiffVel > 0.30 && predictionDiffLastVel > 0.30 && predictionDiffNextVel > 0.30) {
                if (++fallingBuffer >= 2) {
                    flag(true, "Falling"
                            + " deltaY=" + deltaY
                            + " velocity=" + velocity + " |"
                            + " predictionY=" + predictionY
                            + " predictionVel=" + predictionVel + " |"
                            + " predictionDiffY=" + predictionDiffY
                            + " predictionDiffVel=" + predictionDiffVel
                            + " predictionDiffLastVel=" + predictionDiffLastVel
                            + " predictionDiffNextVel=" + predictionDiffNextVel + " |"
                            + " fallingTicks=" + fallingTicks + ")");
                }
            }
        } else {
            fallingBuffer = 0;
        }
    }

    /**
     * Handles when the player is rising.
     *
     * @param onGround    Whether the player is on the ground.
     * @param deltaY      The change in y-axis.
     * @param velocity    The player's velocity.
     * @param predictionY The predicted y-axis.
     */
    private void handleRising(boolean onGround, double deltaY, double velocity, double predictionY) {
        fallingTicks = 0;

        if (onGround) {
            risingTicks = 0;
        } else {
            ++risingTicks;
        }

        double predictionVel = lastDeltaY;

        // Optimize this loop to avoid unnecessary calculations
        double subtractedValue = GRAVITY_DECAY * risingTicks;
        double multiplier = Math.pow(GRAVITY_MULTIPLIER, risingTicks);
        predictionVel = (predictionVel - subtractedValue) * multiplier;

        double predictionNextVel = (predictionVel - GRAVITY_DECAY) * GRAVITY_MULTIPLIER;
        double predictionDiffY = Math.abs(deltaY - predictionY);
        double predictionDiffNextVel = Math.abs(deltaY - predictionNextVel);

        if (predictionDiffY > 0.001 && predictionDiffNextVel > 0.15) {
            if (onGround) {
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

                if (++risingInAirBuffer >= 2 && risingTicks >= 2) {
                    flag(true, "Rising, In Air"
                            + " (deltaY=" + deltaY
                            + " velocity=" + velocity + " |"
                            + " predictionY=" + predictionY
                            + " predictionVel=" + predictionVel + " |"
                            + " predictionDiffY=" + predictionDiffY
                            + " predictionDiffNextVel=" + predictionDiffNextVel + " |"
                            + " risingTicks=" + risingTicks + ")");
                }
            }
        } else {
            risingInAirBuffer = Math.max(risingInAirBuffer - 0.25, 0);
            risingNearGroundBuffer = Math.max(risingNearGroundBuffer - 0.25, 0);
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
            flag(true, "Rising, On Ground"
                    + " (deltaY=" + deltaY
                    + " velocity=" + velocity + " |"
                    + " predictionY=" + predictionY
                    + " maxDeltaY=" + maxDeltaY + ")");
        }
    }
}
