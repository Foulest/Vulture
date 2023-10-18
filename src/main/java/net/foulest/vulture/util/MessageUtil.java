package net.foulest.vulture.util;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.foulest.vulture.Vulture;
import net.foulest.vulture.data.DataManager;
import net.foulest.vulture.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.logging.Level;

/**
 * Utility class for sending messages.
 *
 * @author Foulest
 * @project Vulture
 */
@Getter
@Setter
public final class MessageUtil {

    public static void messagePlayer(@NonNull CommandSender sender, @NonNull String message) {
        sender.sendMessage(colorize(message));
    }

    public static void log(@NonNull Level level, @NonNull String message) {
        Bukkit.getLogger().log(level, "[" + Vulture.instance.getPluginName() + "] " + message);
    }

    public static void broadcast(@NonNull String message) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            messagePlayer(player, message);
        }

        messagePlayer(Bukkit.getConsoleSender(), message);
    }

    public static void broadcastList(@NonNull List<String> message) {
        for (String line : message) {
            broadcast(line);
        }
    }

    public static void sendAlert(@NonNull String message) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerData playerData = DataManager.getPlayerData(player);

            if (playerData.isAlertsEnabled()) {
                messagePlayer(player, Settings.prefix + " " + message);
            }
        }

        messagePlayer(Bukkit.getConsoleSender(), Settings.prefix + " " + message);
    }

    public static void broadcastWithPerm(@NonNull String message, @NonNull String permission) {
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.hasPermission(permission)) {
                messagePlayer(online, message);
            }
        }

        messagePlayer(Bukkit.getConsoleSender(), message);
    }

    public static String colorize(@NonNull String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static String stripColor(@NonNull String message) {
        return ChatColor.stripColor(message);
    }
}
