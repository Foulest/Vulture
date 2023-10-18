package net.foulest.vulture.check.type.flight;

import io.github.retrooper.packetevents.packetwrappers.play.in.flying.WrappedPacketInFlying;
import io.github.retrooper.packetevents.utils.vector.Vector3d;
import lombok.NonNull;
import net.foulest.vulture.action.ActionType;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.event.MovementEvent;
import net.foulest.vulture.util.GhostBlockUtil;
import net.foulest.vulture.util.MovementUtil;
import org.bukkit.GameMode;
import org.bukkit.potion.PotionEffectType;

@CheckInfo(name = "Flight (A)", type = CheckType.FLIGHT, maxViolations = 25,
        description = "Checks for invalid y-axis movement.")
public class FlightA extends Check {

    private double deltaYBufferRising;
    private double velocityBufferRising;

    private double deltaYBufferFalling;
    private double velocityBufferFalling;

    private double lastDeltaY;
    private double lastVelocityY;

    public FlightA(@NonNull PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(@NonNull MovementEvent event, long timestamp) {
        WrappedPacketInFlying to = event.getTo();
        WrappedPacketInFlying from = event.getFrom();

        Vector3d toPosition = to.getPosition();
        Vector3d fromPosition = from.getPosition();

        if (player.getGameMode() == GameMode.CREATIVE
                || player.getGameMode() == GameMode.SPECTATOR
                || playerData.getTimeSince(ActionType.STEER_VEHICLE) < 500L
                || playerData.isTeleporting(toPosition)) {
            return;
        }

        int jumpBoostLevel = MovementUtil.getPotionEffectLevel(player, PotionEffectType.JUMP);

        double deltaY = toPosition.getY() - fromPosition.getY();
        double velocity = player.getVelocity().getY();

        boolean rising = velocity > -0.0784;
        boolean falling = velocity < 0.0 && Math.abs(velocity) > 0.0785;

        double predictionY = (lastDeltaY - 0.08) * 0.9800000190734863;
        double predictionVel = (lastVelocityY - 0.08) * 0.9800000190734863;

        double deltaYDiff = Math.abs(deltaY - predictionY);
        double velocityDiff = Math.abs(velocity - predictionY);

        double deltaYLimitFalling = 0.05;
        double velocityLimitFalling = 0.6226 + jumpBoostLevel * 0.1;

        double deltaYLimitRising = 0.24;
        double velocityLimitRising = 0.6176 + jumpBoostLevel * 0.1;

        int airTicks = playerData.getAirTicks();
        int airTicksStrict = playerData.getAirTicksStrict();

        double velocityY = playerData.getVelocityY();
        double velocityHorizontal = playerData.getVelocityHorizontal();

        boolean nearLiquid = playerData.isNearLiquid();
        boolean nearClimbable = playerData.isNearClimbable();
        boolean insideBlock = playerData.isInsideBlock();
        boolean againstBlock = playerData.isAgainstBlock();
        boolean underBlock = playerData.isUnderBlock();
        boolean onGround = playerData.isOnGround();
        boolean onGroundStrict = playerData.isOnGroundStrict();

        // Adjusts the limit if the player is near a liquid.
        if (nearLiquid) {
            deltaYLimitRising = 0.5;
        }

        // Adjusts the limit if the player is near a ladder or vine.
        if (nearClimbable) {
            deltaYLimitFalling = 0.4351;
            velocityLimitFalling = 0.47;
            deltaYLimitRising = 0.35;
        }

        // Adjusts the limit if the player is against a block.
        if (againstBlock) {
            velocityLimitFalling = 0.6818;
        }

        // Adjusts the limit if the player is taking vertical velocity.
        if (velocityY > 0.0) {
            velocityLimitFalling += velocityY;
        }

//        MessageUtil.log(Level.INFO, "deltaY=" + deltaY
//                + " velocity=" + velocity
//                + " predictionY=" + predictionY
//                + " predictionVel=" + predictionVel
//                + " deltaYDiff=" + deltaYDiff
//                + " deltaYLimitFalling=" + deltaYLimitFalling
//                + " insideBlock=" + insideBlock
//                + " nearLiquid=" + nearLiquid
//                + " nearClimbable=" + nearClimbable
//                + " onGround=" + onGround
//                + " onGroundStrict=" + onGroundStrict
//                + " airTicksStrict=" + airTicksStrict
//                + " velocityHorizontal=" + velocityHorizontal
//                + " velocityY=" + velocityY + ")");

        // Detects the player's sent deltaY values not matching our predictions when falling.
        // This check catches all modules that ignore gravity, which in turn detects flight.
        if (falling && Math.abs(predictionY) >= 0.005) {
            if (deltaYDiff >= deltaYLimitFalling
                    && airTicks > 0 && !nearLiquid
                    && !(deltaY <= 0.0 && underBlock)) {
                // Prevents false flags with ghost blocks.
                GhostBlockUtil.update(player);

                if (deltaYDiff >= 1.0 || ++deltaYBufferFalling >= 3) {
                    flag("DeltaY not matching prediction when falling"
                            + " (deltaY=" + deltaY
                            + " velocity=" + velocity
                            + " predictionY=" + predictionY
                            + " predictionVel=" + predictionVel
                            + " deltaYDiff=" + deltaYDiff
                            + " deltaYLimitFalling=" + deltaYLimitFalling
                            + " insideBlock=" + insideBlock
                            + " liquid=" + false
                            + " climbable=" + nearClimbable
                            + " onGround=" + onGround
                            + " onGroundStrict=" + onGroundStrict
                            + " ticks=" + airTicksStrict
                            + " velXZ=" + velocityHorizontal
                            + " velY=" + velocityY + ")");
                }

            } else if (velocityDiff >= velocityLimitFalling) {
                // Sometimes, players log in stuck in the floor and teleport.
                // This should detect and fix that.
                if (deltaY < 0 && airTicksStrict == 0) {
                    player.teleport(player.getLocation().clone().add(0, 0.5, 0));
                    velocityBufferFalling = 0;
                    return;
                }

                // Prevents false flags with ghost blocks.
                GhostBlockUtil.update(player);

                if (velocityDiff >= 1.051 || ++velocityBufferFalling >= 3) {
                    flag("Velocity not matching prediction when falling"
                            + " (deltaY=" + deltaY
                            + " velocity=" + velocity
                            + " predictionY=" + predictionY
                            + " predictionVel=" + predictionVel
                            + " velocityDiff=" + velocityDiff
                            + " velocityLimitFalling=" + velocityLimitFalling
                            + " insideBlock=" + insideBlock
                            + " againstBlock=" + againstBlock
                            + " liquid=" + nearLiquid
                            + " climbable=" + nearClimbable
                            + " ticks=" + airTicksStrict
                            + " velXZ=" + velocityHorizontal
                            + " velY=" + velocityY + ")");
                }
            }

        } else {
            deltaYBufferFalling = Math.max(deltaYBufferFalling - 0.5, 0);
            velocityBufferFalling = Math.max(velocityBufferFalling - 0.5, 0);
        }

        // Detects the player's sent deltaY values not matching our predictions when on ground.
        // This check catches high jump and v-clip based modules.
        if (airTicksStrict == 0 && !rising && !falling) {
            double maxDeltaY = 0.42 + jumpBoostLevel * 0.1;

            if (deltaY > maxDeltaY || deltaY < -0.7171) {
                if (Math.abs(deltaY - predictionY) > 0.001) {
                    flag("DeltaY not matching prediction when on ground"
                            + " (deltaY=" + deltaY
                            + " maxDeltaY=" + maxDeltaY
                            + " velocity=" + velocity
                            + " predictionY=" + predictionY
                            + " predictionVel=" + predictionVel
                            + " insideBlock=" + insideBlock
                            + " liquid=" + nearLiquid
                            + " climbable=" + nearClimbable
                            + " ticks=" + airTicksStrict
                            + " velXZ=" + velocityHorizontal
                            + " velY=" + velocityY + ")");
                }
            }
        }

        // Detects the player's sent deltaY values not matching our predictions when rising.
        // This check catches most Y-port and bunny-hop based speed modules.
        if (rising && airTicksStrict > 0) {
            if (deltaYDiff >= deltaYLimitRising && !(deltaY <= 0.0 && underBlock)) {
                if (deltaYDiff >= 1.0 || ++deltaYBufferRising >= 3) {
                    flag("DeltaY not matching prediction when rising"
                            + " (deltaY=" + deltaY
                            + " velocity=" + velocity
                            + " predictionY=" + predictionY
                            + " predictionVel=" + predictionVel
                            + " deltaYDiff=" + deltaYDiff
                            + " deltaYLimitRising=" + deltaYLimitRising
                            + " insideBlock=" + insideBlock
                            + " liquid=" + nearLiquid
                            + " climbable=" + nearClimbable
                            + " ticks=" + airTicksStrict
                            + " velXZ=" + velocityHorizontal
                            + " velY=" + velocityY + ")");
                }

            } else if (velocityDiff >= velocityLimitRising && velocity != 0.0) {
                if (velocityDiff >= 1.0 || ++velocityBufferRising >= 3) {
                    flag("Velocity not matching prediction when rising"
                            + " (deltaY=" + deltaY
                            + " velocity=" + velocity
                            + " predictionY=" + predictionY
                            + " predictionVel=" + predictionVel
                            + " velocityDiff=" + velocityDiff
                            + " velocityLimitRising=" + velocityLimitRising
                            + " insideBlock=" + insideBlock
                            + " liquid=" + nearLiquid
                            + " climbable=" + nearClimbable
                            + " ticks=" + airTicksStrict
                            + " velXZ=" + velocityHorizontal
                            + " velY=" + velocityY + ")");
                }
            }

        } else {
            deltaYBufferRising = Math.max(deltaYBufferRising - 0.5, 0);
            velocityBufferRising = Math.max(velocityBufferRising - 0.5, 0);
        }

        lastDeltaY = deltaY;
        lastVelocityY = velocity;
    }
}
