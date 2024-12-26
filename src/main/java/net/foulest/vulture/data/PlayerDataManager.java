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
package net.foulest.vulture.data;

import lombok.Data;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckManager;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
public class PlayerDataManager {

    // Map of player UUIDs to their stored data.
    private static final Map<UUID, PlayerData> playerDataMap = new HashMap<>();

    /**
     * Gets a player's data from the map.
     *
     * @param player The player to get.
     * @return The player's data.
     */
    public static PlayerData getPlayerData(@NotNull Player player) {
        UUID uniqueId = player.getUniqueId();

        if (playerDataMap.containsKey(uniqueId)) {
            return playerDataMap.get(uniqueId);
        } else {
            addPlayerData(player);
        }
        return playerDataMap.get(uniqueId);
    }

    /**
     * Adds a player's data to the map.
     *
     * @param player The player to add.
     */
    private static void addPlayerData(@NotNull Player player) {
        UUID uniqueId = player.getUniqueId();

        // Prevents duplicate entries.
        if (playerDataMap.containsKey(uniqueId)) {
            return;
        }

        PlayerData data = new PlayerData(uniqueId, player);

        // Initialize checks for the player.
        for (Class<? extends Check> checkClass : CheckManager.CHECK_CLASSES) {
            try {
                Constructor<? extends Check> constructor = checkClass.getConstructor(PlayerData.class);
                Check checkInstance = constructor.newInstance(data);
                data.getChecks().add(checkInstance);
            } catch (NoSuchMethodException | IllegalAccessException | InstantiationException
                     | InvocationTargetException ex) {
                ex.printStackTrace();
            }
        }

        // Add the player's data to the map.
        playerDataMap.put(uniqueId, data);
    }

    /**
     * Removes a player's data from the map.
     *
     * @param player The player to remove.
     */
    public static void removePlayerData(@NotNull Player player) {
        UUID uniqueId = player.getUniqueId();
        playerDataMap.remove(uniqueId);
    }
}
