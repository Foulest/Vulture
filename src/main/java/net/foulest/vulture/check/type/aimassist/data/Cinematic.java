package net.foulest.vulture.check.type.aimassist.data;

import net.foulest.vulture.action.ActionType;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.event.RotationEvent;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@CheckInfo(name = "Cinematic", type = CheckType.AIMASSIST,
        description = "Checks if the player is using Cinematic mode.", punishable = false)
public class Cinematic extends Check {

    private double lastDeltaPitch;
    private double lastDeltaPitchAccel;
    private double lastDeltaYaw;
    private double lastDeltaYawAccel;
    private int ticks;

    public Cinematic(PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(@NotNull RotationEvent event, long timestamp) {
        double deltaYaw = event.getDeltaYaw();
        double deltaPitch = event.getDeltaPitch();

        double deltaYawAccel = Math.abs(deltaYaw - lastDeltaYaw);
        double deltaPitchAccel = Math.abs(deltaPitch - lastDeltaPitch);

        double fromYaw = event.getFrom().getYaw();
        double fromPitch = event.getFrom().getPitch();

        float sensitivityFactor = playerData.getSensitivity() * 0.6F + 0.2F;
        float sensitivityMultiplier = sensitivityFactor * sensitivityFactor * sensitivityFactor * 8.0F;
        float adjustedYaw = (float) (fromYaw * sensitivityMultiplier);
        float adjustedPitch = (float) (fromPitch * sensitivityMultiplier);
        float[] predictedAngles = getAngles(adjustedYaw, adjustedPitch);

        playerData.setPredictYaw(predictedAngles[0]);
        playerData.setPredictPitch(predictedAngles[1]);

        if (!isNearlySame(deltaPitch, lastDeltaPitch)
                && !isNearlySame(deltaPitchAccel, lastDeltaPitchAccel)
                && !isNearlySame(deltaYaw, deltaYawAccel)
                && !isNearlySame(deltaYawAccel, lastDeltaYawAccel)) {
            ticks = Math.max(ticks - 1, 0);
            playerData.setCinematic(ticks > 1);

            if (ticks > 1) {
                playerData.setTimestamp(ActionType.CINEMATIC);
            }
        } else {
            ticks = Math.min(80, ticks + 1);

            if (ticks >= 3) {
                playerData.setCinematic(true);
                playerData.setTimestamp(ActionType.CINEMATIC);
            }
        }

        lastDeltaPitch = deltaPitch;
        lastDeltaPitchAccel = deltaPitchAccel;
        lastDeltaYaw = deltaYaw;
        lastDeltaYawAccel = deltaYawAccel;
    }

    private boolean isNearlySame(double d1, double d2) {
        double sensitivity = playerData.getSensitivity();
        double max = sensitivity >= 100 ? 0.0425 * playerData.getSensitivityY() * 3.1 : 0.0325;

        if (sensitivity >= 160 && Math.abs(d1 - d2) > 1.0 && Math.abs(d1 - d2) < 8.0) {
            return true;
        } else {
            return Math.abs(d1 - d2) < max && Math.abs(d1 - d2) > 0.0015;
        }
    }

    @Contract(value = "_, _ -> new", pure = true)
    private float @NotNull [] getAngles(float yaw, float pitch) {
        float yaw2 = (float) (yaw * 0.15);
        float pitch2 = (float) (pitch * -0.15);
        return new float[]{yaw2, pitch2};
    }
}
