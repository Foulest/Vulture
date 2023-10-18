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
import org.bukkit.potion.PotionEffectType;

@CheckInfo(name = "Speed (E)", type = CheckType.SPEED, maxViolations = 25,
        description = "Prevents players from using no-slowdown.")
public class SpeedE extends Check {

    public double bufferStandard;
    public double bufferRapid;

    public SpeedE(@NonNull PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(@NonNull MovementEvent event, long timestamp) {
        WrappedPacketInFlying to = event.getTo();
        WrappedPacketInFlying from = event.getFrom();

        Vector3d toPosition = to.getPosition();
        Vector3d fromPosition = from.getPosition();

        long timeSinceBlocking = playerData.getTimeSince(ActionType.BLOCKING);
        long timeSinceRelease = playerData.getTimeSince(ActionType.RELEASE_USE_ITEM);
        long timeBlocking = (timeSinceBlocking < timeSinceRelease ? timeSinceBlocking : 0);

        boolean onIce = playerData.isOnIce();
        boolean blocking = playerData.isBlocking();
        boolean rapidlyBlocking = timeSinceBlocking <= 100 && timeSinceRelease <= 100;

        float speedLevel = MovementUtil.getPotionEffectLevel(player, PotionEffectType.SPEED);
        float walkSpeed = player.getWalkSpeed();
        float flySpeed = player.getFlySpeed();

        int groundTicks = playerData.getGroundTicks();

        double deltaX = toPosition.getX() - fromPosition.getX();
        double deltaZ = toPosition.getZ() - fromPosition.getZ();
        double deltaXZ = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        double velocityHorizontal = playerData.getVelocityHorizontal();
        double maxSpeed = to.isOnGround() ? 0.21 : 0.32400000005960464;

        maxSpeed += (walkSpeed - 0.2) * 0.02;
        maxSpeed += (flySpeed - 0.1) * 0.01;
        maxSpeed += velocityHorizontal;
        maxSpeed *= onIce ? 6.8 : 1.0;
        maxSpeed += groundTicks < 5 ? speedLevel * 0.07 : speedLevel * 0.0573;

        // Detects standard no-slowdown.
        if (blocking) {
            if (deltaXZ > maxSpeed) {
                if (++bufferRapid >= 3) {
                    flag("Standard"
                            + " (deltaXZ=" + deltaXZ
                            + " maxSpeed=" + maxSpeed
                            + " timeBlocking=" + timeBlocking
                            + " buffer=" + bufferStandard + ")");
                }
            } else {
                bufferStandard = Math.max(0, bufferStandard - 0.5);
            }
        } else {
            bufferStandard = Math.max(0, bufferStandard - 0.9);
        }

        // Detects rapidly blocking no-slowdown.
        if (rapidlyBlocking) {
            if (deltaXZ > maxSpeed) {
                if (++bufferRapid >= 5) {
                    flag("Rapidly blocking"
                            + " (deltaXZ=" + deltaXZ
                            + " maxSpeed=" + maxSpeed
                            + " timeBlocking=" + timeBlocking
                            + " buffer=" + bufferRapid + ")");
                }
            } else {
                bufferRapid = Math.max(0, bufferRapid - 0.5);
            }
        } else {
            bufferRapid = Math.max(0, bufferRapid - 0.9);
        }
    }
}
