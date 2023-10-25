package net.foulest.vulture.check.type.speed;

import io.github.retrooper.packetevents.packetwrappers.play.in.flying.WrappedPacketInFlying;
import io.github.retrooper.packetevents.utils.vector.Vector3d;
import lombok.NonNull;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.event.MovementEvent;
import net.foulest.vulture.util.MathUtil;
import net.foulest.vulture.util.MovementUtil;
import org.bukkit.GameMode;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

@CheckInfo(name = "Speed (D)", type = CheckType.SPEED, maxViolations = 25)
public class SpeedD extends Check {

    private double buffer;

    public SpeedD(@NonNull PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(@NonNull MovementEvent event, long timestamp) {
        WrappedPacketInFlying to = event.getTo();
        WrappedPacketInFlying from = event.getFrom();

        Vector3d toPosition = to.getPosition();
        Vector3d fromPosition = from.getPosition();

        // Checks the player for exemptions.
        if (!playerData.isSprinting()
                || playerData.isNearLiquid()
                || player.isFlying()
                || playerData.isInWeb()
                || playerData.getVelocityH() > 0
                || player.getGameMode().equals(GameMode.CREATIVE)
                || player.getGameMode().equals(GameMode.SPECTATOR)
                || !playerData.isNearGround()
                || event.isTeleport(playerData)) {
            return;
        }

        boolean onIce = playerData.isOnIce();
        boolean onStairs = playerData.isOnStairs();
        boolean onSlab = playerData.isOnSlab();

        float walkSpeed = player.getWalkSpeed();

        int groundTicks = playerData.getGroundTicks();

        Vector direction = MathUtil.getDirection(player);

        double speedDelta = MathUtil.getVectorSpeed(fromPosition, toPosition).distanceSquared(direction);
        double maxDelta = 0.221;
        double speedLevel = MovementUtil.getPotionEffectLevel(player, PotionEffectType.SPEED);

        maxDelta += groundTicks < 5 ? speedLevel * 0.07 : speedLevel * 0.0573;
        maxDelta *= onIce ? 1.5 : 1.0;
        maxDelta *= onStairs || onSlab ? 1.5 : 1.0;
        maxDelta += (walkSpeed - 0.2) * 5;

        if (speedDelta > maxDelta) {
            if (++buffer > 8) {
                flag(true, "speedDelta=" + speedDelta
                        + " maxDelta=" + maxDelta
                        + " direction=" + direction);
            }
        } else {
            buffer = Math.max(buffer - 0.5, 0);
        }
    }
}
