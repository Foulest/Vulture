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

import net.foulest.vulture.action.ActionType;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.event.MovementEvent;
import net.foulest.vulture.util.MovementUtil;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

@CheckInfo(name = "Speed (D)", type = CheckType.SPEED,
        description = "Prevents players from moving with an open inventory.")
public class SpeedD extends Check {

    private double buffer;

    public SpeedD(PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(MovementEvent event, long timestamp) {
        // Checks the player for exemptions.
        if (player.isFlying()) {
            return;
        }

        Vector velocity = player.getVelocity();

        float speedLevel = MovementUtil.getPotionEffectLevel(player, PotionEffectType.SPEED);
        float slownessLevel = MovementUtil.getPotionEffectLevel(player, PotionEffectType.SPEED);

        long timeSinceOpen = playerData.getTicksSince(ActionType.INVENTORY_OPEN);

        double deltaXZ = event.getDeltaXZ();
        double maxSpeed = player.getInventory().getType() == InventoryType.PLAYER ? 0.1 : 0.16;

        maxSpeed += playerData.getGroundTicks() < 5 ? speedLevel * 0.07 : speedLevel * 0.0573;
        maxSpeed -= playerData.getGroundTicks() < 5 ? slownessLevel * 0.07 : slownessLevel * 0.0573;
        maxSpeed += Math.abs(playerData.getVelocityXZ().getLast());
        maxSpeed += Math.abs(velocity.getX());
        maxSpeed += Math.abs(velocity.getZ());

        // Detects moving while inventory is open.
        if (playerData.isInventoryOpen() && deltaXZ > maxSpeed && timeSinceOpen > 500) {
            ++buffer;

            if (buffer >= 5) {
                flag(true, "deltaXZ=" + deltaXZ
                        + " maxSpeed=" + maxSpeed
                        + " timeSinceOpen=" + timeSinceOpen);
            }
        } else {
            buffer = Math.max(buffer - 0.25, 0);
        }
    }
}
