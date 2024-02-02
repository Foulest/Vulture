package net.foulest.vulture.util;

import net.foulest.vulture.Vulture;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.data.PlayerDataManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for sending messages.
 *
 * @author Foulest
 * @project Vulture
 */
@SuppressWarnings("unused")
public final class MessageUtil {

    private static final Logger logger = Bukkit.getLogger();

    /**
     * Logs a message to the console.
     *
     * @param level   The level to log the message at.
     * @param message The message to log.
     */
    public static void log(Level level, String message) {
        logger.log(level, "[Vulture] " + message);
    }

    /**
     * Prints an exception's message as a warning to the console.
     *
     * @param ex The exception to print.
     */
    public static void printException(Throwable ex) {
        logger.log(Level.WARNING, "[Vulture] An error occurred: " + ex.getLocalizedMessage()
                + " (Caused by: " + ex.getCause() + ")");
    }

    /**
     * Sends a message to the specified player.
     *
     * @param sender  The player to send the message to.
     * @param message The message to send.
     */
    public static void messagePlayer(CommandSender sender, String @NotNull ... message) {
        for (String line : message) {
            sender.sendMessage(colorize(line));
        }
    }

    /**
     * Broadcasts a message to all online players.
     *
     * @param message The message to send.
     */
    public static void broadcast(String @NotNull ... message) {
        for (String line : message) {
            for (Player player : Bukkit.getOnlinePlayers()) {
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
    public static void broadcast(@NotNull List<String> message) {
        for (String line : message) {
            for (Player player : Bukkit.getOnlinePlayers()) {
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
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (online.hasPermission(permission)) {
                    messagePlayer(online, line);
                }
            }

            messagePlayer(Bukkit.getConsoleSender(), line);
        }
    }

    /**
     * Colorizes the specified message.
     *
     * @param message The message to colorize.
     */
    @Contract("_ -> new")
    public static @NotNull String colorize(String message) {
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
        if (Vulture.instance.debug) {
            log(Level.INFO, "[DEBUG] " + message);
        }
    }

    /**
     * Sends an alert to all online players with alerts enabled.
     *
     * @param message The message to send.
     */
    public static void sendAlert(String message) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerData playerData = PlayerDataManager.getPlayerData(player);

            if (playerData.isAlertsEnabled()) {
                messagePlayer(player, Settings.prefix + " " + message);
            }
        }

        messagePlayer(Bukkit.getConsoleSender(), Settings.prefix + " " + message);
    }
}
