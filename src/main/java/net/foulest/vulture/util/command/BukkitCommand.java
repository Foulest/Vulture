/*
 * Vulture - a server protection plugin designed for Minecraft 1.8.9 servers.
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

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Custom Bukkit command class that extends org.bukkit.command.Command.
 * This class is part of the CommandFramework.
 *
 * @author minnymin3
 * @see <a href="https://github.com/mcardy/CommandFramework">CommandFramework GitHub</a>
 */
class BukkitCommand extends org.bukkit.command.Command {

    private final Plugin owningPlugin;
    private final CommandExecutor executor;
    BukkitCompleter completer;

    /**
     * Constructs a BukkitCommand instance.
     *
     * @param label    The label associated with this command.
     * @param executor The CommandExecutor responsible for handling this command.
     * @param owner    The owning Plugin of this command.
     */
    BukkitCommand(String label, CommandExecutor executor, Plugin owner) {
        super(label);
        this.executor = executor;
        owningPlugin = owner;
        usageMessage = "";
    }

    /**
     * Executes the command when it is run by a player or the console.
     *
     * @param sender       The CommandSender executing the command.
     * @param commandLabel The alias used to run the command.
     * @param args         The arguments provided with the command.
     * @return true if the command executed successfully, false otherwise.
     */
    @Override
    public boolean execute(@NotNull CommandSender sender,
                           @NotNull String commandLabel,
                           String[] args) {
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

    /**
     * Tab-completes the command when the player presses the tab key.
     *
     * @param sender The CommandSender tab-completing the command.
     * @param alias  The alias used to run the command.
     * @param args   The arguments provided for tab-completion.
     * @return A list of tab-completions or null if no completions are available.
     */
    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender,
                                             @NotNull String alias,
                                             String[] args) {
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
