package net.foulest.vulture.util.command;

import lombok.Getter;
import lombok.Setter;
import net.foulest.vulture.util.MessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.Map.Entry;

/**
 * Custom Bukkit TabCompleter class that implements the TabCompleter interface.
 * This class is part of the CommandFramework.
 *
 * @author minnymin3
 * @see <a href="https://github.com/mcardy/CommandFramework">CommandFramework GitHub</a>
 */
@Getter
@Setter
public class BukkitCompleter implements TabCompleter {

    private final Map<String, Entry<Method, Object>> completers = new HashMap<>();

    /**
     * Adds a TabCompleter method for a specific label.
     *
     * @param label  The label associated with the TabCompleter.
     * @param method The method to invoke for tab-completion.
     * @param obj    The object that contains the method to be invoked.
     */
    public void addCompleter(String label, Method method, Object obj) {
        completers.put(label, new AbstractMap.SimpleEntry<>(method, obj));
    }

    /**
     * Handles tab-completion for commands.
     *
     * @param sender  The CommandSender requesting tab-completion.
     * @param command The Command object being tab-completed.
     * @param label   The label used for the command.
     * @param args    The arguments provided for tab-completion.
     * @return A list of tab-completions or an empty list if no completions are available.
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<String> onTabComplete(@NotNull CommandSender sender,
                                      @NotNull Command command,
                                      @NotNull String label,
                                      String @NotNull [] args) {
        for (int i = args.length; i >= 0; i--) {
            StringBuilder buffer = new StringBuilder();
            buffer.append(label.toLowerCase());

            for (int x = 0; x < i; x++) {
                if (!args[x].isEmpty() && !(" ").equals(args[x])) {
                    buffer.append(".").append(args[x].toLowerCase());
                }
            }

            String cmdLabel = buffer.toString();

            if (completers.containsKey(cmdLabel)) {
                Entry<Method, Object> entry = completers.get(cmdLabel);

                try {
                    return (List<String>) entry.getKey().invoke(entry.getValue(),
                            new CommandArgs(sender, command, label, args, cmdLabel.split("\\.").length - 1));
                } catch (IllegalArgumentException | InvocationTargetException | IllegalAccessException ex) {
                    ex.printStackTrace();
                }
            }
        }
        return Collections.emptyList();
    }
}
