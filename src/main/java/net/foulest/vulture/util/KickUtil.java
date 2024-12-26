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

import io.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.event.eventtypes.CancellableEvent;
import io.github.retrooper.packetevents.packetwrappers.play.out.kickdisconnect.WrappedPacketOutKickDisconnect;
import io.netty.channel.Channel;
import lombok.Data;
import net.foulest.vulture.Vulture;
import org.bukkit.entity.Entity;
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
 */
@Data
public class KickUtil {

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
    private static void kick(@NotNull Player player,
                             String debugMessage,
                             String reason, boolean announceKick) {
        UUID uniqueId = player.getUniqueId();

        synchronized (currentlyKicking) {
            if (currentlyKicking.contains(uniqueId)) {
                // Already in the process of kicking this player.
                return;
            }

            currentlyKicking.add(uniqueId);
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
                    // Forcefully terminate the player's connection.
                    if (Vulture.getInstance().getPledge().getChannel(player).isPresent()) {
                        Channel channel = Vulture.getInstance().getPledge().getChannel(player).get();
                        channel.disconnect(); // Disconnect the player
                        channel.close(); // Close the channel
                    } else {
                        // The player's pledge channel is not present... what?
                        // Kick them using raw packets instead.
                        PacketEvents.getInstance().getPlayerUtils().sendPacket(player, new WrappedPacketOutKickDisconnect(reason));
                    }
                }

                // Run the task later to avoid kicking the player multiple times.
                TaskUtil.runTaskLater(() -> currentlyKicking.remove(uniqueId), 10L);
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
        synchronized (currentlyKicking) {
            UUID uniqueId = entity.getUniqueId();
            return currentlyKicking.contains(uniqueId);
        }
    }
}
