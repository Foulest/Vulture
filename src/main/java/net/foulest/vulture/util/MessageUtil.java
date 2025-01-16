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

import lombok.Data;
import net.foulest.vulture.Vulture;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.data.PlayerDataManager;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for sending messages.
 *
 * @author Foulest
 */
@SuppressWarnings("WeakerAccess")
@Data
public class MessageUtil {

    private static final Logger logger = Bukkit.getLogger();

    /**
     * Logs a message to the console.
     *
     * @param level   The level to log the message at.
     * @param message The message to log.
     */
    public static void log(Level level, String message) {
        if (logger.isLoggable(level)) {
            logger.log(level, String.format("[Vulture] %s", message));
        }
    }

    /**
     * Sends a message to the specified player.
     *
     * @param sender  The player to send the message to.
     * @param message The message to send.
     */
    public static void messagePlayer(@NotNull CommandSender sender,
                                     String @NotNull ... message) {
        for (@NotNull String line : message) {
            sender.sendMessage(colorize(line));
        }
    }

    /**
     * Sends a message to the specified player.
     *
     * @param sender  The player to send the message to.
     * @param message The message to send.
     */
    public static void messagePlayerHoverable(CommandSender sender,
                                              @NotNull Iterable<String> hoverableText,
                                              @NotNull String message) {
        messagePlayerClickable(sender, hoverableText, "", message);
    }

    /**
     * Sends a hoverable message to the specified player
     * with a command to run when clicked.
     *
     * @param sender        The player to send the message to.
     * @param hoverableText The text to display when hovering over the message.
     * @param command       The command to run when the message is clicked.
     * @param message       The message to send.
     */
    private static void messagePlayerClickable(CommandSender sender,
                                               @NotNull Iterable<String> hoverableText,
                                               String command,
                                               @NotNull String message) {
        // Sends a normal message if the sender is not a player.
        if (!(sender instanceof Player)) {
            sender.sendMessage(colorize(message));
            return;
        }

        @NotNull Player player = (Player) sender;
        @NotNull TextComponent textComponent = new TextComponent(colorize(message));

        // Adds the hoverable text to the message.
        textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{
                new TextComponent(colorize(String.join("\n", hoverableText)))
        }));

        // Adds the command to run when the message is clicked.
        textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));

        // Sends the message to the player.
        player.spigot().sendMessage(textComponent);
    }

    /**
     * Broadcasts a message to all online players.
     *
     * @param message The message to send.
     */
    public static void broadcast(String @NotNull ... message) {
        for (String line : message) {
            for (@NotNull Player player : Bukkit.getOnlinePlayers()) {
                messagePlayer(player, line);
            }

            messagePlayer(Bukkit.getConsoleSender(), line);
        }
    }

    /**
     * Broadcasts a message to all online players.
     *
     * @param message The message to send.
     */
    static void broadcast(@NotNull Iterable<String> message) {
        for (String line : message) {
            for (@NotNull Player player : Bukkit.getOnlinePlayers()) {
                messagePlayer(player, line);
            }

            messagePlayer(Bukkit.getConsoleSender(), line);
        }
    }

    /**
     * Sends an alert to all online players with a specified permission.
     *
     * @param message    The message to send.
     * @param permission The permission to check.
     */
    public static void broadcastWithPerm(String permission, String @NotNull ... message) {
        for (String line : message) {
            for (@NotNull Player online : Bukkit.getOnlinePlayers()) {
                if (online.hasPermission(permission)) {
                    messagePlayer(online, line);
                }
            }

            messagePlayer(Bukkit.getConsoleSender(), line);
        }
    }

    /**
     * Translates the & symbol into Minecraft's native color code symbol (§).
     *
     * @param message The message to translate.
     * @return The translated message with § symbols.
     */
    @Contract("_ -> new")
    public static @NotNull String nativeColorCode(@NotNull String message) {
        return message.replace("&", "§");
    }

    /**
     * Colorizes the specified message.
     *
     * @param message The message to colorize.
     */
    @Contract("_ -> new")
    public static @NotNull String colorize(@NotNull String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    /**
     * Strips the color from the specified message.
     *
     * @param message The message to strip the color from.
     */
    public static String stripColor(String message) {
        return ChatColor.stripColor(message);
    }

    // Plugin specific methods

    /**
     * Logs a debug message to the console.
     *
     * @param message The message to log.
     */
    public static void debug(String message) {
        if (Vulture.instance.debugMode) {
            log(Level.INFO, "[DEBUG] " + message);
        }
    }

    /**
     * Sends an alert to all online players with alerts enabled.
     *
     * @param message The message to send.
     */
    public static void sendAlert(String message, @NotNull String verbose) {
        for (@NotNull Player player : Bukkit.getOnlinePlayers()) {
            PlayerData playerData = PlayerDataManager.getPlayerData(player);

            if (playerData.isAlertsEnabled()) {
                String playerName = player.getName();
                boolean verboseEnabled = playerData.isVerboseEnabled();
                boolean verboseEmpty = verbose.isEmpty();

                messagePlayerClickable(player, Collections.singletonList("&aClick to teleport to the player."),
                        "/tp " + playerName, Settings.prefix + " " + message
                                + (verboseEnabled && !verboseEmpty ? " " + verbose : ""));
            }
        }

        messagePlayer(Bukkit.getConsoleSender(), Settings.prefix + " " + message + " " + verbose);
    }
}
