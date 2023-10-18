package net.foulest.vulture.check.type.invalid;

import io.github.retrooper.packetevents.packetwrappers.play.in.flying.WrappedPacketInFlying;
import io.github.retrooper.packetevents.utils.vector.Vector3d;
import lombok.NonNull;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.event.MovementEvent;

@CheckInfo(name = "Invalid (D)", type = CheckType.INVALID, maxViolations = 25,
        description = "Detects jumping without ever leaving the ground.")
public class InvalidD extends Check {

    private boolean jumping;
    private boolean hasLeftGround;
    private boolean wasEverOnGroundDuringJump;
    private int jumpTicks;

    public InvalidD(@NonNull PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(@NonNull MovementEvent event, long timestamp) {
        WrappedPacketInFlying to = event.getTo();
        WrappedPacketInFlying from = event.getFrom();

        Vector3d toPosition = to.getPosition();
        Vector3d fromPosition = from.getPosition();

        double deltaY = toPosition.getY() - fromPosition.getY();

        boolean onGroundStrict = playerData.isOnGroundStrict();

        if (playerData.isTeleporting(toPosition)) {
            return;
        }

        if (deltaY == 0.41999998688697815) {
            jumping = true;
            hasLeftGround = false;
            wasEverOnGroundDuringJump = onGroundStrict;
            jumpTicks = 1;
        }

        if (jumping) {
            if (onGroundStrict) {
                wasEverOnGroundDuringJump = true;
            }

            if (!onGroundStrict) {
                hasLeftGround = true;
            }

            if (deltaY <= 0.0) {
                jumping = false;

                if (!wasEverOnGroundDuringJump || jumpTicks == 1 || jumpTicks == 6) {
                    return;
                }

                if (!hasLeftGround) {
                    flag("jumpTicks=" + jumpTicks);
                }
            }

            ++jumpTicks;
        }
    }
}
