package net.foulest.vulture.check.type.velocity;

import lombok.NonNull;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.event.MovementEvent;
import net.foulest.vulture.util.MathUtil;

@CheckInfo(name = "Velocity (A)", type = CheckType.VELOCITY)
public class VelocityA extends Check {

    private int buffer;

    public VelocityA(@NonNull PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(@NonNull MovementEvent event, long timestamp) {
        // Checks the player for exemptions.
        if (playerData.isNearLiquid()
                || playerData.isUnderBlock()
                || playerData.isInWeb()
                || playerData.isOnClimbable()
                || player.isInsideVehicle()) {
            return;
        }

        long transPing = playerData.getTransPing();

        int totalTicks = playerData.getTotalTicks();
        int velocityTicks = playerData.getVelocityTicks();
        int lastServerPositionTick = playerData.getLastServerPositionTick();

        double velocityY = playerData.getVelocityY();
        double deltaY = event.getDeltaY();
        double lastLastY = playerData.getLastLastLocation().getY();

        if (velocityY > 0.2 && lastLastY % 0.015625 == 0.0) {
            double scaledVelocity = (deltaY / velocityY) * 100.0 + 0.01;

            int velTicks = totalTicks - velocityTicks;
            int limit = MathUtil.getPingInTicks(transPing) + 5;

            if (velTicks <= MathUtil.getPingInTicks(transPing) + 2 && lastServerPositionTick > 55) {
                if (scaledVelocity < 75.0 || scaledVelocity > 105.0) {
                    if (++buffer >= limit) {
                        flag(false, "velocity=" + scaledVelocity + "%");
                    }
                } else {
                    buffer = 0;
                }
            }
        }
    }
}
