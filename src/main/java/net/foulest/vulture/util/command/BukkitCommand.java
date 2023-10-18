package net.foulest.vulture.util.command;

import lombok.NonNull;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.Plugin;

import java.util.List;

/**
 * @author minnymin3
 * @project Vulture
 * <p>
 * <a href="https://github.com/mcardy/CommandFramework">...</a>
 */
public class BukkitCommand extends org.bukkit.command.Command {

    private final Plugin owningPlugin;
    private final CommandExecutor executor;
    protected BukkitCompleter completer;

    protected BukkitCommand(@NonNull String label, @NonNull CommandExecutor executor, @NonNull Plugin owner) {
        super(label);
        this.executor = executor;
        owningPlugin = owner;
        usageMessage = "";
    }

    @Override
    public boolean execute(@NonNull CommandSender sender, @NonNull String commandLabel, @NonNull String[] args) {
        boolean success;

        if (!owningPlugin.isEnabled()) {
            return false;
        }

        if (!testPermission(sender)) {
            return true;
        }

        success = executor.onCommand(sender, this, commandLabel, args);

        if (!success && !usageMessage.isEmpty()) {
            for (String line : usageMessage.replace("<command>", commandLabel).split("\n")) {
                sender.sendMessage(line);
            }
        }
        return success;
    }

    @Override
    public List<String> tabComplete(@NonNull CommandSender sender, @NonNull String alias, @NonNull String[] args) {
        List<String> completions = null;

        if (completer != null) {
            completions = completer.onTabComplete(sender, this, alias, args);
        }

        if (completions == null && executor instanceof TabCompleter) {
            completions = ((TabCompleter) executor).onTabComplete(sender, this, alias, args);
        }

        if (completions == null) {
            return super.tabComplete(sender, alias, args);
        }
        return completions;
    }
}
