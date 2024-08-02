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
package net.foulest.vulture.check.type.flight;

import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.event.MovementEvent;

@CheckInfo(name = "Flight (D)", type = CheckType.FLIGHT,
        description = "Detects players continually rising.")
public class FlightD extends Check {

    private double lastDeltaY;
    private double buffer;
    private int ticksRising;

    public FlightD(PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(MovementEvent event, long timestamp) {
        // Checks the player for exemptions.
        if (player.isFlying()
                || playerData.isNearClimbable()
                || playerData.isNearLiquid()
                || playerData.getVelocityY() > 0
                || playerData.getLastVelocityY() > 0
                || event.isTeleport(playerData)) {
            buffer = 0;
            return;
        }

        double deltaY = event.getDeltaY();

        // Ignores false positives with slabs and stairs.
        if (deltaY == 0.5 && lastDeltaY == 0.5
                && (playerData.isNearStairs() || playerData.isNearSlab())) {
            return;
        }

        // Checks if the player is continually rising for more than 2 ticks.
        if (deltaY > 0 && deltaY >= lastDeltaY) {
            if (++ticksRising > 2) {
                if (++buffer >= 2) {
                    flag(true, "deltaY=" + deltaY
                            + " lastDeltaY=" + lastDeltaY
                            + " ticksRising=" + ticksRising);
                }
            } else {
                buffer = Math.max(buffer - 0.25, 0);
            }
        } else {
            ticksRising = 0;
        }

        lastDeltaY = deltaY;
    }
}
