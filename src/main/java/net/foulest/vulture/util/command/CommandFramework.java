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

import lombok.Data;
import net.foulest.vulture.util.ConstantUtil;
import net.foulest.vulture.util.MessageUtil;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Level;

/**
 * Command framework for Bukkit, allowing easy registration of commands and command completers.
 * This class is part of the CommandFramework.
 *
 * @author minnymin3
 * @see <a href="https://github.com/mcardy/CommandFramework">CommandFramework GitHub</a>
 */
@Data
public class CommandFramework implements CommandExecutor {

    private final Map<String, Map.Entry<Method, Object>> commandMap = new HashMap<>();
    private final @NotNull Plugin plugin;
    private CommandMap map;

    /**
     * Constructor for the CommandFramework.
     *
     * @param plugin The plugin associated with this command framework.
     */
    public CommandFramework(@NotNull Plugin plugin) {
        this.plugin = plugin;

        if (plugin.getServer().getPluginManager() instanceof SimplePluginManager) {
            SimplePluginManager manager = (SimplePluginManager) plugin.getServer().getPluginManager();

            try {
                @NotNull Field field = SimplePluginManager.class.getDeclaredField("commandMap");
                field.setAccessible(true);
                map = (CommandMap) field.get(manager);
            } catch (IllegalArgumentException | NoSuchFieldException | IllegalAccessException | SecurityException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Default command handler for commands that are disabled on the server.
     *
     * @param args The CommandArgs object representing the command arguments.
     */
    private static void defaultCommand(@NotNull CommandArgs args) {
        @NotNull String label = args.getLabel();
        args.getSender().sendMessage(label + " is disabled on this server.");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             org.bukkit.command.@NotNull Command cmd,
                             @NotNull String label,
                             String @NotNull [] args) {
        handleCommand(sender, cmd, label, args);
        return true;
    }

    /**
     * Handles the execution of a command.
     *
     * @param sender The CommandSender executing the command.
     * @param cmd    The executed command.
     * @param label  The label of the command.
     * @param args   The arguments provided to the command.
     */
    private void handleCommand(@NotNull CommandSender sender,
                               org.bukkit.command.Command cmd,
                               @NotNull String label,
                               String @NotNull [] args) {
        for (int i = args.length; i >= 0; i--) {
            @NotNull StringBuilder buffer = new StringBuilder();

            @NotNull String labelLower = label.toLowerCase(Locale.ROOT);
            buffer.append(labelLower);

            for (int x = 0; x < i; x++) {
                @NotNull String argsLower = args[x].toLowerCase(Locale.ROOT);
                buffer.append(".").append(argsLower);
            }

            @NotNull String cmdLabel = buffer.toString();

            if (commandMap.containsKey(cmdLabel)) {
                Method key = commandMap.get(cmdLabel).getKey();
                Object value = commandMap.get(cmdLabel).getValue();
                Command command = key.getAnnotation(Command.class);
                String permission = command.permission();

                if (!permission.isEmpty() && !sender.hasPermission(permission)) {
                    String noPermission = command.noPermission();
                    MessageUtil.messagePlayer(sender, noPermission);
                    return;
                }

                if (command.inGameOnly() && !(sender instanceof Player)) {
                    MessageUtil.messagePlayer(sender, "&cOnly players may execute this command.");
                    return;
                }

                try {
                    String @NotNull [] split = cmdLabel.split("\\.");
                    key.invoke(value, new CommandArgs(sender, cmd, label, args, split.length - 1));
                } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException ex) {
                    ex.printStackTrace();
                }
                return;
            }
        }

        defaultCommand(new CommandArgs(sender, cmd, label, args, 0));
    }

    /**
     * Registers commands and completers from an object.
     *
     * @param obj The object containing command and completer methods.
     */
    public void registerCommands(@NotNull Object obj) {
        for (@NotNull Method method : obj.getClass().getMethods()) {
            Command command = method.getAnnotation(Command.class);
            Completer completer = method.getAnnotation(Completer.class);
            @NotNull String methodName = method.getName();

            boolean invalidMethod = false;

            if (command != null) {
                if (method.getParameterTypes().length > 1 || method.getParameterTypes()[0] != CommandArgs.class) {
                    MessageUtil.log(Level.WARNING, "Unable to register command: "
                            + methodName + "; unexpected method arguments.");
                    invalidMethod = true;
                }
            } else if (completer != null && (method.getParameterTypes().length != 1
                    || method.getParameterTypes()[0] != CommandArgs.class
                    || method.getReturnType() != List.class)) {
                MessageUtil.log(Level.WARNING, ConstantUtil.UNABLE_TO_REGISTER_TAB_COMPLETER
                        + methodName + "; unexpected method arguments or return type.");
                invalidMethod = true;
            }

            if (invalidMethod) {
                continue;
            }

            if (command != null) {
                String commandName = command.name();
                registerCommand(command, commandName, method, obj);

                for (@NotNull String alias : command.aliases()) {
                    registerCommand(command, alias, method, obj);
                }
            } else if (completer != null) {
                String commandName = completer.name();
                registerCompleter(commandName, method, obj);

                for (@NotNull String alias : completer.aliases()) {
                    registerCompleter(alias, method, obj);
                }
            }
        }
    }

    /**
     * Registers a command with the framework.
     *
     * @param command The command annotation.
     * @param label   The label of the command.
     * @param method  The method representing the command handler.
     * @param obj     The object containing the command method.
     */
    private void registerCommand(@NotNull Command command, @NotNull String label, Method method, Object obj) {
        String pluginName = plugin.getName();
        @NotNull String lowerCase = label.toLowerCase(Locale.ROOT);

        commandMap.put(lowerCase, new AbstractMap.SimpleEntry<>(method, obj));
        commandMap.put(pluginName + ':' + lowerCase, new AbstractMap.SimpleEntry<>(method, obj));

        @NotNull String cmdLabel = label.replace(".", ",").split(",")[0].toLowerCase(Locale.ROOT);
        org.bukkit.command.Command mapCommand = map.getCommand(cmdLabel);

        if (mapCommand == null) {
            org.bukkit.command.@NotNull Command cmd = new BukkitCommand(cmdLabel, this, plugin);
            map.register(pluginName, cmd);
        }

        String description = command.description();

        if (!description.isEmpty() && cmdLabel.equalsIgnoreCase(label) && mapCommand != null) {
            mapCommand.setDescription(description);
        }

        String usage = command.usage();

        if (!usage.isEmpty() && cmdLabel.equalsIgnoreCase(label) && mapCommand != null) {
            mapCommand.setUsage(usage);
        }
    }

    /**
     * Registers a tab completer with the framework.
     *
     * @param label  The label of the command.
     * @param method The method representing the tab completer.
     * @param obj    The object containing the tab completer method.
     */
    private void registerCompleter(@NotNull String label, @NotNull Method method, Object obj) {
        @NotNull String cmdLabel = label.replace(".", ",").split(",")[0].toLowerCase(Locale.ROOT);

        if (map.getCommand(cmdLabel) == null) {
            org.bukkit.command.@NotNull Command command = new BukkitCommand(cmdLabel, this, plugin);
            String pluginName = plugin.getName();
            map.register(pluginName, command);
        }

        @NotNull String methodName = method.getName();

        if (map.getCommand(cmdLabel) instanceof BukkitCommand) {
            BukkitCommand command = (BukkitCommand) map.getCommand(cmdLabel);

            if (command == null) {
                MessageUtil.log(Level.WARNING, ConstantUtil.UNABLE_TO_REGISTER_TAB_COMPLETER
                        + methodName + "; a command with that name doesn't exist.");
                return;
            }

            if (command.completer == null) {
                command.completer = new BukkitCompleter();
            }

            command.completer.addCompleter(label, method, obj);

        } else if (map.getCommand(cmdLabel) instanceof PluginCommand) {
            try {
                Object command = map.getCommand(cmdLabel);

                if (command == null) {
                    MessageUtil.log(Level.WARNING, ConstantUtil.UNABLE_TO_REGISTER_TAB_COMPLETER
                            + methodName + "; a command with that name doesn't exist.");
                    return;
                }

                @NotNull Field field = command.getClass().getDeclaredField("completer");
                field.setAccessible(true);

                if (field.get(command) == null) {
                    @NotNull BukkitCompleter completer = new BukkitCompleter();
                    completer.addCompleter(label, method, obj);
                    field.set(command, completer);

                } else if (field.get(command) instanceof BukkitCompleter) {
                    BukkitCompleter completer = (BukkitCompleter) field.get(command);
                    completer.addCompleter(label, method, obj);

                } else {
                    MessageUtil.log(Level.WARNING, ConstantUtil.UNABLE_TO_REGISTER_TAB_COMPLETER
                            + methodName + "; a tab completer is already registered for that command.");
                }
            } catch (IllegalAccessException | NoSuchFieldException ex) {
                ex.printStackTrace();
            }
        }
    }
}
