package net.foulest.vulture.util.command;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author minnymin3
 * @project Vulture
 * <p>
 * <a href="https://github.com/mcardy/CommandFramework">...</a>
 */
@Getter
@Setter
public class CommandArgs {

    private final CommandSender sender;
    private final org.bukkit.command.Command command;
    private final String label;
    private final String[] args;

    protected CommandArgs(@NonNull CommandSender sender, @NonNull org.bukkit.command.Command command,
                          @NonNull String label, @NonNull String[] args, int subCommand) {
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
     * Gets the label including sub command labels of this command
     *
     * @return Something like 'test.subcommand'
     */
    public String getLabel() {
        return label.replaceAll("\\.", " ");
    }

    /**
     * Gets the argument at the specified index
     *
     * @param index The index to simpleGet
     * @return The string at the specified index
     */
    public String getArgs(int index) {
        return args[index];
    }

    /**
     * Returns the length of the command arguments
     *
     * @return int length of args
     */
    public int length() {
        return args.length;
    }

    public boolean isPlayer() {
        return sender instanceof Player;
    }

    public Player getPlayer() {
        return isPlayer() ? ((Player) sender) : null;
    }
}
