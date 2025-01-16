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
package net.foulest.vulture.util;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.CancellableEvent;
import lombok.Data;
import net.foulest.vulture.Vulture;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility class for kicking players.
 *
 * @author Foulest
 */
@Data
public class KickUtil {

    private static final Set<UUID> currentlyKicking = ConcurrentHashMap.newKeySet();

    public static void kickPlayer(@NotNull Player player,
                                  @NotNull String debugMessage) {
        kick(player, debugMessage, "Disconnected", true);
    }

    public static void kickPlayer(@NotNull Player player,
                                  @NotNull String debugMessage,
                                  @NotNull String reason) {
        kick(player, debugMessage, reason, true);
    }

    public static void kickPlayer(@NotNull Player player,
                                  @NotNull Cancellable event,
                                  @NotNull String debugMessage,
                                  @NotNull String reason) {
        event.setCancelled(true);
        kick(player, debugMessage, reason, true);
    }

    public static void kickPlayer(@NotNull Player player,
                                  @NotNull Cancellable event,
                                  @NotNull String debugMessage) {
        event.setCancelled(true);
        kick(player, debugMessage, "Disconnected", true);
    }

    public static void kickPlayer(@NotNull Player player,
                                  @NotNull Cancellable event,
                                  boolean valueToCheck,
                                  @NotNull String debugMessage) {
        if (valueToCheck) {
            event.setCancelled(true);
            kick(player, debugMessage, "Disconnected", true);
        }
    }

    public static void kickPlayer(@NotNull Player player,
                                  @NotNull CancellableEvent event,
                                  @NotNull String debugMessage,
                                  @NotNull String reason) {
        event.setCancelled(true);
        kick(player, debugMessage, reason, true);
    }

    public static void kickPlayer(@NotNull Player player,
                                  @NotNull CancellableEvent event,
                                  @NotNull String debugMessage) {
        event.setCancelled(true);
        kick(player, debugMessage, "Disconnected", true);
    }

    public static void kickPlayer(@NotNull Player player,
                                  @NotNull CancellableEvent event,
                                  boolean valueToCheck,
                                  @NotNull String debugMessage) {
        if (valueToCheck) {
            event.setCancelled(true);
            kick(player, debugMessage, "Disconnected", true);
        }
    }

    public static void kickPlayer(@NotNull Player player,
                                  @NotNull String debugMessage,
                                  boolean announceKick) {
        kick(player, debugMessage, "Disconnected", announceKick);
    }

    public static void kickPlayer(@NotNull Player player,
                                  @NotNull String debugMessage,
                                  boolean valueToCheck,
                                  boolean announceKick) {
        if (valueToCheck) {
            kick(player, debugMessage, "Disconnected", announceKick);
        }
    }

    public static void kickPlayer(@NotNull Player player,
                                  @NotNull String debugMessage,
                                  @NotNull String reason,
                                  boolean announceKick) {
        kick(player, debugMessage, reason, announceKick);
    }

    /**
     * Kicks the specified player synchronously with the given reason.
     *
     * @param player The player to be kicked.
     * @param reason The reason for kicking the player.
     */
    @SuppressWarnings("NestedMethodCall")
    private static void kick(@NotNull Player player,
                             @NotNull String debugMessage,
                             @NotNull String reason,
                             boolean announceKick) {
        UUID uniqueId = player.getUniqueId();

        // Use atomic operations on ConcurrentHashMap keySet
        if (!currentlyKicking.add(uniqueId)) {
            // Already in the process of kicking this player.
            return;
        }

        if (announceKick) {
            String playerName = player.getName();
            boolean debugMessageEmpty = debugMessage.isEmpty();

            MessageUtil.sendAlert("&f" + playerName + " &7has been kicked by Vulture."
                    + (debugMessageEmpty ? "" : " &8(" + debugMessage + "&8)"), "");
        }

        // Kicks the player with a message.
        if (Vulture.instance.isEnabled()) {
            TaskUtil.runTask(() -> {
                if (player.isOnline()) {
                    player.kickPlayer(MessageUtil.colorize(reason));
                }

                // If the player is still online, forcefully terminate their connection.
                if (player.isOnline()) {
                    PacketEvents.getAPI().getPlayerManager().getUser(player).closeConnection();
                }

                // Run the task later to avoid kicking the player multiple times.
                TaskUtil.runTaskLater(() -> currentlyKicking.remove(uniqueId), 20L);
            });
        } else {
            currentlyKicking.remove(uniqueId);
        }
    }

    /**
     * Checks if the specified player is being kicked.
     *
     * @param entity The player to check.
     * @return If the player is being kicked.
     */
    public static boolean isPlayerBeingKicked(@NotNull Entity entity) {
        UUID uniqueId = entity.getUniqueId();
        return currentlyKicking.contains(uniqueId);
    }
}
