package net.foulest.vulture.util.command;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.foulest.vulture.util.MessageUtil;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

/**
 * @author minnymin3
 * @project Vulture
 * <p>
 * <a href="https://github.com/mcardy/CommandFramework">...</a>
 */
@Getter
@Setter
public class CommandFramework implements CommandExecutor {

    private final Map<String, Entry<Method, Object>> commandMap = new HashMap<>();
    private final Plugin plugin;
    private CommandMap map;

    /**
     * Initializes the command framework and sets up the command maps
     */
    public CommandFramework(@NonNull Plugin plugin) {
        this.plugin = plugin;

        if (plugin.getServer().getPluginManager() instanceof SimplePluginManager) {
            SimplePluginManager manager = (SimplePluginManager) plugin.getServer().getPluginManager();

            try {
                Field field = SimplePluginManager.class.getDeclaredField("commandMap");
                field.setAccessible(true);
                map = (CommandMap) field.get(manager);
            } catch (IllegalArgumentException | NoSuchFieldException | IllegalAccessException | SecurityException ex) {
                ex.printStackTrace();
            }
        }
    }

    private static void defaultCommand(@NonNull CommandArgs args) {
        args.getSender().sendMessage(args.getLabel() + " is disabled on this server.");
    }

    @Override
    public boolean onCommand(@NonNull CommandSender sender, @NonNull org.bukkit.command.Command cmd,
                             @NonNull String label, @NonNull String[] args) {
        handleCommand(sender, cmd, label, args);
        return true;
    }

    /**
     * Handles commands. Used in the onCommand method in your JavaPlugin class
     *
     * @param sender The {@link CommandSender} parsed from onCommand
     * @param cmd    The {@link org.bukkit.command.Command} parsed from onCommand
     * @param label  The label parsed from onCommand
     * @param args   The arguments parsed from onCommand
     */
    public void handleCommand(@NonNull CommandSender sender, @NonNull org.bukkit.command.Command cmd,
                              @NonNull String label, @NonNull String[] args) {
        for (int i = args.length; i >= 0; i--) {
            StringBuilder buffer = new StringBuilder();
            buffer.append(label.toLowerCase());

            for (int x = 0; x < i; x++) {
                buffer.append(".").append(args[x].toLowerCase());
            }

            String cmdLabel = buffer.toString();

            if (commandMap.containsKey(cmdLabel)) {
                Method key = commandMap.get(cmdLabel).getKey();
                Object value = commandMap.get(cmdLabel).getValue();
                Command command = key.getAnnotation(Command.class);

                if (!("").equals(command.permission()) && !sender.hasPermission(command.permission())) {
                    MessageUtil.messagePlayer(sender, command.noPermission());
                    return;
                }

                if (command.inGameOnly() && !(sender instanceof Player)) {
                    MessageUtil.messagePlayer(sender, "&cOnly players may execute this command.");
                    return;
                }

                try {
                    key.invoke(value, new CommandArgs(sender, cmd, label, args,
                            cmdLabel.split("\\.").length - 1));
                } catch (IllegalArgumentException | IllegalAccessException ex) {
                    ex.printStackTrace();
                } catch (InvocationTargetException ex) {
                    ex.getTargetException().printStackTrace();
                }
                return;
            }
        }

        defaultCommand(new CommandArgs(sender, cmd, label, args, 0));
    }

    /**
     * Registers all command and completer methods inside the object.
     * Similar to Bukkit's registerEvents method.
     *
     * @param obj The object to register the commands of
     */
    public void registerCommands(@NonNull Object obj) {
        for (Method method : obj.getClass().getMethods()) {
            if (method.getAnnotation(Command.class) != null) {
                Command command = method.getAnnotation(Command.class);

                if (method.getParameterTypes().length > 1 || method.getParameterTypes()[0] != CommandArgs.class) {
                    MessageUtil.log(Level.WARNING, "&cUnable to register command "
                            + method.getName() + ". Unexpected method arguments");
                    continue;
                }

                registerCommand(command, command.name(), method, obj);

                for (String alias : command.aliases()) {
                    registerCommand(command, alias, method, obj);
                }

            } else if (method.getAnnotation(Completer.class) != null) {
                Completer completer = method.getAnnotation(Completer.class);

                if (method.getParameterTypes().length != 1 || method.getParameterTypes()[0] != CommandArgs.class) {
                    MessageUtil.log(Level.WARNING, "Unable to register tab completer "
                            + method.getName() + ". Unexpected method arguments");
                    continue;
                }

                if (method.getReturnType() != List.class) {
                    MessageUtil.log(Level.WARNING, "Unable to register tab completer "
                            + method.getName() + ". Unexpected return type");
                    continue;
                }

                registerCompleter(completer.name(), method, obj);

                for (String alias : completer.aliases()) {
                    registerCompleter(alias, method, obj);
                }
            }
        }
    }

    public void registerCommand(@NonNull Command command, @NonNull String label,
                                @NonNull Method method, @NonNull Object obj) {
        commandMap.put(label.toLowerCase(), new AbstractMap.SimpleEntry<>(method, obj));
        commandMap.put(plugin.getName() + ':' + label.toLowerCase(), new AbstractMap.SimpleEntry<>(method, obj));

        String cmdLabel = label.replace(".", ",").split(",")[0].toLowerCase();

        if (map.getCommand(cmdLabel) == null) {
            org.bukkit.command.Command cmd = new BukkitCommand(cmdLabel, this, plugin);
            map.register(plugin.getName(), cmd);
        }

        if (!("").equalsIgnoreCase(command.description()) && cmdLabel.equalsIgnoreCase(label)) {
            map.getCommand(cmdLabel).setDescription(command.description());
        }

        if (!("").equalsIgnoreCase(command.usage()) && cmdLabel.equalsIgnoreCase(label)) {
            map.getCommand(cmdLabel).setUsage(command.usage());
        }
    }

    public void registerCompleter(@NonNull String label, @NonNull Method method, @NonNull Object obj) {
        String cmdLabel = label.replace(".", ",").split(",")[0].toLowerCase();

        if (map.getCommand(cmdLabel) == null) {
            org.bukkit.command.Command command = new BukkitCommand(cmdLabel, this, plugin);
            map.register(plugin.getName(), command);
        }

        if (map.getCommand(cmdLabel) instanceof BukkitCommand) {
            BukkitCommand command = (BukkitCommand) map.getCommand(cmdLabel);

            if (command.completer == null) {
                command.completer = new BukkitCompleter();
            }

            command.completer.addCompleter(label, method, obj);

        } else if (map.getCommand(cmdLabel) instanceof PluginCommand) {
            try {
                Object command = map.getCommand(cmdLabel);
                Field field = command.getClass().getDeclaredField("completer");
                field.setAccessible(true);

                if (field.get(command) == null) {
                    BukkitCompleter completer = new BukkitCompleter();
                    completer.addCompleter(label, method, obj);
                    field.set(command, completer);

                } else if (field.get(command) instanceof BukkitCompleter) {
                    BukkitCompleter completer = (BukkitCompleter) field.get(command);
                    completer.addCompleter(label, method, obj);

                } else {
                    MessageUtil.log(Level.WARNING, "&cUnable to register tab completer " + method.getName()
                            + ". A tab completer is already registered for that command!");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
