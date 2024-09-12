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
package net.foulest.vulture.listeners;

import lombok.AllArgsConstructor;
import net.foulest.vulture.util.MessageUtil;
import net.foulest.vulture.util.Settings;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatTabCompleteEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.RemoteServerCommandEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

/**
 * Block commands that are not allowed.
 * This prevents exploits with out-of-date plugins.
 *
 * @author Foulest
 */
@AllArgsConstructor
public class CommandListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public static void onServerCommand(ServerCommandEvent event) {
        processCommand(event, event.getCommand(), event.getSender());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public static void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        processCommand(event, event.getMessage(), event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public static void onRemoteServerCommand(RemoteServerCommandEvent event) {
        processCommand(event, event.getCommand(), event.getSender());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public static void onTabComplete(@NotNull PlayerChatTabCompleteEvent event) {
        if (!event.getPlayer().hasPermission("vulture.blocked.commands.bypass")) {
            event.getTabCompletions().removeIf(completion -> {
                for (String command : Settings.blockedCommands) {
                    if (Pattern.compile(command).matcher(completion).find()) {
                        return true;
                    }
                }
                return false;
            });
        }
    }

    /**
     * Processes a command and cancels it if it is blocked.
     *
     * @param event   The event.
     * @param command The command.
     * @param sender  The sender.
     */
    private static void processCommand(Cancellable event, CharSequence command, CommandSender sender) {
        // Check if the command is blocked.
        for (String line : Settings.blockedCommands) {
            Pattern pattern = Pattern.compile(line);

            // If the command matches the pattern, cancel it.
            if (pattern.matcher(command).find()) {
                if ((sender instanceof Player
                        && sender.hasPermission("vulture.blocked.commands.bypass"))
                        || sender instanceof ConsoleCommandSender) {
                    continue;
                }

                event.setCancelled(true);
                MessageUtil.messagePlayer(sender, "Unknown command. Type \"/help\" for help.");
                return;
            }
        }
    }
}
