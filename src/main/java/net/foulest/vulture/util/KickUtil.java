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

import io.github.retrooper.packetevents.event.eventtypes.CancellableEvent;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.foulest.vulture.Vulture;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Utility class for kicking players.
 *
 * @author Foulest
 * @project Vulture
 */
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class KickUtil {

    private static final Set<UUID> currentlyKicking = Collections.synchronizedSet(new HashSet<>());

    public static void kickPlayer(Player player, String debugMessage) {
        kick(player, debugMessage, "Disconnected", true);
    }

    public static void kickPlayer(Player player, String debugMessage, String reason) {
        kick(player, debugMessage, reason, true);
    }

    public static void kickPlayer(Player player, @NotNull Cancellable event,
                                  String debugMessage, String reason) {
        event.setCancelled(true);
        kick(player, debugMessage, reason, true);
    }

    public static void kickPlayer(Player player, @NotNull Cancellable event,
                                  String debugMessage) {
        event.setCancelled(true);
        kick(player, debugMessage, "Disconnected", true);
    }

    public static void kickPlayer(Player player, Cancellable event,
                                  boolean valueToCheck, String debugMessage) {
        if (valueToCheck) {
            event.setCancelled(true);
            kick(player, debugMessage, "Disconnected", true);
        }
    }

    public static void kickPlayer(Player player, @NotNull CancellableEvent event,
                                  String debugMessage, String reason) {
        event.setCancelled(true);
        kick(player, debugMessage, reason, true);
    }

    public static void kickPlayer(Player player, @NotNull CancellableEvent event,
                                  String debugMessage) {
        event.setCancelled(true);
        kick(player, debugMessage, "Disconnected", true);
    }

    public static void kickPlayer(Player player, CancellableEvent event,
                                  boolean valueToCheck, String debugMessage) {
        if (valueToCheck) {
            event.setCancelled(true);
            kick(player, debugMessage, "Disconnected", true);
        }
    }

    public static void kickPlayer(Player player, String debugMessage, boolean announceKick) {
        kick(player, debugMessage, "Disconnected", announceKick);
    }

    public static void kickPlayer(Player player, String debugMessage,
                                  boolean valueToCheck, boolean announceKick) {
        if (valueToCheck) {
            kick(player, debugMessage, "Disconnected", announceKick);
        }
    }

    public static void kickPlayer(Player player, String debugMessage,
                                  String reason, boolean announceKick) {
        kick(player, debugMessage, reason, announceKick);
    }

    /**
     * Kicks the specified player synchronously with the given reason.
     *
     * @param player The player to be kicked.
     * @param reason The reason for kicking the player.
     */
    public static void kick(@NotNull Player player,
                            String debugMessage,
                            String reason, boolean announceKick) {
        synchronized (currentlyKicking) {
            if (currentlyKicking.contains(player.getUniqueId())) {
                // Already in the process of kicking this player.
                return;
            }

            currentlyKicking.add(player.getUniqueId());
        }

        if (announceKick) {
            MessageUtil.sendAlert("&f" + player.getName() + " &7has been kicked by Vulture."
                    + (debugMessage.isEmpty() ? "" : " &8(" + debugMessage + "&8)"), "");
        }

        // Kicks the player with a message.
        if (Vulture.instance.isEnabled()) {
            TaskUtil.runTask(() -> {
                if (player.isOnline()) {
                    player.kickPlayer(MessageUtil.colorize(reason));
                }

                currentlyKicking.remove(player.getUniqueId());
            });
        } else {
            currentlyKicking.remove(player.getUniqueId());
        }
    }

    /**
     * Checks if the specified player is being kicked.
     *
     * @param player The player to check.
     * @return If the player is being kicked.
     */
    public static boolean isPlayerBeingKicked(@NotNull Player player) {
        synchronized (currentlyKicking) {
            return currentlyKicking.contains(player.getUniqueId());
        }
    }
}
