/*
 * Vulture - an advanced anti-cheat plugin designed for Minecraft 1.8.9 servers.
 * Copyright (C) 2024 Foulest (https://github.com/Foulest)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package net.foulest.vulture.check.type.speed;

import lombok.ToString;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.event.MovementEvent;
import org.jetbrains.annotations.NotNull;

@ToString
@CheckInfo(name = "Speed (B)", type = CheckType.SPEED)
public class SpeedB extends Check {

    private double buffer;
    private double lastDeltaXZ;

    public SpeedB(PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(@NotNull MovementEvent event, long timestamp) {
        double deltaXZ = event.getDeltaXZ();

        // Checks the player for exemptions.
        if (player.getAllowFlight()
                || playerData.isNearLiquid()
                || playerData.isOnGround()
                || playerData.isOnClimbable()
                || player.getWalkSpeed() != 0.2
                || playerData.isInWeb()
                || deltaXZ <= 0.005) {
            lastDeltaXZ = deltaXZ;
            return;
        }

        double diff = lastDeltaXZ * 0.91F + 0.02;

        if (playerData.isSprinting()) {
            diff += 0.0063;
        }

        double deltaXZDiff = deltaXZ - diff;

        if (deltaXZDiff > 0.0 && diff > 0.08 && deltaXZ > 0.15) {
            ++buffer;

            if (buffer > 8) {
                flag(true, "deltaXZ=" + deltaXZ
                        + " diff=" + diff
                        + " buffer=" + buffer);
                buffer /= 2;
            }
        } else {
            buffer = Math.max(buffer - 0.25, 0);
        }

        lastDeltaXZ = deltaXZ;
    }
}
