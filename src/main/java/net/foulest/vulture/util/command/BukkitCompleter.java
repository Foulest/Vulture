/*
 * Vulture - an advanced anti-cheat plugin designed for Minecraft 1.8.9 servers.
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
package net.foulest.vulture.util.command;

import lombok.Data;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Custom Bukkit TabCompleter class that implements the TabCompleter interface.
 * This class is part of the CommandFramework.
 *
 * @author minnymin3
 * @see <a href="https://github.com/mcardy/CommandFramework">CommandFramework GitHub</a>
 */
@Data
public class BukkitCompleter implements TabCompleter {

    private final Map<String, Map.Entry<Method, Object>> completers = new HashMap<>();

    /**
     * Adds a TabCompleter method for a specific label.
     *
     * @param label  The label associated with the TabCompleter.
     * @param method The method to invoke for tab-completion.
     * @param obj    The object that contains the method to be invoked.
     */
    void addCompleter(String label, Method method, Object obj) {
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
            String labelLower = label.toLowerCase(Locale.ROOT);
            buffer.append(labelLower);

            for (int x = 0; x < i; x++) {
                if (!args[x].isEmpty() && !(" ").equals(args[x])) {
                    String argsLower = args[x].toLowerCase(Locale.ROOT);
                    buffer.append(".").append(argsLower);
                }
            }

            String cmdLabel = buffer.toString();

            if (completers.containsKey(cmdLabel)) {
                Map.Entry<Method, Object> entry = completers.get(cmdLabel);

                try {
                    Object entryValue = entry.getValue();
                    String[] split = cmdLabel.split("\\.");

                    return (List<String>) entry.getKey().invoke(entryValue,
                            new CommandArgs(sender, command, label, args, split.length - 1));
                } catch (IllegalArgumentException | InvocationTargetException | IllegalAccessException ex) {
                    ex.printStackTrace();
                }
            }
        }
        return Collections.emptyList();
    }
}
