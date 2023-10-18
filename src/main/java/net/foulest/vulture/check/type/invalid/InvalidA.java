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
import org.bukkit.Location;

@CheckInfo(name = "Invalid (A)", type = CheckType.INVALID, maxViolations = 25)
public class InvalidA extends Check {

    private double buffer, buffer2;
    private int ticks;
    private double lastDeltaY;

    public InvalidA(@NonNull PlayerData playerData) throws ClassNotFoundException {
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

        double deltaY = toPosition.getY() - fromPosition.getY();

        Location playerLocation = player.getLocation();

        boolean onGround = playerData.isOnGround();
        boolean againstBlock = playerData.isAgainstBlock();

        if (playerLocation.getWorld().isChunkLoaded(playerLocation.getBlockX() >> 4, playerLocation.getBlockZ() >> 4)) {
            buffer = Math.max(buffer - 1, 0);
            lastDeltaY = deltaY;
            return;
        }

        if (deltaY == lastDeltaY) {
            if (++buffer > 2) {
                flag("deltaY=" + deltaY
                        + "buffer=" + buffer);
            }
        } else {
            buffer = Math.max(buffer - 0.75, 0);
        }

        if (!onGround && againstBlock) {
            if (++ticks > 7) {
                if (deltaY > 0.1) {
                    if (++buffer2 > 3) {
                        flag("deltaY=" + deltaY
                                + "buffer=" + buffer2);
                    }
                } else {
                    buffer2 = Math.max(buffer2 - 0.75, 0);
                }
            }
        } else {
            ticks = 0;
        }

        lastDeltaY = deltaY;
    }
}
