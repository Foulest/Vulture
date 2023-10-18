package net.foulest.vulture.check.type.invalid;

import io.github.retrooper.packetevents.packetwrappers.play.in.flying.WrappedPacketInFlying;
import io.github.retrooper.packetevents.utils.vector.Vector3d;
import lombok.NonNull;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.event.MovementEvent;

@CheckInfo(name = "Invalid (B)", type = CheckType.INVALID, maxViolations = 25,
        description = "Detects players continually rising.")
public class InvalidB extends Check {

    private double lastDeltaY;
    private int ticksRising;

    public InvalidB(@NonNull PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(@NonNull MovementEvent event, long timestamp) {
        WrappedPacketInFlying to = event.getTo();
        WrappedPacketInFlying from = event.getFrom();

        Vector3d toPosition = to.getPosition();
        Vector3d fromPosition = from.getPosition();

        if (player.isFlying()
                || playerData.isOnClimbable()
                || playerData.isTeleporting(toPosition)
                || playerData.isNearLiquid()
                || playerData.getVelocityY() > 0
                || playerData.getLastVelocityY() > 0) {
            return;
        }

        double deltaY = toPosition.getY() - fromPosition.getY();

        boolean rising = deltaY > 0 && deltaY >= lastDeltaY;

        if (rising) {
            if (++ticksRising > 2) {
                flag("deltaY=" + deltaY
                        + " lastDeltaY=" + lastDeltaY
                        + " ticksRising=" + ticksRising);
            }
        } else {
            ticksRising = 0;
        }

        lastDeltaY = deltaY;
    }
}
