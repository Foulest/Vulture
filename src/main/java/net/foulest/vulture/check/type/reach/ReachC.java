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
import net.foulest.vulture.util.MessageUtil;
import net.foulest.vulture.util.data.MovingObjectPosition;
import net.foulest.vulture.util.data.Pair;
import net.foulest.vulture.util.raytrace.BoundingBox;
import org.bukkit.GameMode;

@CheckInfo(name = "Reach (C)", type = CheckType.REACH)
public class ReachC extends Check {

    private double buffer;
    private Vector3d playerEyeLocation;

    public ReachC(@NonNull PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(@NonNull MovementEvent event, long timestamp) {
        // Checks the player for exemptions.
        if (player.getGameMode().equals(GameMode.CREATIVE)
                || playerData.getLastAttackTick() > 1
                || playerData.getLastServerPositionTick() <= 100 + Math.min(MathUtil.getPingInTicks(playerData.getTransPing()), 5)
                || playerData.getLastTarget() == null
                || playerData.getPastLocsC().size() < 10
                || playerData.getTimeSince(ActionType.DELAYED_PACKET) <= 160L
                || playerData.getTimeSince(ActionType.LAG) <= 200L
                || playerData.getTotalTicks() - playerData.getLastPacketDrop() <= 5) {
            return;
        }

        WrappedPacketInFlying to = event.getTo();

        boolean sneaking = playerData.isSneaking();

        float sneakAmount1_8 = sneaking ? 1.54F : 1.62F;
        float sneakAmount1_13 = sneaking ? 1.27F : 1.62F;

        double attackerX2 = playerData.getAttackerX2();
        double attackerY2 = playerData.getAttackerY2();
        double attackerZ2 = playerData.getAttackerZ2();

        if (playerData.getLocation() != null) {
            playerEyeLocation = MathUtil.getPositionEyes(attackerX2, attackerY2, attackerZ2,
                    playerData.getVersion().isOlderThan(ClientVersion.v_1_13) ? sneakAmount1_8 : sneakAmount1_13);
        }

        Vector3d lookDirection;
        Vector3d lookDirectionMD;

        if (to.isRotating()) {
            lookDirectionMD = MathUtil.getVectorForRotation(playerData.getAttackerPitch2(), to.getYaw());
            lookDirection = MathUtil.getVectorForRotation(playerData.getAttackerPitch2(), playerData.getAttackerYaw2());
        } else {
            lookDirectionMD = MathUtil.getVectorForRotation(playerData.getAttackerPitch2(), playerData.getAttackerYaw2());
            lookDirection = lookDirectionMD;
        }

        Vector3d eyeLocation = playerEyeLocation;
        Vector3d eyeLocationFixed = eyeLocation.add(new Vector3d(lookDirection.getX() * 6.0D,
                lookDirection.getY() * 6.0D, lookDirection.getZ() * 6.0D));
        Vector3d eyeLocationFixedMD = eyeLocation.add(new Vector3d(lookDirectionMD.getX() * 6.0D,
                lookDirectionMD.getY() * 6.0D, lookDirectionMD.getZ() * 6.0D));

        BoundingBox BoundingBox = new BoundingBox(0, 0, 0, 0, 0, 0);

        boolean boundingBoxUpdated = false;

        int nowTicks = playerData.getTotalTicks();
        int pingTicks = MathUtil.getPingInTicks(playerData.getTransPing()) + 3;
        int misses = 0;

        for (Pair<BoundingBox, Integer> pair : playerData.getPastLocsC()) {
            if (Math.abs(nowTicks - pair.getY() - pingTicks) < 2) {
                if (!boundingBoxUpdated) {
                    BoundingBox = pair.getX();
                    boundingBoxUpdated = true;
                } else {
                    BoundingBox.getMin().setX(Math.min(BoundingBox.getMin().getX(), pair.getX().getMin().getX()));
                    BoundingBox.getMax().setX(Math.max(BoundingBox.getMax().getX(), pair.getX().getMax().getX()));
                    BoundingBox.getMin().setY(Math.min(BoundingBox.getMin().getY(), pair.getX().getMin().getY()));
                    BoundingBox.getMax().setY(Math.max(BoundingBox.getMax().getY(), pair.getX().getMax().getY()));
                    BoundingBox.getMin().setZ(Math.min(BoundingBox.getMin().getZ(), pair.getX().getMin().getZ()));
                    BoundingBox.getMax().setZ(Math.max(BoundingBox.getMax().getZ(), pair.getX().getMax().getZ()));
                }

                MovingObjectPosition intersection = BoundingBox.calculateIntercept(eyeLocation, eyeLocationFixed);
                MovingObjectPosition intersectionMD = BoundingBox.calculateIntercept(eyeLocation, eyeLocationFixedMD);

                if (intersection == null && intersectionMD == null && !BoundingBox.isVecInside(eyeLocation)) {
                    ++misses;
                }
            }
        }

        MessageUtil.debug("misses=" + misses);

        if (misses >= 3 && playerData.getTotalTicks() - playerData.getLastPacketDrop() > 10) {
            if ((buffer += 0.75) > 8.5) {
                flag(false, "misses=" + misses);
            }
        } else {
            buffer = Math.max(buffer - 4.5, 0.0);
        }

        PlayerData.updateFlyingLocations(playerData, to, false);
    }
}
