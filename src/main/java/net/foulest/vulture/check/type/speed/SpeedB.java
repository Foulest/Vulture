package net.foulest.vulture.check.type.speed;

import io.github.retrooper.packetevents.packetwrappers.play.in.flying.WrappedPacketInFlying;
import io.github.retrooper.packetevents.utils.vector.Vector3d;
import lombok.NonNull;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.event.MovementEvent;

@CheckInfo(name = "Speed (B)", type = CheckType.SPEED, maxViolations = 25)
public class SpeedB extends Check {

    private int buffer;
    private double lastDeltaXZ;

    public SpeedB(@NonNull PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(@NonNull MovementEvent event, long timestamp) {
        WrappedPacketInFlying to = event.getTo();
        WrappedPacketInFlying from = event.getFrom();

        Vector3d toPosition = to.getPosition();
        Vector3d fromPosition = from.getPosition();

        double deltaX = toPosition.getX() - fromPosition.getX();
        double deltaZ = toPosition.getZ() - fromPosition.getZ();
        double deltaXZ = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        if (player.getAllowFlight()
                || playerData.isNearLiquid()
                || playerData.isOnGround()
                || playerData.isOnClimbable()
                || playerData.getVelocityH() > 0
                || player.getWalkSpeed() != 0.2
                || playerData.isInWeb()
                || deltaXZ <= 0.005) {
            lastDeltaXZ = deltaXZ;
            return;
        }

        double diff = lastDeltaXZ * 0.91F + 0.02;

        if (playerData.isSprinting()) {
            diff += 0.0063;
        }

        double deltaXZDiff = deltaXZ - diff;

        if (deltaXZDiff > 0.0 && diff > 0.08 && deltaXZ > 0.15) {
            if (++buffer > 8) {
                flag();
                buffer /= 2;
            }
        } else {
            buffer = 0;
        }

        lastDeltaXZ = deltaXZ;
    }
}
