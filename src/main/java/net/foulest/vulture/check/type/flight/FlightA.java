package net.foulest.vulture.check.type.flight;

import lombok.NonNull;
import net.foulest.vulture.action.ActionType;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.event.MovementEvent;
import net.foulest.vulture.util.block.BlockUtil;
import org.bukkit.GameMode;

@CheckInfo(name = "Flight (A)", type = CheckType.FLIGHT,
        description = "Checks for invalid y-axis movement when falling.")
public class FlightA extends Check {

    private static final double GRAVITY_DECAY = 0.08;
    private static final double GRAVITY_MULTIPLIER = 0.9800000190734863;
    private static final double ON_GROUND_VELOCITY = -0.0784000015258789;

    private double lastDeltaY;
    private double lastVelocity;

    private int offGroundTicks;
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

        boolean nearGround = playerData.isNearGround();
        nearGroundTicks = nearGround ? nearGroundTicks + 1 : 0;
        offGroundTicks = !nearGround ? offGroundTicks + 1 : 0;
        flatDeltaYTicks = deltaY == 0.0 ? flatDeltaYTicks + 1 : 0;

        double toY = event.getTo().getPosition().getY();
        double fromY = event.getFrom().getPosition().getY();

        // Tries to predict the player's next Y value.
        // This is 99.99% accurate when the player is falling.
        double predictionY = (lastDeltaY - GRAVITY_DECAY) * GRAVITY_MULTIPLIER;
        double predictionDiffY = Math.abs(deltaY - predictionY);
        double predictionDiffVel = Math.abs(velocity - predictionY);

        // If the player is no longer on the ground, start checking.
        if (velocity != ON_GROUND_VELOCITY) {
            double threshold = 0.001;
            threshold += (deltaY == 0.0 ? 0.002017 : 0.0);
            threshold += (playerData.isUnderBlock() ? 0.20 : 0.0);

            if (predictionDiffY > threshold) {
                // Fixes a strange false flag.
                if (predictionDiffVel < 0.05) {
                    return;
                }

                // Player landed, ignore.
                if (event.isYLevel(toY) && !event.isYLevel(fromY)) {
                    return;
                }

                flag(false, "deltaY=" + deltaY
                        + " lastDeltaY=" + lastDeltaY
                        + " predictionY=" + predictionY
                        + " predictionDiffY=" + predictionDiffY
                        + " predictionDiffVel=" + predictionDiffVel
                        + " velocity=" + velocity
                        + " nearGroundTicks=" + nearGroundTicks
                        + " offGroundTicks=" + offGroundTicks
                        + " flatDeltaYTicks=" + flatDeltaYTicks
                        + " toY=" + event.getTo().getPosition().getY()
                        + " fromY=" + event.getFrom().getPosition().getY()
                        + " 0.3=" + BlockUtil.isOnGroundOffset(player, 0.3)
                        + " 0.4=" + BlockUtil.isOnGroundOffset(player, 0.4)
                        + " 0.5=" + BlockUtil.isOnGroundOffset(player, 0.5)
                        + " 0.6=" + BlockUtil.isOnGroundOffset(player, 0.6)
                        + " 0.7=" + BlockUtil.isOnGroundOffset(player, 0.7)
                        + " 0.8=" + BlockUtil.isOnGroundOffset(player, 0.8)
                        + " 0.9=" + BlockUtil.isOnGroundOffset(player, 0.9)
                        + " 1.0=" + BlockUtil.isOnGroundOffset(player, 1.0)
                );
            } else {
                debug("deltaY=" + deltaY
                        + " lastDeltaY=" + lastDeltaY
                        + " predictionY=" + predictionY
                        + " predictionDiffY=" + predictionDiffY
                        + " predictionDiffVel=" + predictionDiffVel
                        + " velocity=" + velocity
                        + " nearGroundTicks=" + nearGroundTicks
                        + " offGroundTicks=" + offGroundTicks
                        + " flatDeltaYTicks=" + flatDeltaYTicks
                        + " toY=" + event.getTo().getPosition().getY()
                        + " fromY=" + event.getFrom().getPosition().getY()
                        + " 0.3=" + BlockUtil.isOnGroundOffset(player, 0.3)
                        + " 0.4=" + BlockUtil.isOnGroundOffset(player, 0.4)
                        + " 0.5=" + BlockUtil.isOnGroundOffset(player, 0.5)
                        + " 0.6=" + BlockUtil.isOnGroundOffset(player, 0.6)
                        + " 0.7=" + BlockUtil.isOnGroundOffset(player, 0.7)
                        + " 0.8=" + BlockUtil.isOnGroundOffset(player, 0.8)
                        + " 0.9=" + BlockUtil.isOnGroundOffset(player, 0.9)
                        + " 1.0=" + BlockUtil.isOnGroundOffset(player, 1.0)
                );
            }
        }

        lastDeltaY = deltaY;
        lastVelocity = velocity;
    }
}
