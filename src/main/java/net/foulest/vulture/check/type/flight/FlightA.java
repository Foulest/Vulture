package net.foulest.vulture.check.type.flight;

import lombok.NonNull;
import net.foulest.vulture.action.ActionType;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.event.MovementEvent;
import net.foulest.vulture.util.MessageUtil;
import net.foulest.vulture.util.MovementUtil;
import net.foulest.vulture.util.block.BlockUtil;
import org.bukkit.GameMode;
import org.bukkit.potion.PotionEffectType;

@CheckInfo(name = "Flight (A)", type = CheckType.FLIGHT,
        description = "Checks for invalid y-axis movement when falling.")
public class FlightA extends Check {

    private static final double GRAVITY_DECAY = 0.08;
    private static final double GRAVITY_MULTIPLIER = 0.9800000190735147;
    private static final double ON_GROUND_VELOCITY = -0.0784000015258789;

    private double lastDeltaY;
    private double lastVelocity;

    private int nearGroundTicks;
    private int notNearGroundTicks;
    private int onGroundTicks;
    private int notOnGroundTicks;
    private int flatDeltaYTicks;

    public FlightA(@NonNull PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(@NonNull MovementEvent event, long timestamp) {
        double deltaY = event.getDeltaY();
        double velocity = player.getVelocity().getY();
        double takenVelocity = playerData.getVelocityY();
        double lastTakenVelocity = playerData.getLastVelocityY();

        long timeSinceEnterVehicle = playerData.getTimeSince(ActionType.ENTER_VEHICLE);
        long timeSinceSteerVehicle = playerData.getTimeSince(ActionType.STEER_VEHICLE);
        long timeSinceLeaveVehicle = playerData.getTimeSince(ActionType.LEAVE_VEHICLE);

        // Checks the player for exemptions.
        if (player.getGameMode() == GameMode.CREATIVE
                || player.getGameMode() == GameMode.SPECTATOR
                || playerData.isInLiquid()
                || BlockUtil.isPlayerInUnloadedChunk(player)
                || BlockUtil.isLocationInUnloadedChunk(event.getToLocation())
                || BlockUtil.isLocationInUnloadedChunk(event.getFromLocation())
                || playerData.getTimeSince(ActionType.LEAVE_VEHICLE) <= 6L
                || event.isTeleport(playerData)) {
            setLastValues(deltaY, velocity);
            return;
        }

        boolean onGround = playerData.isOnGround();
        boolean nearGround = playerData.isNearGround();
        boolean underBlock = playerData.isUnderBlock();
        boolean againstBlock = playerData.isAgainstBlock();
        boolean insideBlock = playerData.isInsideBlock();
        boolean nearClimbable = playerData.isNearClimbable();
        boolean nearStairs = playerData.isNearStairs();

        nearGroundTicks = nearGround ? nearGroundTicks + 1 : 0;
        notNearGroundTicks = !nearGround ? notNearGroundTicks + 1 : 0;
        onGroundTicks = onGround ? onGroundTicks + 1 : 0;
        notOnGroundTicks = !onGround ? notOnGroundTicks + 1 : 0;
        flatDeltaYTicks = deltaY == 0.0 ? flatDeltaYTicks + 1 : 0;

        double toY = event.getTo().getPosition().getY();
        double fromY = event.getFrom().getPosition().getY();
        double jumpY = (0.41999998688697815 + (0.1 * MovementUtil.getPotionEffectLevel(player, PotionEffectType.JUMP)));
        double threshold = 0.003017;

        // Predictions
        double predVelocityY = (lastDeltaY - GRAVITY_DECAY) * GRAVITY_MULTIPLIER;
        double predVelocityVel = (lastVelocity - GRAVITY_DECAY) * GRAVITY_MULTIPLIER;
        double predDeltaY = (lastDeltaY - GRAVITY_DECAY) * GRAVITY_MULTIPLIER;

        // Y Differences
        double diffYPredY = Math.abs(deltaY - predDeltaY);
        double diffYPredVY = Math.abs(deltaY - predVelocityY);
        double diffYPredVV = Math.abs(deltaY - predVelocityVel);
        double diffYLastY = Math.abs(deltaY - lastDeltaY);
        double diffYLastV = Math.abs(deltaY - lastVelocity);
        double diffYTakenV = Math.abs(deltaY - takenVelocity);
        double diffYGravity = Math.abs(deltaY - (GRAVITY_MULTIPLIER * -0.10));
        double diffYGroundV = Math.abs(deltaY - ON_GROUND_VELOCITY);

        // Velocity Differences
        double diffVPredY = Math.abs(velocity - predDeltaY);
        double diffVLastY = Math.abs(velocity - lastDeltaY);
        double diffVLastV = Math.abs(velocity - lastVelocity);
        double diffVPredVY = Math.abs(velocity - predVelocityY);
        double diffVPredVV = Math.abs(velocity - predVelocityVel);
        double diffVTakenV = Math.abs(velocity - takenVelocity);
        double diffVRaw = Math.abs(velocity - deltaY);

        // Checks for invalid y-axis movement when falling.
        if (velocity != ON_GROUND_VELOCITY && velocity != lastVelocity && diffYPredY > threshold) {
            // Ignores players who are on stairs.
            if (nearStairs && deltaY == 0.5) {
                MessageUtil.debug("FlightA: " + player.getName() + " failed (Stairs) (Y=" + deltaY + ")");
                setLastValues(deltaY, velocity);
                return;
            }

            // Ignores players who are near a climbable.
            if (nearClimbable) {
                if (deltaY >= -0.15000000000000568 && deltaY <= 0.11760000228882461) {
                    MessageUtil.debug("FlightA: " + player.getName() + " failed (A1) (Y=" + deltaY + ")");
                    setLastValues(deltaY, velocity);
                    return;
                }

                if (diffVPredY < threshold && nearGroundTicks == 1) {
                    MessageUtil.debug("FlightA: " + player.getName() + " failed (A2) (Y=" + deltaY + ")");
                    setLastValues(deltaY, velocity);
                    return;
                }
            }

            // Ignores players jumping under blocks.
            if (underBlock && (deltaY + (fromY - (int) fromY)) - 0.20000004768 < threshold) {
                MessageUtil.debug("FlightA: " + player.getName() + " failed (B1) (Y=" + deltaY + ")");
                setLastValues(deltaY, velocity);
                return;
            }

            // ??? (This is a quirk of the movement system.)
            if (diffYGroundV < threshold) {
                if (diffYLastV < threshold) {
                    MessageUtil.debug("FlightA: " + player.getName() + " failed (C1) (Y=" + deltaY + ")");
                    setLastValues(deltaY, velocity);
                    return;
                }

                if (diffVPredY < threshold) {
                    MessageUtil.debug("FlightA: " + player.getName() + " failed (C2) (Y=" + deltaY + ")");
                    setLastValues(deltaY, velocity);
                    return;
                }

                if (diffYPredVV < threshold) {
                    MessageUtil.debug("FlightA: " + player.getName() + " failed (C3) (Y=" + deltaY + ")");
                    setLastValues(deltaY, velocity);
                    return;
                }

                if (timeSinceLeaveVehicle <= 72L) {
                    MessageUtil.debug("FlightA: " + player.getName() + " failed (C4) (Y=" + deltaY + ")");
                    setLastValues(deltaY, velocity);
                    return;
                }

                if (notOnGroundTicks == 1) {
                    MessageUtil.debug("FlightA: " + player.getName() + " failed (C5) (Y=" + deltaY + ")");
                    setLastValues(deltaY, velocity);
                    return;
                }
            }

            // Ignores players stuck falling inside a block.
            if (insideBlock && deltaY <= 0.0 && Math.abs(deltaY) <= 0.07840000152587834) {
                MessageUtil.debug("FlightA: " + player.getName() + " failed (D1) (Y=" + deltaY + ")");
                setLastValues(deltaY, velocity);
                return;
            }

            // Ignores players who were recently teleported.
            if (velocity == 0.0 || lastVelocity == 0.0) {
                MessageUtil.debug("FlightA: " + player.getName() + " failed (E1) (Y=" + deltaY + ")");
                setLastValues(deltaY, velocity);
                return;
            }

            // Ignores players taking the correct velocity given to them.
            if ((takenVelocity != 0.0 || lastTakenVelocity != 0.0) && diffYTakenV < threshold) {
                MessageUtil.debug("FlightA: " + player.getName() + " failed (F1) (Y=" + deltaY + ")");
                setLastValues(deltaY, velocity);
                return;
            }

            // Ignores players who are logging in and loading chunks.
            if (diffYGravity == 0.0 && (diffVLastV == 0.0 || diffYLastY == 0.0)) {
                MessageUtil.debug("FlightA: " + player.getName() + " failed (G1) (Y=" + deltaY + ")");
                setLastValues(deltaY, velocity);
                return;
            }

            // Handles players who just left the ground.
            if (!event.isYLevel(toY) && event.isYLevel(fromY)) {
                // Ignores players jumping in the air.
                if (Math.abs(deltaY - jumpY) < threshold) {
                    if (nearGround) {
                        MessageUtil.debug("FlightA: " + player.getName() + " failed (H1) (Y=" + deltaY + ")");
                        setLastValues(deltaY, velocity);
                        return;
                    }

                    if (diffVLastV < threshold) {
                        MessageUtil.debug("FlightA: " + player.getName() + " failed (H2) (Y=" + deltaY + ")");
                        setLastValues(deltaY, velocity);
                        return;
                    }

                    if (notNearGroundTicks == 1) {
                        MessageUtil.debug("FlightA: " + player.getName() + " failed (H3) (Y=" + deltaY + ")");
                        setLastValues(deltaY, velocity);
                        return;
                    }
                }
            }

            // Handles players who just landed on the ground.
            if (event.isYLevel(toY) && !event.isYLevel(fromY)) {
                // Ignores players landing on the ground.
                if (deltaY < 0.0 && !event.isYLevel(deltaY)) {
                    MessageUtil.debug("FlightA: " + player.getName() + " failed (I1) (Y=" + deltaY + ")");
                    setLastValues(deltaY, velocity);
                    return;
                }

                // Ignores players landing on the ground.
                if (!event.isYLevel(deltaY) && onGround) {
                    MessageUtil.debug("FlightA: " + player.getName() + " failed (I2) (Y=" + deltaY + ")");
                    setLastValues(deltaY, velocity);
                    return;
                }

                // ??? (This is a quirk of the movement system.)
                if (deltaY > 0.0 && !event.isYLevel(deltaY) && diffVLastY < threshold) {
                    MessageUtil.debug("FlightA: " + player.getName() + " failed (I3) (Y=" + deltaY + ")");
                    setLastValues(deltaY, velocity);
                    return;
                }
            }

            // Handles players who should be on the ground.
            if (event.isYLevel(toY) && event.isYLevel(fromY)) {
                // Ignores players on the ground.
                if (deltaY == 0.0) {
                    if (onGround) {
                        MessageUtil.debug("FlightA: " + player.getName() + " failed (J1) (Y=" + deltaY + ")");
                        setLastValues(deltaY, velocity);
                        return;
                    }

                    if (diffVLastV == 0.0) {
                        MessageUtil.debug("FlightA: " + player.getName() + " failed (J2) (Y=" + deltaY + ")");
                        setLastValues(deltaY, velocity);
                        return;
                    }

                    if (lastDeltaY != 0.0) {
                        MessageUtil.debug("FlightA: " + player.getName() + " failed (J3) (Y=" + deltaY + ")");
                        setLastValues(deltaY, velocity);
                        return;
                    }
                }
            }

            // Handles players who are in the air.
            if (!event.isYLevel(toY) && !event.isYLevel(fromY)) {
                if (againstBlock && diffYPredY < 0.016 && diffYPredVY < 0.016 && diffVLastY < 0.016) {
                    MessageUtil.debug("FlightA: " + player.getName() + " failed (K1) (Y=" + deltaY + ")");
                    setLastValues(deltaY, velocity);
                    return;
                }

                if (againstBlock && notNearGroundTicks == 1) {
                    MessageUtil.debug("FlightA: " + player.getName() + " failed (K2) (Y=" + deltaY + ")");
                    setLastValues(deltaY, velocity);
                    return;
                }
            }

            flag(true, "deltaY=" + deltaY
                    + " lastDeltaY=" + lastDeltaY
                    + " predDeltaY=" + predDeltaY
                    + " |"
                    + " velocity=" + velocity
                    + " lastVelocity=" + lastVelocity
                    + " predVelocityY=" + predVelocityY
                    + " predVelocityVel=" + predVelocityVel
                    + " |"
                    + " takenVelocity=" + takenVelocity
                    + " lastTakenVelocity=" + lastTakenVelocity
                    + " |"
                    + " YPredY=" + diffYPredY
                    + " YLastY=" + diffYLastY
                    + " YLastV=" + diffYLastV
                    + " YPredVY=" + diffYPredVY
                    + " YPredVV=" + diffYPredVV
                    + " YTakenV=" + diffYTakenV
                    + " YGravity=" + diffYGravity
                    + " YGroundV=" + diffYGroundV
                    + " |"
                    + " VPredY=" + diffVPredY
                    + " VLastY=" + diffVLastY
                    + " VLastV=" + diffVLastV
                    + " VTakenV=" + diffVTakenV
                    + " VRaw=" + diffVRaw
                    + " |"
                    + " 0.3=" + BlockUtil.isOnGroundOffset(player, 0.3)
                    + " 0.4=" + BlockUtil.isOnGroundOffset(player, 0.4)
                    + " 0.5=" + BlockUtil.isOnGroundOffset(player, 0.5)
                    + " 0.Y=" + BlockUtil.isOnGroundOffset(player, Math.abs(deltaY) + 0.001)
                    + " |"
                    + " nearGround=" + nearGroundTicks
                    + " notNearGround=" + notNearGroundTicks
                    + " onGround=" + onGroundTicks
                    + " notOnGround=" + notOnGroundTicks
                    + " flatDeltaY=" + flatDeltaYTicks
                    + " |"
                    + " enterVehicle=" + timeSinceEnterVehicle
                    + " steerVehicle=" + timeSinceSteerVehicle
                    + " leaveVehicle=" + timeSinceLeaveVehicle
                    + " |"
                    + " underBlock=" + underBlock
                    + " againstBlock=" + againstBlock
                    + " insideBlock=" + insideBlock
                    + " nearClimbable=" + playerData.isNearClimbable()
                    + " onClimbable=" + playerData.isOnClimbable()
                    + " nearStairs=" + nearStairs
                    + " |"
                    + " toY=" + toY
                    + " fromY=" + fromY
                    + " threshold=" + threshold
            );
        }

        setLastValues(deltaY, velocity);
    }

    public void setLastValues(double lastDeltaY, double lastVelocity) {
        this.lastDeltaY = lastDeltaY;
        this.lastVelocity = lastVelocity;
    }
}
