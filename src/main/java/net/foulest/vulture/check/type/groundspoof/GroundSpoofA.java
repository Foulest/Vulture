package net.foulest.vulture.check.type.groundspoof;

import io.github.retrooper.packetevents.packetwrappers.play.in.flying.WrappedPacketInFlying;
import io.github.retrooper.packetevents.utils.vector.Vector3d;
import lombok.NonNull;
import net.foulest.vulture.action.ActionType;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.event.MovementEvent;
import net.foulest.vulture.util.block.BlockUtil;

@CheckInfo(name = "GroundSpoof (A)", type = CheckType.GROUNDSPOOF,
        description = "Detects clients spoofing their on-ground status.")
public class GroundSpoofA extends Check {

    public double offGroundBuffer;

    public GroundSpoofA(@NonNull PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(@NonNull MovementEvent event, long timestamp) {
        if (playerData.getTimeSince(ActionType.RESPAWN) < 1000L
                || playerData.getTimeSince(ActionType.TELEPORT) < 1000L
                || playerData.getTimeSince(ActionType.LOGIN) < 1000L
                || playerData.getVelocityY() > 0.0
                || playerData.isOnClimbable()
                || playerData.isNearSlimeBlock()
                || playerData.isNearLiquid()) {
            return;
        }

        WrappedPacketInFlying to = event.getTo();
        WrappedPacketInFlying from = event.getFrom();

        Vector3d toPosition = to.getPosition();
        Vector3d fromPosition = from.getPosition();

        double deltaY = toPosition.getY() - fromPosition.getY();
        double velocityY = player.getVelocity().getY();

        boolean isYLevel = toPosition.getY() % 0.015625 == 0.0;
        boolean underBlock = playerData.isUnderBlock();
        boolean onGround = playerData.isOnGround();
        boolean onGroundStrict = playerData.isOnGroundStrict();

        int underBlockTicks = playerData.getUnderBlockTicks();

        // Detects spoofing being on the ground.
        if (!onGround && to.isOnGround()) {
            // Fixes false flags.
            if (isYLevel && deltaY > -0.13 && deltaY <= 0.0) {
                if ((velocityY <= 0.0 && Math.abs(velocityY) <= 0.0784000015258789)
                        || velocityY == 0.0 || velocityY == 0.08307781780646721
                        || velocityY == 0.16477328182606651) {
                    return;
                }
            }

            // Alternative on ground check.
            if (BlockUtil.isOnGroundOffset(player, 0.075)) {
                return;
            }

            // Fixes false flags with jumping under trees.
            if (underBlock && isYLevel && BlockUtil.isOnGroundOffset(player, 0.250)) {
                return;
            }

            flag("Spoofing being on the ground"
                    + " (y=" + toPosition.getY()
                    + " deltaY=" + deltaY
                    + " velocityY=" + velocityY
                    + " underBlock=" + underBlock
                    + " underBlockTicks=" + underBlockTicks
                    + " 0.075=" + BlockUtil.isOnGroundOffset(player, 0.075)
                    + " 0.100=" + BlockUtil.isOnGroundOffset(player, 0.100)
                    + " 0.150=" + BlockUtil.isOnGroundOffset(player, 0.150)
                    + " 0.200=" + BlockUtil.isOnGroundOffset(player, 0.200)
                    + " 0.250=" + BlockUtil.isOnGroundOffset(player, 0.250)
                    + " 0.300=" + BlockUtil.isOnGroundOffset(player, 0.300)
                    + " 0.350=" + BlockUtil.isOnGroundOffset(player, 0.350)
                    + " 0.400=" + BlockUtil.isOnGroundOffset(player, 0.400)
                    + " 0.450=" + BlockUtil.isOnGroundOffset(player, 0.450)
                    + " 0.500=" + BlockUtil.isOnGroundOffset(player, 0.500)
                    + " 0.600=" + BlockUtil.isOnGroundOffset(player, 0.600)
                    + " 0.700=" + BlockUtil.isOnGroundOffset(player, 0.700)
                    + " 0.750=" + BlockUtil.isOnGroundOffset(player, 0.750) + ")"
            );
        }

        // Detects spoofing being off the ground.
        // A buffer is needed to prevent false flags with jumping under blocks.
        if (onGroundStrict && !to.isOnGround() && isYLevel) {
            if (++offGroundBuffer >= 2) {
                flag("Spoofing being off the ground"
                        + " (y=" + toPosition.getY()
                        + " deltaY=" + deltaY
                        + " velocityY=" + velocityY + ")"
                );
            }
        } else {
            offGroundBuffer = Math.max(0, offGroundBuffer - 0.75);
        }
    }
}
