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
package net.foulest.vulture.util;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.foulest.vulture.action.ActionType;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.data.PlayerDataManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class SetbackUtil {

    /**
     * Sets the player back to their last on ground location.
     *
     * @param player The player to set back.
     */
    static void setback(Player player) {
        PlayerData playerData = PlayerDataManager.getPlayerData(player);
        playerData.setTimestamp(ActionType.SETBACK);

        if (playerData.getTimestamp(ActionType.LAST_ON_GROUND_LOCATION_SET) == 0) {
            setback(player, player.getLocation());
        } else {
            setback(player, playerData.getLastOnGroundLocation());
        }
    }

    /**
     * Sets the player back to the specified location.
     *
     * @param player   The player to set back.
     * @param location The location to set the player back to.
     */
    private static void setback(@NotNull Player player, Location location) {
        if (player.isInsideVehicle()) {
            return;
        }

        TaskUtil.runTask(() -> {
            location.setPitch(player.getLocation().getPitch());
            location.setYaw(player.getLocation().getYaw());

            player.setVelocity(new Vector(player.getVelocity().getX(), 0, player.getVelocity().getZ()));
            player.teleport(location, PlayerTeleportEvent.TeleportCause.UNKNOWN);
            player.setVelocity(new Vector(player.getVelocity().getX(), 0, player.getVelocity().getZ()));
        });
    }
}
