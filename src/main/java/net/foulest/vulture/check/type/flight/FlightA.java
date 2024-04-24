package net.foulest.vulture.check.type.flight;

import io.github.retrooper.packetevents.utils.player.ClientVersion;
import net.foulest.vulture.action.ActionType;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.event.MovementEvent;
import net.foulest.vulture.util.BlockUtil;
import net.foulest.vulture.util.MessageUtil;
import net.foulest.vulture.util.MovementUtil;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

@CheckInfo(name = "Flight (A)", type = CheckType.FLIGHT,
        description = "Checks for invalid y-axis movement when falling.")
public class FlightA extends Check {

    // TODO: Patch players moving through blocks using Phase sand modules.
    // TODO: Test slime block predictions thoroughly.

    private static final double GRAVITY_DECAY = 0.08;
    private static final double GRAVITY_MULTIPLIER = 0.9800000190735147;
    private static final double ON_GROUND_VELOCITY = -0.0784000015258789;

    private double lastDeltaY;
    private double lastVelocity;

    private double buffer;

    private int nearGroundTicks;
    private int notNearGroundTicks;
    private int onGroundTicks;
    private int notOnGroundTicks;
    private int flatDeltaYTicks;

    public FlightA(PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(@NotNull MovementEvent event, long timestamp) {
        double deltaY = event.getDeltaY();
        double velocity = player.getVelocity().getY();
        double takenVelocity = playerData.getVelocityY();
        double lastTakenVelocity = playerData.getLastVelocityY();

        // Checks the player for exemptions.
        if (playerData.isFlying()
                || playerData.getTicksSince(ActionType.LEAVE_VEHICLE) <= 1
                || playerData.getTicksSince(ActionType.STOP_FLYING) <= 4
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
        double thresholdJump = threshold + (0.1 * MovementUtil.getPotionEffectLevel(player, PotionEffectType.JUMP));

        // Predictions
        double predVelocity = (lastVelocity - GRAVITY_DECAY) * GRAVITY_MULTIPLIER;
        double predDeltaY = (lastDeltaY - GRAVITY_DECAY) * GRAVITY_MULTIPLIER;

        // Y Differences
        double diffYPredY = Math.abs(deltaY - predDeltaY);
        double diffYPredV = Math.abs(deltaY - predVelocity);
        double diffYLastY = Math.abs(deltaY - lastDeltaY);
        double diffYLastV = Math.abs(deltaY - lastVelocity);
        double diffYTakenV = Math.abs(deltaY - takenVelocity);
        double diffYLastTakenV = Math.abs(deltaY - lastTakenVelocity);
        double diffYGravity = Math.abs(deltaY - (GRAVITY_MULTIPLIER * -0.10));
        double diffYGroundV = Math.abs(deltaY - ON_GROUND_VELOCITY);

        // Velocity Differences
        double diffVPredY = Math.abs(velocity - predDeltaY);
        double diffVLastY = Math.abs(velocity - lastDeltaY);
        double diffVLastV = Math.abs(velocity - lastVelocity);
        double diffVTakenV = Math.abs(velocity - takenVelocity);
        double diffVLastTakenV = Math.abs(velocity - lastTakenVelocity);
        double diffVRaw = Math.abs(velocity - deltaY);

        // Ignores players who are in liquid.
        if (playerData.isInLiquid() && deltaY <= jumpY) {
            setLastValues(deltaY, velocity);
            return;
        }

        // Ignores players who are under the effects of slime blocks.
        // Note: The effects of slime blocks are entirely done client-side.
        if (playerData.isUnderEffectOfSlime()
                && playerData.isTouchedGroundSinceLogin()) {
            boolean nearGroundSlime = BlockUtil.isOnGroundOffset(player, 0.75);
            double deltaYChange = deltaY - lastDeltaY;

            if ((diffYPredY > 0.1 || diffYLastY < threshold) && diffVRaw >= 0.1 && diffYLastV >= 0.1) {
                if (nearGroundSlime && deltaY != 0.0 && lastDeltaY <= 0.0) {
                    if (Math.abs(Math.abs(deltaY) - Math.abs(lastDeltaY)) <= 0.1) {
                        MessageUtil.debug("FlightA: ignoring movement for " + player.getName() + " (Slime #1) (Y=" + deltaY + ")");
                        setLastValues(deltaY, velocity);
                        return;
                    }

                    if (velocity > 0.0 && lastVelocity < 0.0) {
                        MessageUtil.debug("FlightA: ignoring movement for " + player.getName() + " (Slime #2) (Y=" + deltaY + ")");
                        setLastValues(deltaY, velocity);
                        return;
                    }

                    if (deltaY < 0.0 && velocity < 0.0 && lastVelocity < 0.0) {
                        MessageUtil.debug("FlightA: ignoring movement for " + player.getName() + " (Slime #3) (Y=" + deltaY + ")");
                        setLastValues(deltaY, velocity);
                        return;
                    }

                    if (onGround && Math.abs(deltaY - jumpY) < threshold) {
                        MessageUtil.debug("FlightA: ignoring movement for " + player.getName() + " (Slime #4) (Y=" + deltaY + ")");
                        setLastValues(deltaY, velocity);
                        return;
                    }

                    if (deltaY > 0.0 && velocity > deltaY && lastVelocity > 0.0) {
                        MessageUtil.debug("FlightA: ignoring movement for " + player.getName() + " (Slime #5) (Y=" + deltaY + ")");
                        setLastValues(deltaY, velocity);
                        return;
                    }

                    if (velocity == ON_GROUND_VELOCITY) {
                        MessageUtil.debug("FlightA: ignoring movement for " + player.getName() + " (Slime #6) (Y=" + deltaY + ")");
                        setLastValues(deltaY, velocity);
                        return;
                    }
                }

                if (deltaY < 0.0 && lastDeltaY > 0.0 && velocity < 0.0 && lastVelocity < 0.0) {
                    MessageUtil.debug("FlightA: ignoring movement for " + player.getName() + " (Slime #7) (Y=" + deltaY + ")");
                    setLastValues(deltaY, velocity);
                    return;
                }

                if (velocity < 0.0 && lastVelocity > 0.0 && deltaY < 0.0 && lastDeltaY > 0.0) {
                    MessageUtil.debug("FlightA: ignoring movement for " + player.getName() + " (Slime #8) (Y=" + deltaY + ")");
                    setLastValues(deltaY, velocity);
                    return;
                }

                if (velocity < 0.0 && Math.abs(velocity) > 1.0 && lastVelocity < 0.0
                        && deltaY < 0.0 && Math.abs(deltaY) < 1.0 && lastDeltaY < 0.0) {
                    MessageUtil.debug("FlightA: ignoring movement for " + player.getName() + " (Slime #9) (Y=" + deltaY + ")");
                    setLastValues(deltaY, velocity);
                    return;
                }

                if (deltaY == 0.0 && onGround) {
                    MessageUtil.debug("FlightA: ignoring movement for " + player.getName() + " (Slime #10) (Y=" + deltaY + ")");
                    setLastValues(deltaY, velocity);
                    return;
                }

                flag(true, "y=" + deltaY
                        + " v=" + velocity
                        + " lastY=" + lastDeltaY
                        + " lastV=" + lastVelocity
                        + " yChange=" + deltaYChange
                        + " yPredY=" + diffYPredY
                        + " vRaw=" + diffVRaw
                        + " yLastV=" + diffYLastV
                        + " onGround=" + onGround
                        + " nearGround=" + nearGroundSlime
                );
            }

            setLastValues(deltaY, velocity);
            return;
        }

        // Checks for invalid y-axis movement when stepping up a block.
        if (Math.abs(lastDeltaY - jumpY) < threshold && playerData.getTicksSince(ActionType.DAMAGE) > 10) {
            double diff = (deltaY - (0.33319999363422426
                    + (0.1 * MovementUtil.getPotionEffectLevel(player, PotionEffectType.JUMP)))
                    + (0.002 * MovementUtil.getPotionEffectLevel(player, PotionEffectType.JUMP)));

            if (Math.abs(diff) >= 0.00000001 && Math.abs(diff) <= 0.001) {
                flag(true, "deltaY=" + deltaY + " diff=" + diff);
            }
        }

        // Checks if the predicted Y difference is greater than the threshold.
        if (diffYPredY > thresholdJump) {
            // Fixes false flags when colliding with boats.
            if (playerData.isNearbyBoat(0.6, 0.6, 0.6)
                    && ((deltaY == 0.0 && lastDeltaY == 0.0) || deltaY == 0.5625
                    || Math.abs(deltaY - jumpY) < threshold
                    || Math.abs(lastDeltaY - jumpY) < threshold)) {
                MessageUtil.debug("FlightA: ignoring movement for " + player.getName() + " (Boat) (Y=" + deltaY + ")");
                setLastValues(deltaY, velocity);
                return;
            }

            // Ignores weird falling behavior in clients above 1.8.
            if (predDeltaY - threshold < 0.001 && diffYGroundV - 0.006 < threshold && lastDeltaY > 0.0
                    && playerData.getVersion().isNewerThan(ClientVersion.v_1_8)) {
                MessageUtil.debug("FlightA: ignoring movement for " + player.getName() + " (A0) (Y=" + deltaY + ")");
                setLastValues(deltaY, velocity);
                return;
            }

            // Ignores players who are on stairs.
            if (nearStairs && deltaY == 0.5) {
                MessageUtil.debug("FlightA: ignoring movement for " + player.getName() + " (Stairs) (Y=" + deltaY + ")");
                setLastValues(deltaY, velocity);
                return;
            }

            // Ignores players who are near a climbable.
            if (nearClimbable) {
                if (deltaY >= -0.15000000596046448 && deltaY <= 0.11760000228882461) {
                    MessageUtil.debug("FlightA: ignoring movement for " + player.getName() + " (A1) (Y=" + deltaY + ")");
                    setLastValues(deltaY, velocity);
                    return;
                }

                if (diffVPredY < threshold && nearGroundTicks == 1) {
                    MessageUtil.debug("FlightA: ignoring movement for " + player.getName() + " (A2) (Y=" + deltaY + ")");
                    setLastValues(deltaY, velocity);
                    return;
                }
            }

            // Ignores players jumping under blocks.
            if (underBlock && (deltaY + (fromY - (int) fromY)) - 0.20000004768 < 0.001) {
                MessageUtil.debug("FlightA: ignoring movement for " + player.getName() + " (B1) (Y=" + deltaY + ")");
                setLastValues(deltaY, velocity);
                return;
            }

            // Ignores quirks of the movement system.
            if (diffYGroundV < threshold) {
                if (diffYLastV < threshold && playerData.getTicksSince(ActionType.DAMAGE) < 2) {
                    MessageUtil.debug("FlightA: ignoring movement for " + player.getName() + " (C1) (Y=" + deltaY + ")");
                    setLastValues(deltaY, velocity);
                    return;
                }

                if (diffVPredY < threshold) {
                    MessageUtil.debug("FlightA: ignoring movement for " + player.getName() + " (C2) (Y=" + deltaY + ")");
                    setLastValues(deltaY, velocity);
                    return;
                }

                if (diffYPredV < threshold) {
                    MessageUtil.debug("FlightA: ignoring movement for " + player.getName() + " (C3) (Y=" + deltaY + ")");
                    setLastValues(deltaY, velocity);
                    return;
                }

                if (playerData.getTicksSince(ActionType.LEAVE_VEHICLE) <= 2) {
                    MessageUtil.debug("FlightA: ignoring movement for " + player.getName() + " (C4) (Y=" + deltaY + ")");
                    setLastValues(deltaY, velocity);
                    return;
                }

                if (diffVRaw < threshold) {
                    MessageUtil.debug("FlightA: ignoring movement for " + player.getName() + " (C5) (Y=" + deltaY + ")");
                    setLastValues(deltaY, velocity);
                    return;
                }

                if (lastDeltaY - 0.20000004768 < 0.001 && notOnGroundTicks == 1) {
                    MessageUtil.debug("FlightA: ignoring movement for " + player.getName() + " (C6) (Y=" + deltaY + ")");
                    setLastValues(deltaY, velocity);
                    return;
                }

                if (playerData.getTicksSince(ActionType.TELEPORT) <= 2) {
                    MessageUtil.debug("FlightA: ignoring movement for " + player.getName() + " (C7) (Y=" + deltaY + ")");
                    setLastValues(deltaY, velocity);
                    return;
                }

                if (playerData.getVersion().isNewerThan(ClientVersion.v_1_8) && notNearGroundTicks == 1) {
                    MessageUtil.debug("FlightA: ignoring movement for " + player.getName() + " (C8) (Y=" + deltaY + ")");
                    setLastValues(deltaY, velocity);
                    return;
                }
            }

            // Ignores players stuck falling inside a block.
            if (insideBlock && deltaY <= 0.0 && Math.abs(deltaY) <= 0.07840000152587834) {
                MessageUtil.debug("FlightA: ignoring movement for " + player.getName() + " (D1) (Y=" + deltaY + ")");
                setLastValues(deltaY, velocity);
                return;
            }

            // Ignores weird after teleport behavior.
            if (deltaY == -0.07840000152587834 && diffYLastV < threshold && notOnGroundTicks == 1) {
                MessageUtil.debug("FlightA: ignoring movement for " + player.getName() + " (D2) (Y=" + deltaY + ")");
                setLastValues(deltaY, velocity);
                return;
            }

            // Ignores players who were recently teleported.
            if (deltaY == 0.0 && velocity == 0.0) {
                MessageUtil.debug("FlightA: ignoring movement for " + player.getName() + " (E1) (Y=" + deltaY + ")");
                setLastValues(deltaY, velocity);
                return;
            }

            // Ignores players taking the correct velocity given to them.
            if ((takenVelocity != 0.0 || lastTakenVelocity != 0.0) && deltaY != 0.0
                    && (diffYTakenV < threshold || diffYLastTakenV < threshold)) {
                MessageUtil.debug("FlightA: ignoring movement for " + player.getName() + " (F1) (Y=" + deltaY + ")");
                setLastValues(deltaY, velocity);
                return;
            }

            // Ignores players who are teleporting to unloaded chunks.
            if (deltaY == -0.09800000190735147
                    && (playerData.getTicksSince(ActionType.IN_UNLOADED_CHUNK) < 30
                    || playerData.getTicksSince(ActionType.TELEPORT) < 15)) {
                MessageUtil.debug("FlightA: ignoring movement for " + player.getName() + " (G1) (Y=" + deltaY + ")"
                        + " (T=" + playerData.getTicksSince(ActionType.IN_UNLOADED_CHUNK) + ")");
                setLastValues(deltaY, velocity);
                return;
            }

            if (BlockUtil.isNearBed(player) && Math.abs(deltaY) < 0.1) {
                MessageUtil.debug("FlightA: ignoring movement for " + player.getName() + " (G2) (Y=" + deltaY + ")");
                setLastValues(deltaY, velocity);
                return;
            }

            if (BlockUtil.isNearBed(player) && onGroundTicks == 1 && Math.abs(deltaY) <= 0.5625) {
                MessageUtil.debug("FlightA: ignoring movement for " + player.getName() + " (G3) (Y=" + deltaY + ")");
                setLastValues(deltaY, velocity);
                return;
            }

            // Handles players who just left the ground.
            if (!MovementUtil.isYLevel(toY) && MovementUtil.isYLevel(fromY)) {
                // Ignores players jumping in the air.
                if (Math.abs(deltaY - jumpY) < 0.00000001) {
                    if (nearGround) {
                        MessageUtil.debug("FlightA: ignoring movement for " + player.getName() + " (H1) (Y=" + deltaY + ")");
                        setLastValues(deltaY, velocity);
                        return;
                    }

                    if (diffVLastV < threshold) {
                        MessageUtil.debug("FlightA: ignoring movement for " + player.getName() + " (H2) (Y=" + deltaY + ")");
                        setLastValues(deltaY, velocity);
                        return;
                    }

                    if (notNearGroundTicks == 1) {
                        MessageUtil.debug("FlightA: ignoring movement for " + player.getName() + " (H3) (Y=" + deltaY + ")");
                        setLastValues(deltaY, velocity);
                        return;
                    }

                    if (diffVTakenV < threshold) {
                        MessageUtil.debug("FlightA: ignoring movement for " + player.getName() + " (H4) (Y=" + deltaY + ")");
                        setLastValues(deltaY, velocity);
                        return;
                    }

                    if (velocity == ON_GROUND_VELOCITY) {
                        MessageUtil.debug("FlightA: ignoring movement for " + player.getName() + " (H5) (Y=" + deltaY + ")");
                        setLastValues(deltaY, velocity);
                        return;
                    }
                }

                // Ignores players jumping under blocks.
                if ((deltaY + (toY - (int) toY)) - 0.20000004768 < 0.001) {
                    MessageUtil.debug("FlightA: ignoring movement for " + player.getName() + " (B2) (Y=" + deltaY + ")");
                    setLastValues(deltaY, velocity);
                    return;
                }
            }

            // Handles players who just landed on the ground.
            if (MovementUtil.isYLevel(toY) && !MovementUtil.isYLevel(fromY)) {
                // Ignores players jumping from a land.
                if (deltaY > 0.0 && lastDeltaY < 0.0 && toY % 1.0 == 0.0
                        && !MovementUtil.isYLevel(deltaY)
                        && !MovementUtil.isYLevel(lastDeltaY)) {
                    MessageUtil.debug("FlightA: ignoring movement for " + player.getName() + " (I1) (Y=" + deltaY + ")");
                    setLastValues(deltaY, velocity);
                    return;
                }

                // Ignores players landing on the ground.
                if (deltaY < 0.0 && !MovementUtil.isYLevel(deltaY)) {
                    MessageUtil.debug("FlightA: ignoring movement for " + player.getName() + " (I2) (Y=" + deltaY + ")");
                    setLastValues(deltaY, velocity);
                    return;
                }

                // Ignores players landing on the ground.
                if (!MovementUtil.isYLevel(deltaY) && onGround && !againstBlock) {
                    MessageUtil.debug("FlightA: ignoring movement for " + player.getName() + " (I3) (Y=" + deltaY + ")");
                    setLastValues(deltaY, velocity);
                    return;
                }

                // Ignores rising right after falling onto a block.
                if (deltaY > 0.0 && lastDeltaY < 0.0 && !MovementUtil.isYLevel(deltaY) && diffVLastY < threshold) {
                    MessageUtil.debug("FlightA: ignoring movement for " + player.getName() + " (I4) (Y=" + deltaY + ")");
                    setLastValues(deltaY, velocity);
                    return;
                }

                if (BlockUtil.isNearLilyPad(player) && Math.abs(deltaY) < 0.1) {
                    MessageUtil.debug("FlightA: ignoring movement for " + player.getName() + " (I5) (Y=" + deltaY + ")");
                    setLastValues(deltaY, velocity);
                    return;
                }
            }

            // Handles players who should be on the ground.
            if (MovementUtil.isYLevel(toY) && MovementUtil.isYLevel(fromY)) {
                // Ignores players on the ground.
                if (deltaY == 0.0) {
                    if (onGround) {
                        MessageUtil.debug("FlightA: ignoring movement for " + player.getName() + " (J1) (Y=" + deltaY + ")");
                        setLastValues(deltaY, velocity);
                        return;
                    }

                    if (diffVLastV == 0.0) {
                        MessageUtil.debug("FlightA: ignoring movement for " + player.getName() + " (J2) (Y=" + deltaY + ")");
                        setLastValues(deltaY, velocity);
                        return;
                    }

                    if (lastDeltaY != 0.0) {
                        MessageUtil.debug("FlightA: ignoring movement for " + player.getName() + " (J3) (Y=" + deltaY + ")");
                        setLastValues(deltaY, velocity);
                        return;
                    }

                    if (BlockUtil.isNearFence(player) || BlockUtil.isNearFenceGate(player)) {
                        MessageUtil.debug("FlightA: ignoring movement for " + player.getName() + " (J4) (Y=" + deltaY + ")");
                        setLastValues(deltaY, velocity);
                        return;
                    }
                }

                // Ignores players stepping on specific blocks.
                if (velocity == ON_GROUND_VELOCITY) {
                    if (deltaY == 0.125 && (BlockUtil.isNearChest(player) || BlockUtil.isNearBrewingStand(player))
                            || (BlockUtil.isNearTrapdoor(player) && BlockUtil.isNearCarpet(player))
                            || (BlockUtil.isNearSlab(player) && BlockUtil.isNearFlowerPot(player))) {
                        MessageUtil.debug("FlightA: ignoring movement for " + player.getName() + " (J5) (Y=" + deltaY + ")");
                        setLastValues(deltaY, velocity);
                        return;
                    }

                    if (deltaY % 0.125 == 0 && deltaY <= 0.5 && BlockUtil.isNearSnowLayer(player)) {
                        MessageUtil.debug("FlightA: ignoring movement for " + player.getName() + " (J6) (Y=" + deltaY + ")");
                        setLastValues(deltaY, velocity);
                        return;
                    }

                    if ((Math.abs(deltaY) == 0.015625 || Math.abs(deltaY) == 0.09375) && BlockUtil.isNearLilyPad(player)) {
                        MessageUtil.debug("FlightA: ignoring movement for " + player.getName() + " (J7) (Y=" + deltaY + ")");
                        setLastValues(deltaY, velocity);
                        return;
                    }

                    if (Math.abs(deltaY) == 0.0625 && BlockUtil.isNearCarpet(player)) {
                        MessageUtil.debug("FlightA: ignoring movement for " + player.getName() + " (J8) (Y=" + deltaY + ")");
                        setLastValues(deltaY, velocity);
                        return;
                    }

                    if (Math.abs(deltaY) == 0.5 && (BlockUtil.isNearSlab(player)
                            || BlockUtil.isNearFenceGate(player) || BlockUtil.isNearFence(player))) {
                        MessageUtil.debug("FlightA: ignoring movement for " + player.getName() + " (J9) (Y=" + deltaY + ")");
                        setLastValues(deltaY, velocity);
                        return;
                    }

                    if (Math.abs(deltaY) == 0.1875 && BlockUtil.isNearTrapdoor(player)) {
                        MessageUtil.debug("FlightA: ignoring movement for " + player.getName() + " (J10) (Y=" + deltaY + ")");
                        setLastValues(deltaY, velocity);
                        return;
                    }

                    if (Math.abs(deltaY) == 0.3125 && ((BlockUtil.isNearTrapdoor(player) && BlockUtil.isNearSlab(player))
                            || (BlockUtil.isNearCarpet(player) && BlockUtil.isNearFlowerPot(player)))) {
                        MessageUtil.debug("FlightA: ignoring movement for " + player.getName() + " (J11) (Y=" + deltaY + ")");
                        setLastValues(deltaY, velocity);
                        return;
                    }

                    if (Math.abs(deltaY) == 0.375
                            && ((BlockUtil.isNearBrewingStand(player) && BlockUtil.isNearSlab(player))
                            || BlockUtil.isNearHopper(player) || BlockUtil.isNearFlowerPot(player))) {
                        MessageUtil.debug("FlightA: ignoring movement for " + player.getName() + " (J12) (Y=" + deltaY + ")");
                        setLastValues(deltaY, velocity);
                        return;
                    }

                    if (Math.abs(deltaY) == 0.4375 && BlockUtil.isNearCarpet(player) && BlockUtil.isNearSlab(player)) {
                        MessageUtil.debug("FlightA: ignoring movement for " + player.getName() + " (J13) (Y=" + deltaY + ")");
                        setLastValues(deltaY, velocity);
                        return;
                    }

                    if (Math.abs(deltaY) == 0.5625 && BlockUtil.isNearBed(player)) {
                        MessageUtil.debug("FlightA: ignoring movement for " + player.getName() + " (J14) (Y=" + deltaY + ")");
                        setLastValues(deltaY, velocity);
                        return;
                    }
                }
            }

            // Handles players who are in the air.
            if (!MovementUtil.isYLevel(toY) && !MovementUtil.isYLevel(fromY)) {
                if (againstBlock && diffYPredY < 0.016 && diffYPredV < 0.016 && diffVLastY < 0.016) {
                    MessageUtil.debug("FlightA: ignoring movement for " + player.getName() + " (K1) (Y=" + deltaY + ")");
                    setLastValues(deltaY, velocity);
                    return;
                }

                if (againstBlock && notNearGroundTicks == 1) {
                    MessageUtil.debug("FlightA: ignoring movement for " + player.getName() + " (K2) (Y=" + deltaY + ")");
                    setLastValues(deltaY, velocity);
                    return;
                }

                if (diffYPredV < threshold && playerData.getTicksSince(ActionType.TELEPORT) <= 10) {
                    MessageUtil.debug("FlightA: ignoring movement for " + player.getName() + " (K3) (Y=" + deltaY + ")");
                    setLastValues(deltaY, velocity);
                    return;
                }

                if (diffVLastY < threshold && underBlock) {
                    MessageUtil.debug("FlightA: ignoring movement for " + player.getName() + " (K4) (Y=" + deltaY + ")");
                    setLastValues(deltaY, velocity);
                    return;
                }

                if (diffVLastY < threshold && deltaY == 0.0 && nearGround) {
                    MessageUtil.debug("FlightA: ignoring movement for " + player.getName() + " (K5) (Y=" + deltaY + ")");
                    setLastValues(deltaY, velocity);
                    return;
                }

                if (againstBlock && onGround && velocity - threshold < 0.0001) {
                    MessageUtil.debug("FlightA: ignoring movement for " + player.getName() + " (K6) (Y=" + deltaY + ")");
                    setLastValues(deltaY, velocity);
                    return;
                }

                if (diffVPredY < threshold) {
                    MessageUtil.debug("FlightA: ignoring movement for " + player.getName() + " (K7) (Y=" + deltaY + ")");
                    setLastValues(deltaY, velocity);
                    return;
                }

                if ((toY - (int) toY) - jumpY < threshold && deltaY > 0.0 && lastDeltaY < 0.0
                        && velocity == ON_GROUND_VELOCITY && diffVLastV == 0.0) {
                    MessageUtil.debug("FlightA: ignoring movement for " + player.getName() + " (K8) (Y=" + deltaY + ")");
                    setLastValues(deltaY, velocity);
                    return;
                }

                if (deltaY == 0.0 && lastDeltaY < 0.0 && velocity == lastVelocity && onGround
                        && Math.abs(lastDeltaY) - Math.abs(velocity) < threshold) {
                    MessageUtil.debug("FlightA: ignoring movement for " + player.getName() + " (K9) (Y=" + deltaY + ")");
                    setLastValues(deltaY, velocity);
                    return;
                }
            }

            if (nearGround && playerData.getTicksSince(ActionType.IN_LIQUID) < 2 && diffYPredY < 0.02) {
                MessageUtil.debug("FlightA: ignoring movement for " + player.getName() + " (L1) (Y=" + deltaY + ")");
                setLastValues(deltaY, velocity);
                return;
            }

            if (!nearGround && lastVelocity == 0.0 && deltaY > 0.0 && deltaY < 0.1 && diffYPredY < 0.1) {
                MessageUtil.debug("FlightA: ignoring movement for " + player.getName() + " (M1) (Y=" + deltaY + ")");
                setLastValues(deltaY, velocity);
                return;
            }

            if ((buffer += deltaY + 0.5) >= 1.0) {
                flag(true, "deltaY=" + deltaY
                        + " lastDeltaY=" + lastDeltaY
                        + " predDeltaY=" + predDeltaY
                        + " |"
                        + " velocity=" + velocity
                        + " lastVelocity=" + lastVelocity
                        + " predVelocity=" + predVelocity
                        + " |"
                        + " takenVelocity=" + takenVelocity
                        + " lastTakenVelocity=" + lastTakenVelocity
                        + " |"
                        + " YPredY=" + diffYPredY
                        + " YLastY=" + diffYLastY
                        + " YLastV=" + diffYLastV
                        + " YPredVV=" + diffYPredV
                        + " YTakenV=" + diffYTakenV
                        + " YGravity=" + diffYGravity
                        + " YGroundV=" + diffYGroundV
                        + " |"
                        + " VPredY=" + diffVPredY
                        + " VLastY=" + diffVLastY
                        + " VLastV=" + diffVLastV
                        + " VTakenV=" + diffVTakenV
                        + " VLastTakenV=" + diffVLastTakenV
                        + " VRaw=" + diffVRaw
                        + " |"
                        + " 0.01=" + BlockUtil.isOnGroundOffset(player, 0.01)
                        + " 0.05=" + BlockUtil.isOnGroundOffset(player, 0.05)
                        + " 0.1=" + BlockUtil.isOnGroundOffset(player, 0.1)
                        + " 0.2=" + BlockUtil.isOnGroundOffset(player, 0.2)
                        + " 0.3=" + BlockUtil.isOnGroundOffset(player, 0.3)
                        + " 0.4=" + BlockUtil.isOnGroundOffset(player, 0.4)
                        + " 0.5=" + BlockUtil.isOnGroundOffset(player, 0.5)
                        + " |"
                        + " nearGround=" + nearGroundTicks
                        + " notNearGround=" + notNearGroundTicks
                        + " onGround=" + onGroundTicks
                        + " notOnGround=" + notOnGroundTicks
                        + " flatDeltaY=" + flatDeltaYTicks
                        + " |"
                        + " enterVehicle=" + playerData.getTicksSince(ActionType.ENTER_VEHICLE)
                        + " steerVehicle=" + playerData.getTicksSince(ActionType.STEER_VEHICLE)
                        + " leaveVehicle=" + playerData.getTicksSince(ActionType.LEAVE_VEHICLE)
                        + " damage=" + playerData.getTicksSince(ActionType.DAMAGE)
                        + " teleport=" + playerData.getTicksSince(ActionType.TELEPORT)
                        + " liquid=" + playerData.getTicksSince(ActionType.IN_LIQUID)
                        + " stopFlying=" + playerData.getTicksSince(ActionType.STOP_FLYING)
                        + " inUnloadedChunk=" + playerData.getTicksSince(ActionType.IN_UNLOADED_CHUNK)
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
                        + " buffer=" + buffer
                );
            }
        } else {
            buffer = Math.max(0, buffer - 0.05);
        }

        setLastValues(deltaY, velocity);
    }

    public void setLastValues(double lastDeltaY, double lastVelocity) {
        this.lastDeltaY = lastDeltaY;
        this.lastVelocity = lastVelocity;
    }
}
