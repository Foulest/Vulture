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

import lombok.Getter;
import lombok.Setter;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents the arguments provided to a command handler method.
 * These arguments include the sender, the executed command, the command label, and the command arguments.
 * This class is part of the CommandFramework.
 *
 * @author minnymin3
 * @see <a href="https://github.com/mcardy/CommandFramework">CommandFramework GitHub</a>
 */
@Getter
@Setter
public class CommandArgs {

    private final CommandSender sender;
    private final org.bukkit.command.Command command;
    private final String label;
    private final String[] args;

    /**
     * Constructor for CommandArgs.
     *
     * @param sender     The CommandSender executing the command.
     * @param command    The executed command.
     * @param label      The label of the command.
     * @param args       The arguments provided to the command.
     * @param subCommand The number of sub-commands in the command.
     */
    CommandArgs(CommandSender sender, org.bukkit.command.Command command,
                String label, String @NotNull [] args, int subCommand) {
        String[] modArgs = new String[args.length - subCommand];

        if (args.length - subCommand >= 0) {
            System.arraycopy(args, subCommand, modArgs, 0, args.length - subCommand);
        }

        StringBuilder buffer = new StringBuilder();
        buffer.append(label);

        for (int x = 0; x < subCommand; x++) {
            buffer.append(".").append(args[x]);
        }

        String cmdLabel = buffer.toString();
        this.sender = sender;
        this.command = command;
        this.label = cmdLabel;
        this.args = modArgs;
    }

    /**
     * Get the command label with sub-commands replaced by spaces.
     *
     * @return The formatted command label.
     */
    String getLabel() {
        return label.replace(".", " ");
    }

    /**
     * Get the argument at the specified index.
     *
     * @param index The index of the argument.
     * @return The argument at the specified index.
     */
    public String getArgs(int index) {
        return args[index];
    }

    /**
     * Get the number of arguments.
     *
     * @return The number of arguments.
     */
    public int length() {
        return args.length;
    }

    /**
     * Check if the sender is a player.
     *
     * @return true if the sender is a player, false otherwise.
     */
    private boolean isPlayer() {
        return sender instanceof Player;
    }

    /**
     * Get the sender as a Player if the sender is a player.
     *
     * @return The sender as a Player or null if the sender is not a player.
     */
    public @Nullable Player getPlayer() {
        return isPlayer() ? ((Player) sender) : null;
    }
}
