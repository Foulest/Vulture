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

@CheckInfo(name = "Invalid (A)", type = CheckType.INVALID, maxViolations = 25)
public class InvalidA extends Check {

    private double buffer;

    public InvalidA(@NonNull PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(@NonNull MovementEvent event, long timestamp) {
        // Checks the player for exemptions.
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

        double deltaY = event.getDeltaY();
        double expected = playerData.isUnderBlock() ? 0.200000047 : 0.41999998688697815;

        if (toPosition.getY() % 1.0 > 0.0 && fromPosition.getY() % 1.0 == 0.0 && deltaY > 0) {
            if (deltaY < expected) {
                if (++buffer > 12) {
                    flag(true, "deltaY=" + deltaY
                            + "expected=" + expected
                            + "buffer=" + buffer);
                }
            } else {
                buffer = Math.max(buffer - 2, 0);
            }
        }
    }
}
