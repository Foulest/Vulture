package net.foulest.vulture.listeners;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import net.foulest.vulture.util.MessageUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.RemoteServerCommandEvent;
import org.bukkit.event.server.ServerCommandEvent;

import java.util.Arrays;
import java.util.List;
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

    // TODO: Make a blocked-commands config setting.

    private static final List<Pattern> BLACKLISTED_COMMANDS = Arrays.asList(
            // WorldEdit
            Pattern.compile("/calc"),
            Pattern.compile("/eval"),
            Pattern.compile("/solve"),

            // Holographic Displays
            Pattern.compile("/h.* readtext"),

            // PermissionsEx
            Pattern.compile("/pe.*x promote"),
            Pattern.compile("/pe.*x demote"),
            Pattern.compile("/promote"),
            Pattern.compile("/demote"),

            // Multiverse
            Pattern.compile("/m.*v.* \\^"),
            Pattern.compile("/m.*v.*help <"),
            Pattern.compile("/\\$")
    );

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

    private void processCommand(@NonNull Cancellable event, @NonNull String command, @NonNull CommandSender sender) {
        for (Pattern pattern : BLACKLISTED_COMMANDS) {
            if (pattern.matcher(command).find()) {
                event.setCancelled(true);
                MessageUtil.messagePlayer(sender, "&cThat command is not allowed.");
                return;
            }
        }
    }
}
