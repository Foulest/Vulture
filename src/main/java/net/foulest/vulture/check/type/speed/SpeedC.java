package net.foulest.vulture.check.type.speed;

import io.github.retrooper.packetevents.packetwrappers.play.in.flying.WrappedPacketInFlying;
import io.github.retrooper.packetevents.utils.vector.Vector3d;
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

@CheckInfo(name = "Speed (C)", type = CheckType.SPEED, maxViolations = 25)
public class SpeedC extends Check {

    private double lastDeltaXZ;
    private double friction = 0.91;
    private double buffer;

    public SpeedC(@NonNull PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(@NonNull MovementEvent event, long timestamp) {
        WrappedPacketInFlying to = event.getTo();
        WrappedPacketInFlying from = event.getFrom();

        Vector3d toPosition = to.getPosition();
        Vector3d fromPosition = from.getPosition();

        double deltaX = toPosition.getX() - fromPosition.getX();
        double deltaY = toPosition.getY() - fromPosition.getY();
        double deltaZ = toPosition.getZ() - fromPosition.getZ();
        double deltaXZ = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        // Sets friction to 0.91 if the player is not on the ground.
        if (!from.isOnGround()) {
            friction = 0.91;
        }

        // Returns if the player is flying, climbing, in a vehicle, in creative, or in spectator mode.
        if (player.getAllowFlight()
                || playerData.isOnClimbable()
                || player.isInsideVehicle()
                || playerData.getTimeSince(ActionType.STEER_VEHICLE) < 100L
                || player.getGameMode().equals(GameMode.CREATIVE)
                || player.getGameMode().equals(GameMode.SPECTATOR)
                || playerData.isTeleporting(toPosition)) {
            lastDeltaXZ = deltaXZ * friction;
            return;
        }

        boolean sprinting = playerData.isSprinting();
        boolean onSlab = playerData.isOnSlab();
        boolean onIce = playerData.isOnIce();
        boolean underBlock = playerData.isUnderBlock();
        boolean nearLiquid = playerData.isNearLiquid();

        float speedLevel = MovementUtil.getPotionEffectLevel(player, PotionEffectType.SPEED);
        float jumpLevel = MovementUtil.getPotionEffectLevel(player, PotionEffectType.JUMP);
        float walkSpeed = player.getWalkSpeed();

        double jumpHeight = 0.42 + (jumpLevel * 0.2);
        double velocityHorizontal = playerData.getVelocityHorizontal();
        double blockFriction = BlockUtil.getBlockFriction(player);

        double movementSpeed;

        if (from.isOnGround()) {
            movementSpeed = sprinting ? (0.0699999988079071 + 0.030000001192092896) * 1.3 : 0.1;
            movementSpeed *= 0.16277136 / (friction * friction * friction);

            if (deltaY > 0.0000001 && deltaY < jumpHeight && sprinting) {
                movementSpeed += 0.2;
            }

            if (onSlab) {
                movementSpeed += 0.2;
            }

            if (onIce) {
                movementSpeed += 0.11;
            }

        } else {
            movementSpeed = 0.026;

            if (underBlock) {
                movementSpeed = movementSpeed * 2;
            }

            if (onSlab) {
                movementSpeed += 0.2;
            }

            if (deltaY > 0.4199 && sprinting) {
                movementSpeed += 0.4199;
            }
        }

        movementSpeed += (speedLevel * 0.2) * movementSpeed;
        movementSpeed += velocityHorizontal;
        movementSpeed += (walkSpeed - 0.2) * 12;

        if (nearLiquid) {
            movementSpeed += 0.2;
            movementSpeed *= 8;
        }

        double diff = deltaXZ - lastDeltaXZ;
        double speedup = diff - movementSpeed;

        if (speedup > 0.1 && deltaXZ > 0.25) {
            if (++buffer >= 3) {
                flag("speedup=" + speedup
                        + " buffer=" + buffer
                        + " deltaXZ=" + deltaXZ
                        + " diff=" + diff
                        + " movementSpeed=" + movementSpeed);
            }
        } else {
            buffer = Math.max(0, buffer - 0.25);
        }

        lastDeltaXZ = deltaXZ * friction;
        friction = blockFriction * 0.91;
    }
}
