package net.foulest.vulture.check.type.aimassist.data;

import net.foulest.vulture.action.ActionType;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.event.RotationEvent;
import net.foulest.vulture.util.MathUtil;
import net.foulest.vulture.util.data.EvictingList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@CheckInfo(name = "Sensitivity", type = CheckType.AIMASSIST,
        description = "Calculates the player's sensitivity.", punishable = false)
public class Sensitivity extends Check {

    private final List<Float> pitchSamples = new EvictingList<>(50);
    private float lastPitch;

    public Sensitivity(PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(@NotNull RotationEvent event, long timestamp) {
        float deltaPitch = event.getDeltaPitch();

        if (playerData.getTicksSince(ActionType.TELEPORT) > 2 && Math.abs(deltaPitch - lastPitch) < 4.0F) {
            float pitchGcd = MathUtil.getGcd(deltaPitch, lastPitch);

            if (pitchGcd > 0.009F) {
                pitchSamples.add(pitchGcd);

                if (pitchSamples.size() == 50) {
                    float pitchMode = MathUtil.getMode(pitchSamples);
                    float sensitivityY = convertToMouseDelta(pitchMode);
                    int sensitivityPercent = (int) (sensitivityY * 200.0F);

                    playerData.setSensitivityY(sensitivityY);
                    playerData.setSensitivity(sensitivityPercent);
                }
            }
        }

        lastPitch = deltaPitch;
    }

    private float convertToMouseDelta(float value) {
        return ((float) Math.cbrt(value / 0.15F / 8.0F) - 0.2F) / 0.6F;
    }
}
