package net.foulest.vulture.util;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.foulest.vulture.Vulture;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.data.PlayerDataManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for sending messages.
 *
 * @author Foulest
 * @project Vulture
 */
@Getter
@Setter
@SuppressWarnings("unused")
public final class MessageUtil {

    public static Logger logger = Bukkit.getLogger();

    /**
     * Sends a message to the specified player.
     *
     * @param sender  The player to send the message to.
     * @param message The message to send.
     */
    public static void messagePlayer(@NonNull CommandSender sender, @NonNull String message) {
        sender.sendMessage(colorize(message));
    }

    /**
     * Logs a message to the console.
     *
     * @param level   The level to log the message at.
     * @param message The message to log.
     */
    public static void log(@NonNull Level level, @NonNull String message) {
        logger.log(level, "[Vulture] " + message);
    }

    /**
     * Logs a debug message to the console.
     *
     * @param message The message to log.
     */
    public static void debug(@NonNull String message) {
        if (Vulture.instance.debug) {
            log(Level.INFO, "[DEBUG] " + message);
        }
    }

    /**
     * Broadcasts a message to all online players.
     *
     * @param message The message to send.
     */
    public static void broadcast(@NonNull String message) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            messagePlayer(player, message);
        }

        messagePlayer(Bukkit.getConsoleSender(), message);
    }

    /**
     * Broadcasts a list of messages to all online players.
     *
     * @param message The list of messages to send.
     */
    public static void broadcastList(@NonNull List<String> message) {
        for (String line : message) {
            broadcast(line);
        }
    }

    /**
     * Sends an alert to all online players with alerts enabled.
     *
     * @param message The message to send.
     */
    public static void sendAlert(@NonNull String message) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerData playerData = PlayerDataManager.getPlayerData(player);

            if (playerData.isAlertsEnabled()) {
                messagePlayer(player, Settings.prefix + " " + message);
            }
        }

        messagePlayer(Bukkit.getConsoleSender(), Settings.prefix + " " + message);
    }

    /**
     * Sends an alert to all online players with a specified permission.
     *
     * @param message    The message to send.
     * @param permission The permission to check.
     */
    public static void broadcastWithPerm(@NonNull String message, @NonNull String permission) {
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.hasPermission(permission)) {
                messagePlayer(online, message);
            }
        }

        messagePlayer(Bukkit.getConsoleSender(), message);
    }

    /**
     * Colorizes the specified message.
     *
     * @param message The message to colorize.
     */
    public static String colorize(@NonNull String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    /**
     * Strips the color from the specified message.
     *
     * @param message The message to strip the color from.
     */
    public static String stripColor(@NonNull String message) {
        return ChatColor.stripColor(message);
    }
}
