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

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTabComplete(@NotNull PlayerChatTabCompleteEvent event) {
        if (!event.getPlayer().hasPermission("vulture.blocked.commands.bypass")) {
            event.getTabCompletions().removeIf(completion -> {
                for (String string : Settings.blockedCommands) {
                    if (Pattern.compile(string).matcher(completion).find()) {
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
    private void processCommand(Cancellable event,
                                String command,
                                CommandSender sender) {
        // Check if the command is blocked.
        for (String string : Settings.blockedCommands) {
            Pattern pattern = Pattern.compile(string);

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
