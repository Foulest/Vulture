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
        WrappedPacketInFlying from = event.getFrom();

        Vector3d toPosition = to.getPosition();
        Vector3d fromPosition = from.getPosition();

        int totalTicks = playerData.getTotalTicks();
        int velocityTicks = playerData.getVelocityTicks();
        int lastServerPositionTick = playerData.getLastServerPositionTick();
        int velTicks = totalTicks - velocityTicks;

        boolean newerThan1_8 = PacketEvents.get().getPlayerUtils().getClientVersion(player).isNewerThan(ClientVersion.v_1_8);
        boolean isOnGround = toPosition.getY() % 0.015625 == 0.0;
        boolean isVelocityCheckTime = velTicks == 1 && lastServerPositionTick > 120;

        double deltaX = toPosition.getX() - fromPosition.getX();
        double deltaZ = toPosition.getZ() - fromPosition.getZ();
        double deltaXZ = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        double lastVelocityX = playerData.getLastVelocityX();
        double lastVelocityZ = playerData.getLastVelocityZ();
        double magnitude = MathUtil.hypot(lastVelocityX, lastVelocityZ);
        double scaledVelocity = getVelocity(magnitude, toPosition);
        double velocityHorizontal = playerData.getVelocityHorizontal();
        double velocityMultiplier = isOnGround ? 0.5 : newerThan1_8 ? 0.65 : 0.99;
        double velocityLimit = isOnGround ? 0.4 : velocityMultiplier;

        if (isVelocityCheckTime) {
            if (deltaXZ <= velocityHorizontal * velocityMultiplier
                    && scaledVelocity < velocityLimit && magnitude > 0.2) {
                if ((buffer += (int) (1.1 - scaledVelocity)) > 3.5) {
                    flag("vel=" + (isOnGround ? (int) scaledVelocity : scaledVelocity) * 100 + "%");
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
