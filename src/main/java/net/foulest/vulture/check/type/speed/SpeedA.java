package net.foulest.vulture.check.type.speed;

import io.github.retrooper.packetevents.packetwrappers.play.in.flying.WrappedPacketInFlying;
import lombok.NonNull;
import net.foulest.vulture.action.ActionType;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.event.MovementEvent;
import net.foulest.vulture.util.MovementUtil;
import org.bukkit.GameMode;
import org.bukkit.potion.PotionEffectType;

@CheckInfo(name = "Speed (A)", type = CheckType.SPEED)
public class SpeedA extends Check {

    private double buffer;
    private double terrainBuffer;

    public SpeedA(@NonNull PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(@NonNull MovementEvent event, long timestamp) {
        WrappedPacketInFlying to = event.getTo();

        // Checks the player for exemptions.
        if (player.isFlying()
                || player.getAllowFlight()
                || player.getGameMode().equals(GameMode.CREATIVE)
                || player.getGameMode().equals(GameMode.SPECTATOR)
                || player.isInsideVehicle()
                || event.isTeleport(playerData)) {
            return;
        }

        boolean inWeb = playerData.isInWeb();
        boolean onSoulSand = playerData.isOnSoulSand();
        boolean onStairs = playerData.isOnStairs();
        boolean onSlab = playerData.isOnSlab();
        boolean underBlock = playerData.isUnderBlock();
        boolean nearLiquid = playerData.isNearLiquid();
        boolean nearSlimeBlock = playerData.isNearSlimeBlock();
        boolean nearLilyPad = playerData.isNearLilyPad();

        double deltaXZ = event.getDeltaXZ();
        double maxSpeed = to.isOnGround() && !nearLilyPad ? 0.3125 : 0.35855;
        double velocityHorizontal = playerData.getVelocityHorizontal();

        int groundTicks = playerData.getGroundTicks();
        int groundTicksStrict = playerData.getGroundTicksStrict();

        long timeSinceOnIce = playerData.getTimeSince(ActionType.ON_ICE);
        long timeSinceUnderBlock = playerData.getTimeSince(ActionType.UNDER_BLOCK);

        float speedLevel = MovementUtil.getPotionEffectLevel(player, PotionEffectType.SPEED);
        float slownessLevel = MovementUtil.getPotionEffectLevel(player, PotionEffectType.SLOW);
        float depthStriderLevel = MovementUtil.getDepthStriderLevel(player);

        float walkSpeed = player.getWalkSpeed();
        float flySpeed = player.getFlySpeed();

        maxSpeed += groundTicks < 5 ? speedLevel * 0.07 : speedLevel * 0.0573;
        maxSpeed -= groundTicks < 5 ? slownessLevel * 0.07 : slownessLevel * 0.0573;

        maxSpeed += underBlock ? 0.26 : 0.0;
        maxSpeed += nearLiquid ? depthStriderLevel * 0.45 : 0.0;
        maxSpeed += (walkSpeed - 0.2) * 2.5;
        maxSpeed += (flySpeed - 0.1) * 2.5;
        maxSpeed += velocityHorizontal;

        maxSpeed *= (onStairs || onSlab) ? 1.5 : 1.0;
        maxSpeed *= inWeb ? 0.11 : 1.0;
        maxSpeed *= timeSinceOnIce < 100 ? 4.4 : 1.0; // TODO: This is a bit high
        maxSpeed *= onSoulSand && groundTicksStrict > 2 ? 0.6 : 1.0;
        maxSpeed *= nearSlimeBlock ? 1.25 : 1.0;

        double difference = deltaXZ - maxSpeed;

        if (deltaXZ > maxSpeed) {
            if (inWeb || onSoulSand) {
                if (++terrainBuffer > 2) {
                    flag(true, "(Terrain)"
                            + " deltaXZ=" + deltaXZ
                            + " maxSpeed=" + maxSpeed
                            + " buffer=" + buffer);
                }
            } else {
                buffer += 0.1 + difference;

                if (buffer > 1) {
                    flag(true, "deltaXZ=" + deltaXZ
                            + " maxSpeed=" + maxSpeed
                            + " difference=" + difference
                            + " buffer=" + buffer);
                }
            }
        } else {
            buffer = Math.max(buffer - 0.25, 0);
            terrainBuffer = Math.max(terrainBuffer - 0.25, 0);
        }
    }
}
