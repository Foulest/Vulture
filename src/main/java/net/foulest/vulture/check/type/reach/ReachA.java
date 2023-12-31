package net.foulest.vulture.check.type.reach;

import io.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.packetwrappers.play.in.flying.WrappedPacketInFlying;
import io.github.retrooper.packetevents.utils.vector.Vector3d;
import lombok.NonNull;
import net.foulest.vulture.action.ActionType;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.data.PlayerDataManager;
import net.foulest.vulture.event.MovementEvent;
import net.foulest.vulture.util.MathUtil;
import net.foulest.vulture.util.MessageUtil;
import net.foulest.vulture.util.data.Pair;
import net.foulest.vulture.util.raytrace.BoundingBox;
import net.foulest.vulture.util.raytrace.RayTraceUtil;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

@CheckInfo(name = "Reach (A)", type = CheckType.REACH)
public class ReachA extends Check {

    private double buffer;

    public ReachA(@NonNull PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(@NonNull MovementEvent event, long timestamp) {
        // Checks the player for exemptions.
        if (player.getGameMode().equals(GameMode.CREATIVE)
                || playerData.getLastTarget() == null
                || playerData.getLastAttackTick() > 1
                || playerData.getTotalTicks() - playerData.getLastPacketDrop() <= 20
                || playerData.getPastLocsA().size() < 20
                || playerData.getLastTeleportPacket() != null
                || player.isInsideVehicle()
                || playerData.getTimeSince(ActionType.TELEPORT) <= 5000L
                || playerData.getTimeSince(ActionType.LAG) <= 250L
                || playerData.getTimeSince(ActionType.DELAYED_PACKET) <= 100L) {
            return;
        }

        WrappedPacketInFlying to = event.getTo();
        Vector3d toPosition = to.getPosition();

        Player target = playerData.getLastTarget();
        PlayerData targetData = PlayerDataManager.getPlayerData(target);

        if (targetData.getLastServerPositionTick() < 100
                || targetData.getTotalTicks() - targetData.getLastPacketDrop() < 15) {
            return;
        }

        BoundingBox boundingBox = new BoundingBox(new Vector(0, 0, 0), new Vector(0, 0, 0));

        boolean updateBoundingBox = false;

        int nowTicks = playerData.getTotalTicks();
        int pingTicks = MathUtil.getPingInTicks(PacketEvents.get().getPlayerUtils().getPing(player)) + 3;
        int collided = 0;

        Vector origin = new Vector(toPosition.getX(), toPosition.getY(), toPosition.getZ());

        double distance = -1;

        for (Pair<BoundingBox, Integer> pair : playerData.getPastLocsA()) {
            if (Math.abs(nowTicks - pair.getY() - pingTicks) < 2) {
                if (!updateBoundingBox) {
                    boundingBox = pair.getX();
                    updateBoundingBox = true;
                } else {
                    boundingBox.min.setX(Math.min(boundingBox.min.getX(), pair.getX().min.getX()));
                    boundingBox.max.setX(Math.max(boundingBox.max.getX(), pair.getX().max.getX()));
                    boundingBox.min.setY(Math.min(boundingBox.min.getY(), pair.getX().min.getY()));
                    boundingBox.max.setY(Math.max(boundingBox.max.getY(), pair.getX().max.getY()));
                    boundingBox.min.setZ(Math.min(boundingBox.min.getZ(), pair.getX().min.getZ()));
                    boundingBox.max.setZ(Math.max(boundingBox.max.getZ(), pair.getX().max.getZ()));
                }

                double boxX = Math.abs(boundingBox.min.getX() - boundingBox.max.getX()) / 2;
                double boxZ = Math.abs(boundingBox.min.getZ() - boundingBox.max.getZ()) / 2;

                Vector loc = new Vector(boundingBox.min.getX() + boxX, 0, boundingBox.min.getZ() + boxZ);

                distance = (origin.setY(0).distance(loc) - Math.hypot(boxX, boxZ) - 0.1) - 0.05;

                if (distance > 3.05) {
                    ++collided;
                }
            }
        }

        MessageUtil.debug("distance=" + distance + " collided=" + collided
                + " looking=" + RayTraceUtil.getBlockPlayerLookingAt(player, 3.05));

        if (distance > 3.05 && collided > 2) {
            if ((buffer += 1.5) > 3.5) {
                flag(false, "distance=" + distance);
                event.setCancelled(true);
            }
        } else {
            buffer = Math.max(buffer - 0.75, 0);
        }
    }
}
