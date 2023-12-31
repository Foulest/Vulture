package net.foulest.vulture.listeners;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import net.foulest.vulture.util.MessageUtil;
import net.foulest.vulture.util.Settings;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.RemoteServerCommandEvent;
import org.bukkit.event.server.ServerCommandEvent;

import java.util.regex.Pattern;

/**
 * Block commands that are not allowed.
 * This prevents exploits with out-of-date plugins.
 *
 * @author Foulest
 * @project Vulture
 */
@AllArgsConstructor
public class CommandListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onServerCommand(ServerCommandEvent event) {
        processCommand(event, event.getCommand(), event.getSender());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        processCommand(event, event.getMessage(), event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRemoteServerCommand(RemoteServerCommandEvent event) {
        processCommand(event, event.getCommand(), event.getSender());
    }

    /**
     * Processes a command and cancels it if it is blacklisted.
     *
     * @param event   The event.
     * @param command The command.
     * @param sender  The sender.
     */
    private void processCommand(@NonNull Cancellable event,
                       @NonNull String command,
                       @NonNull CommandSender sender) {
        // Check if the command is blacklisted.
        for (String string : Settings.blacklistedCommands) {
            Pattern pattern = Pattern.compile(string);

            // If the command matches the pattern, cancel it.
            if (pattern.matcher(command).find()) {
                event.setCancelled(true);
                MessageUtil.messagePlayer(sender, "&cThat command is not allowed.");
                return;
            }
        }
    }
}
