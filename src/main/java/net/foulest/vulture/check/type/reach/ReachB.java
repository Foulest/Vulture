package net.foulest.vulture.check.type.reach;

import io.github.retrooper.packetevents.packetwrappers.play.in.flying.WrappedPacketInFlying;
import io.github.retrooper.packetevents.utils.player.ClientVersion;
import io.github.retrooper.packetevents.utils.vector.Vector3d;
import lombok.NonNull;
import net.foulest.vulture.action.ActionType;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.event.MovementEvent;
import net.foulest.vulture.util.MathUtil;
import net.foulest.vulture.util.MovingObjectPosition;
import net.foulest.vulture.util.Pair;
import net.foulest.vulture.util.raytrace.BoundingBox;
import org.bukkit.GameMode;

@CheckInfo(name = "Reach (B)", type = CheckType.REACH)
public class ReachB extends Check {

    private double buffer;
    private Vector3d playerEyeLocation;

    public ReachB(@NonNull PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(@NonNull MovementEvent event, long timestamp) {
        if (player.getGameMode().equals(GameMode.CREATIVE)
                || playerData.getLastTarget() == null
                || playerData.getTimeSince(ActionType.LAG) <= 200L
                || playerData.getPastLocsB().size() < 10
                || playerData.getLastAttackTick() > 1
                || playerData.getTotalTicks() - playerData.getLastPacketDrop() <= 5
                || playerData.getTimeSince(ActionType.DELAYED_PACKET) <= 160L
                || playerData.getLastServerPositionTick() <= 100 + Math.min(MathUtil.getPingInTicks(playerData.getTransPing()), 5)) {
            return;
        }

        WrappedPacketInFlying to = event.getTo();

        boolean sneaking = playerData.isSneaking();

        float sneakAmount1_8 = sneaking ? 1.54F : 1.62F;
        float sneakAmount1_13 = sneaking ? 1.27F : 1.62F;

        double attackerX = playerData.getAttackerX();
        double attackerY = playerData.getAttackerY();
        double attackerZ = playerData.getAttackerZ();

        if (playerData.getLocation() != null) {
            playerEyeLocation = MathUtil.getPositionEyes(attackerX, attackerY, attackerZ,
                    playerData.getVersion().isOlderThan(ClientVersion.v_1_13) ? sneakAmount1_8 : sneakAmount1_13);
        }

        Vector3d lookDirection;
        Vector3d lookDirectionMD;

        if (to.isRotating()) {
            lookDirectionMD = MathUtil.getVectorForRotation(playerData.getAttackerPitch(), to.getYaw());
            lookDirection = MathUtil.getVectorForRotation(playerData.getAttackerPitch(), playerData.getAttackerYaw());
        } else {
            lookDirectionMD = MathUtil.getVectorForRotation(playerData.getAttackerPitch(), playerData.getAttackerYaw());
            lookDirection = lookDirectionMD;
        }

        Vector3d eyeLocation = playerEyeLocation;
        Vector3d eyeLocationFixed = eyeLocation.add(new Vector3d(lookDirection.getX() * 6.0D,
                lookDirection.getY() * 6.0D, lookDirection.getZ() * 6.0D));
        Vector3d eyeLocationFixedMD = eyeLocation.add(new Vector3d(lookDirectionMD.getX() * 6.0D,
                lookDirectionMD.getY() * 6.0D, lookDirectionMD.getZ() * 6.0D));

        BoundingBox boundingBox = new BoundingBox(0, 0, 0, 0, 0, 0);

        boolean boundingBoxUpdated = false;

        int nowTicks = playerData.getTotalTicks();
        int pingTicks = MathUtil.getPingInTicks(playerData.getTransPing()) + 3;
        int colliding = 0;

        double distance = -1;

        for (Pair<BoundingBox, Integer> pair : playerData.getPastLocsB()) {
            if (Math.abs(nowTicks - pair.getY() - pingTicks) < 2) {
                if (!boundingBoxUpdated) {
                    boundingBox = pair.getX();
                    boundingBoxUpdated = true;
                } else {
                    boundingBox.getMin().setX(Math.min(boundingBox.getMin().getX(), pair.getX().getMin().getX()));
                    boundingBox.getMax().setX(Math.max(boundingBox.getMax().getX(), pair.getX().getMax().getX()));
                    boundingBox.getMin().setY(Math.min(boundingBox.getMin().getY(), pair.getX().getMin().getY()));
                    boundingBox.getMax().setY(Math.max(boundingBox.getMax().getY(), pair.getX().getMax().getY()));
                    boundingBox.getMin().setZ(Math.min(boundingBox.getMin().getZ(), pair.getX().getMin().getZ()));
                    boundingBox.getMax().setZ(Math.max(boundingBox.getMax().getZ(), pair.getX().getMax().getZ()));
                }

                MovingObjectPosition intersection = boundingBox.calculateIntercept(eyeLocation, eyeLocationFixed);
                MovingObjectPosition intersectionMD = boundingBox.calculateIntercept(eyeLocation, eyeLocationFixedMD);

                if (intersection != null && intersectionMD != null && !boundingBox.isVecInside(eyeLocation)) {
                    double distanceToIntersection = eyeLocation.distance(intersection.hitVec);
                    double distanceToIntersectionMD = eyeLocation.distance(intersectionMD.hitVec);
                    distance = Math.min(distanceToIntersection, distanceToIntersectionMD);

                    if (distance > 3.03) {
                        ++colliding;
                    }
                } else {
                    if (intersection == null && intersectionMD == null) {
                        boundingBox.isVecInside(eyeLocation);
                    }
                }
            }
        }

        if (distance > 3.03 && colliding > 2) {
            if ((buffer += 1.5) > 3.5) {
                flag("distance=" + distance);
                event.setCancelled(true);
            }
        } else {
            buffer = Math.max(buffer - 0.5, 0);
        }

        PlayerData.updateFlyingLocations(playerData, to, true);
    }
}

