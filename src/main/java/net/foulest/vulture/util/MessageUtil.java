package net.foulest.vulture.util;

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
     * Sends a message to the specified player.
     *
     * @param sender  The player to send the message to.
     * @param message The message to send.
     */
    public static void messagePlayerHoverable(CommandSender sender, List<String> hoverableText, String message) {
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
    public static void messagePlayerClickable(CommandSender sender, List<String> hoverableText,
                                              String command, String message) {
        // Sends a normal message if the sender is not a player.
        if (!(sender instanceof Player)) {
            sender.sendMessage(colorize(message));
            return;
        }

        Player player = (Player) sender;
        TextComponent textComponent = new TextComponent(MessageUtil.colorize(message));

        // Adds the hoverable text to the message.
        textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{
                new TextComponent(MessageUtil.colorize(String.join("\n", hoverableText)))
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
        if (Vulture.instance.debugMode) {
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
                messagePlayerClickable(player, Collections.singletonList("&aClick to teleport to the player."),
                        "/tp " + player.getName(), Settings.prefix + " " + message);
            }
        }

        messagePlayer(Bukkit.getConsoleSender(), Settings.prefix + " " + message);
    }
}
