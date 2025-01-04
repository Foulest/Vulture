/*
 * Vulture - a server protection plugin designed for Minecraft 1.8.9 servers.
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
package net.foulest.vulture.listeners;

import net.foulest.packetevents.PacketEvents;
import net.foulest.packetevents.utils.player.ClientVersion;
import net.foulest.vulture.action.ActionType;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.data.PlayerDataManager;
import net.foulest.vulture.util.KickUtil;
import net.foulest.vulture.util.MessageUtil;
import net.foulest.vulture.util.Settings;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerDataListener implements Listener {

    /**
     * Handles player login events.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public static void onLogin(@NotNull PlayerLoginEvent event) {
        Player player = event.getPlayer();

        // Checks if the player's IP matches the whitelist.
        if (Settings.ipWhitelistEnabled) {
            String playerIp = event.getAddress().getHostAddress();
            UUID uniqueId = player.getUniqueId();
            List<String> whitelistedIps = Settings.ipWhitelist.getOrDefault(uniqueId, new ArrayList<>());

            // Kicks players with non-whitelisted IPs.
            if (!whitelistedIps.isEmpty() && !whitelistedIps.contains(playerIp)) {
                event.disallow(PlayerLoginEvent.Result.KICK_FULL,
                        MessageUtil.nativeColorCode("&cYour IP address does not match the whitelist."
                                + "\n\n&cContact an administrator for more information."));
            }
        }
    }

    /**
     * Handles player join events.
     *
     * @param event PlayerJoinEvent
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public static void onJoin(@NotNull PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerDataManager.getPlayerData(player);
        ClientVersion clientVersion = PacketEvents.getInstance().getPlayerUtils().getClientVersion(player);

        // Returns if the player's data is null.
        if (playerData == null) {
            KickUtil.kickPlayer(player, "Failed to load player data", true);
            return;
        }

        // Resolves the player's client version.
        if (!clientVersion.isResolved() || clientVersion == ClientVersion.UNKNOWN) {
            KickUtil.kickPlayer(player, "Failed to resolve client version", true);
        } else {
            playerData.setVersion(clientVersion);
        }
    }

    /**
     * Handles player quit events.
     *
     * @param event PlayerQuitEvent
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public static void onQuit(@NotNull PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PlayerDataManager.removePlayerData(player);
    }

    /**
     * Handles block break events.
     *
     * @param event BlockBreakEvent
     */
    @EventHandler
    public static void onBlockBreak(@NotNull BlockBreakEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerDataManager.getPlayerData(player);

        // Sets digging to false.
        playerData.setDigging(false);
    }

    /**
     * Handles player item consume events.
     *
     * @param event PlayerItemConsumeEvent
     */
    @EventHandler
    public static void onPlayerItemConsume(@NotNull PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerDataManager.getPlayerData(player);

        // Sets eating and drinking to false.
        playerData.setEating(false);
        playerData.setDrinking(false);
    }

    /**
     * Handles player respawn events.
     *
     * @param event PlayerRespawnEvent
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public static void onPlayerRespawn(@NotNull PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerDataManager.getPlayerData(player);

        // Sets sprinting and sneaking to false.
        playerData.setSprinting(false);
        playerData.setSneaking(false);
    }

    /**
     * Handles player damage events.
     *
     * @param event EntityDamageEvent
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public static void onDamage(@NotNull EntityDamageEvent event) {
        // Returns if the entity is not a player.
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();
        PlayerData playerData = PlayerDataManager.getPlayerData(player);

        // Sets the player's last damage timestamp.
        playerData.setTimestamp(ActionType.DAMAGE);
    }
}
