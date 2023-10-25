package net.foulest.vulture.check.type.velocity;

import io.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.packetwrappers.play.in.flying.WrappedPacketInFlying;
import io.github.retrooper.packetevents.utils.player.ClientVersion;
import io.github.retrooper.packetevents.utils.vector.Vector3d;
import lombok.NonNull;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.event.MovementEvent;
import net.foulest.vulture.util.MathUtil;

@CheckInfo(name = "Velocity (B)", type = CheckType.VELOCITY)
public class VelocityB extends Check {

    private int buffer;

    public VelocityB(@NonNull PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(@NonNull MovementEvent event, long timestamp) {
        // Checks the player for exemptions.
        if (playerData.isNearLiquid()
                || playerData.isUnderBlock()
                || playerData.getLastLocation().getY() % 0.015625 != 0.0
                || playerData.isInWeb()
                || playerData.isOnClimbable()
                || !playerData.isLastLastOnGroundPacket()
                || playerData.getLastLocation() == null
                || playerData.getLastLastLocation() == null
                || playerData.isAgainstBlock()
                || player.isInsideVehicle()) {
            return;
        }

        WrappedPacketInFlying to = event.getTo();
        Vector3d toPosition = to.getPosition();

        int totalTicks = playerData.getTotalTicks();
        int velocityTicks = playerData.getVelocityTicks();
        int lastServerPositionTick = playerData.getLastServerPositionTick();
        int velTicks = totalTicks - velocityTicks;

        boolean newerThan1_8 = PacketEvents.get().getPlayerUtils().getClientVersion(player).isNewerThan(ClientVersion.v_1_8);
        boolean isYLevel = event.isYLevel();
        boolean isVelocityCheckTime = velTicks == 1 && lastServerPositionTick > 120;

        double deltaXZ = event.getDeltaXZ();
        double lastVelocityX = playerData.getLastVelocityX();
        double lastVelocityZ = playerData.getLastVelocityZ();
        double magnitude = MathUtil.hypot(lastVelocityX, lastVelocityZ);
        double scaledVelocity = getVelocity(magnitude, toPosition);
        double velocityHorizontal = playerData.getVelocityHorizontal();
        double velocityMultiplier = isYLevel ? 0.5 : newerThan1_8 ? 0.65 : 0.99;
        double velocityLimit = isYLevel ? 0.4 : velocityMultiplier;

        if (isVelocityCheckTime) {
            if (deltaXZ <= velocityHorizontal * velocityMultiplier
                    && scaledVelocity < velocityLimit && magnitude > 0.2) {
                if ((buffer += (int) (1.1 - scaledVelocity)) > 3.5) {
                    flag(false, "vel=" + (isYLevel ? (int) scaledVelocity : scaledVelocity) * 100 + "%");
                    buffer = 0;
                }
            } else {
                buffer -= (int) Math.min(buffer, 1.1);
            }
        }
    }

    public double getVelocity(double magnitude, @NonNull Vector3d position) {
        double lastX = playerData.getLastLocation().getX();
        double lastZ = playerData.getLastLocation().getZ();

        double lastLastX = playerData.getLastLastLocation().getX();
        double lastLastZ = playerData.getLastLastLocation().getZ();

        double deltaXZ = MathUtil.hypot(position.getX() - lastX, position.getZ() - lastZ);
        double lastDeltaXZ = MathUtil.hypot(lastLastX - lastX, lastLastZ - lastZ);
        return Math.max(deltaXZ, lastDeltaXZ) / magnitude;
    }
}
