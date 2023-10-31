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

@CheckInfo(name = "GroundSpoof (A)", type = CheckType.GROUNDSPOOF,
        description = "Detects clients spoofing their ground status.")
public class GroundSpoofA extends Check {

    private int offGroundTicks;
    private int onGroundTicks;

    public GroundSpoofA(@NonNull PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(@NonNull MovementEvent event, long timestamp) {
        // Checks the player for exemptions.
        if (playerData.getTimeSince(ActionType.LOGIN) < 2000) {
            return;
        }

        WrappedPacketInFlying to = event.getTo();
        Vector3d toPosition = to.getPosition();

        double deltaY = event.getDeltaY();
        double velocity = player.getVelocity().getY();

        boolean isYLevel = event.isYLevel();
        boolean underBlock = playerData.isUnderBlock();
        boolean onGround = playerData.isOnGround();
        boolean nearGround = playerData.isNearGround();

        int underBlockTicks = playerData.getUnderBlockTicks();

        if (!to.isOnGround() && onGround && isYLevel && velocity != 0.0) {
            if (++offGroundTicks >= 2) {
                flag(true, "Sending Off Ground"
                        + " (Y=" + toPosition.getY()
                        + " deltaY=" + deltaY
                        + " velocity=" + velocity
                        + " underBlockTicks=" + underBlockTicks
                );
            }
        } else {
            offGroundTicks = 0;
        }

        if (to.isOnGround()) {
            ++onGroundTicks;

            if (!onGround && !isYLevel) {
                // Fixes a false flag when landing. (hopefully)
                if (onGroundTicks == 1 && nearGround) {
                    return;
                }

                // Fixes false flags when colliding with boats.
                if (playerData.isNearbyBoat(0.6, 0.6, 0.6)) {
                    return;
                }

                flag(true, "Sending On Ground"
                        + " (Y=" + toPosition.getY()
                        + " deltaY=" + deltaY
                        + " velocity=" + velocity
                        + " underBlock=" + underBlock
                        + " underBlockTicks=" + underBlockTicks
                        + " nearGround=" + nearGround
                        + " onGroundTicks=" + onGroundTicks
                );
            }
        } else {
            onGroundTicks = 0;
        }
    }
}
