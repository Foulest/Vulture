package net.foulest.vulture.check.type.speed;

import io.github.retrooper.packetevents.packetwrappers.play.in.flying.WrappedPacketInFlying;
import io.github.retrooper.packetevents.utils.player.ClientVersion;
import net.foulest.vulture.action.ActionType;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.event.MovementEvent;
import net.foulest.vulture.util.MovementUtil;
import org.bukkit.GameMode;
import org.bukkit.potion.PotionEffectType;

@CheckInfo(name = "Speed (E)", type = CheckType.SPEED,
        description = "Prevents players from using NoSlowdown.")
public class SpeedE extends Check {

    public double bufferStandard;
    public double bufferRapid;

    public SpeedE(PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(MovementEvent event, long timestamp) {
        // Checks the player for exemptions.
        if (player.isFlying()
                || player.getAllowFlight()
                || player.getGameMode().equals(GameMode.CREATIVE)
                || player.getGameMode().equals(GameMode.SPECTATOR)
                || event.isTeleport(playerData)) {
            return;
        }

        WrappedPacketInFlying to = event.getTo();

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

        double deltaXZ = event.getDeltaXZ();
        double velocityHorizontal = playerData.getVelocityXZ();
        double maxSpeed = to.isOnGround() ? 0.21 : 0.32400000005960464;

        maxSpeed += (playerData.getVersion().isOlderThanOrEquals(ClientVersion.v_1_8) ? 0.0 : 0.2);
        maxSpeed += (walkSpeed - 0.2) * 0.02;
        maxSpeed += (flySpeed - 0.1) * 0.01;
        maxSpeed += velocityHorizontal;
        maxSpeed *= onIce ? 6.8 : 1.0;
        maxSpeed += groundTicks < 5 ? speedLevel * 0.07 : speedLevel * 0.0573;

        // Detects standard no-slowdown.
        if (blocking) {
            if (deltaXZ > maxSpeed) {
                if (++bufferStandard >= 3) {
                    flag(true, "Standard"
                            + " (deltaXZ=" + deltaXZ
                            + " maxSpeed=" + maxSpeed
                            + " timeBlocking=" + timeBlocking
                            + " buffer=" + bufferStandard + ")");
                }
            } else {
                bufferStandard = Math.max(bufferStandard - 0.25, 0);
            }
        } else {
            bufferStandard = Math.max(bufferStandard - 0.25, 0);
        }

        // Detects rapidly blocking no-slowdown.
        if (rapidlyBlocking) {
            if (deltaXZ > maxSpeed) {
                if (++bufferRapid >= 5) {
                    flag(true, "Rapidly blocking"
                            + " (deltaXZ=" + deltaXZ
                            + " maxSpeed=" + maxSpeed
                            + " timeBlocking=" + timeBlocking
                            + " buffer=" + bufferRapid + ")");
                }
            } else {
                bufferRapid = Math.max(bufferRapid - 0.25, 0);
            }
        } else {
            bufferRapid = Math.max(bufferRapid - 0.25, 0);
        }
    }
}
