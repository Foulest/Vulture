package net.foulest.vulture.check.type.invalid;

import io.github.retrooper.packetevents.packetwrappers.play.in.flying.WrappedPacketInFlying;
import io.github.retrooper.packetevents.utils.vector.Vector3d;
import lombok.NonNull;
import net.foulest.vulture.action.ActionType;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.event.MovementEvent;

@CheckInfo(name = "Invalid (C)", type = CheckType.INVALID, maxViolations = 25)
public class InvalidC extends Check {

    private double buffer;

    public InvalidC(@NonNull PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(@NonNull MovementEvent event, long timestamp) {
        if (playerData.getTimeSince(ActionType.RESPAWN) < 1000L
                || playerData.getTimeSince(ActionType.TELEPORT) < 1000L
                || playerData.getTimeSince(ActionType.LOGIN) < 1000L
                || playerData.getVelocityY() > 0.0
                || playerData.isNearLiquid()
                || playerData.isOnClimbable()
                || playerData.isNearAnvil()
                || playerData.isNearSlimeBlock()
                || playerData.isNearTrapdoor()
                || playerData.isOnIce()) {
            return;
        }

        WrappedPacketInFlying to = event.getTo();
        WrappedPacketInFlying from = event.getFrom();

        Vector3d toPosition = to.getPosition();
        Vector3d fromPosition = from.getPosition();

        double toY = toPosition.getY();
        double fromY = fromPosition.getY();

        double deltaY = toY - fromY;
        double expected = playerData.isUnderBlock() ? 0.200000047 : 0.41999998688697815;

        if (toY % 1.0 > 0.0 && fromY % 1.0 == 0.0 && deltaY > 0) {
            if (deltaY < expected) {
                if (++buffer > 12) {
                    flag("deltaY=" + deltaY
                            + "expected=" + expected
                            + "buffer=" + buffer);
                }
            } else {
                buffer = Math.max(buffer - 2, 0);
            }
        }
    }
}
