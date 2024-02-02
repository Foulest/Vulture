package net.foulest.vulture.cmds;

import lombok.Getter;
import lombok.Setter;
import net.foulest.vulture.Vulture;
import net.foulest.vulture.check.type.clientbrand.type.PayloadType;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.data.PlayerDataManager;
import net.foulest.vulture.util.KickUtil;
import net.foulest.vulture.util.MessageUtil;
import net.foulest.vulture.util.Settings;
import net.foulest.vulture.util.command.Command;
import net.foulest.vulture.util.command.CommandArgs;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

/**
 * Main command for Vulture.
 *
 * @author Foulest
 * @project Vulture
 */
@Getter
@Setter
public class VultureCmd {

    @Command(name = "vulture", description = "Main command for Vulture.",
            permission = "vulture.main", usage = "/vulture")
    public void onCommand(@NotNull CommandArgs args) {
        CommandSender sender = args.getSender();

        // No additional arguments, display help menu.
        if (args.length() == 0) {
            handleHelp(sender, args);
            return;
        }

        // Handle sub-commands.
        String subCommand = args.getArgs(0);
        switch (subCommand.toLowerCase()) {
            case "alerts":
                if (!(sender instanceof Player)) {
                    MessageUtil.messagePlayer(sender, "&cOnly players can use this command!");
                    return;
                }

                if (!sender.hasPermission("vulture.alerts")) {
                    MessageUtil.messagePlayer(sender, "&cNo permission.");
                    return;
                }

                if (args.length() != 1) {
                    MessageUtil.messagePlayer(sender, "&cUsage: /vulture alerts");
                    return;
                }

                Player player = args.getPlayer();
                PlayerData playerData = PlayerDataManager.getPlayerData(player);

                playerData.setAlertsEnabled(!playerData.isAlertsEnabled());
                MessageUtil.messagePlayer(player, Settings.prefix + " &7Alerts have been &f"
                        + (playerData.isAlertsEnabled() ? "enabled" : "disabled") + "&7.");
                break;

            case "debug":
                if (!sender.hasPermission("vulture.debug")) {
                    MessageUtil.messagePlayer(sender, "&cNo permission.");
                    return;
                }

                if (args.length() != 1) {
                    MessageUtil.messagePlayer(sender, "&cUsage: /vulture debug");
                    return;
                }

                Vulture.instance.debug = !Vulture.instance.debug;
                MessageUtil.messagePlayer(sender, Settings.prefix + " &7Debug mode has been &f"
                        + (Vulture.instance.debug ? "enabled" : "disabled") + "&7.");
                break;

            case "info":
                if (!sender.hasPermission("vulture.info")) {
                    MessageUtil.messagePlayer(sender, "&cNo permission.");
                    return;
                }

                if (args.length() != 2) {
                    MessageUtil.messagePlayer(sender, "&cUsage: /vulture info <player>");
                    return;
                }

                Player infoTarget = Bukkit.getPlayer(args.getArgs(1));

                if (infoTarget == null || !infoTarget.isOnline()) {
                    MessageUtil.messagePlayer(sender, "&cPlayer not found.");
                    return;
                }

                PlayerData targetData = PlayerDataManager.getPlayerData(infoTarget);

                MessageUtil.messagePlayer(sender, "");
                MessageUtil.messagePlayer(sender, "&e" + infoTarget.getName() + "'s Info");
                MessageUtil.messagePlayer(sender, "&7* &fVersion: &e" + targetData.getVersion().name());
                MessageUtil.messagePlayer(sender, "&7* &fPing: &e" + Vulture.instance.getPacketEvents().getPlayerUtils().getPing(infoTarget) + "ms");
                MessageUtil.messagePlayer(sender, "&7* &fViolations: &e" + targetData.getViolations().size());

                if (!targetData.getPayloads().isEmpty()) {
                    MessageUtil.messagePlayer(sender, "");
                    MessageUtil.messagePlayer(sender, "&fPayloads:");

                    for (PayloadType payloadType : targetData.getPayloads()) {
                        MessageUtil.messagePlayer(sender, "&7* &e" + payloadType.getName()
                                + " &7(" + payloadType.getDataType().getName() + ")");
                    }
                }

                MessageUtil.messagePlayer(sender, "");
                break;

            case "kick":
                if (!sender.hasPermission("vulture.kick")) {
                    MessageUtil.messagePlayer(sender, "&cNo permission.");
                    return;
                }

                if (args.length() < 3) {
                    MessageUtil.messagePlayer(sender, "&cUsage: /vulture kick <player> <reason>");
                    return;
                }

                Player kickTarget = Bukkit.getPlayer(args.getArgs(1));

                if (kickTarget == null || !kickTarget.isOnline()) {
                    MessageUtil.messagePlayer(sender, "&cPlayer not found.");
                    return;
                }

                StringBuilder reasonBuilder = new StringBuilder();

                for (int i = 2; i < args.length(); i++) {
                    reasonBuilder.append(args.getArgs(i)).append(" ");
                }

                String reason = reasonBuilder.toString().trim();

                KickUtil.kickPlayer(kickTarget, reason);
                break;

            case "reload":
                if (!sender.hasPermission("vulture.reload")) {
                    MessageUtil.messagePlayer(sender, "&cNo permission.");
                    return;
                }

                if (args.length() != 1) {
                    MessageUtil.messagePlayer(sender, "&cUsage: /vulture reload");
                    return;
                }

                Settings.loadSettings();
                MessageUtil.messagePlayer(sender, "&aReloaded the config files successfully.");
                break;

            default:
                handleHelp(sender, args);
                break;
        }
    }

    /**
     * Handles the help command.
     *
     * @param sender The command sender
     * @param args   The command arguments
     */
    private void handleHelp(@NotNull CommandSender sender, CommandArgs args) {
        if (!sender.hasPermission("vulture.main")) {
            MessageUtil.messagePlayer(sender, "&cNo permission.");
            return;
        }

        // A list of available commands with their usages.
        List<String> commands = Arrays.asList(
                "&f/vulture alerts &7- Toggles alerts.",
                "&f/vulture reload &7- Reloads the config.",
                "&f/vulture info <player> &7- View player info.",
                "&f/vulture kick <player> <reason> &7- Kicks a player.",
                "&f/vulture debug &7- Toggles debug mode."
        );

        int itemsPerPage = 4;
        int maxPages = (int) Math.ceil((double) commands.size() / itemsPerPage);
        int page = 1;

        if (args.length() > 1) {
            try {
                page = Integer.parseInt(args.getArgs(1));
            } catch (NumberFormatException ex) {
                MessageUtil.messagePlayer(sender, "&cInvalid page number. Choose between 1 and " + maxPages + ".");
                return;
            }
        }

        if (page > maxPages || page < 1) {
            MessageUtil.messagePlayer(sender, "&cInvalid page number. Choose between 1 and " + maxPages + ".");
            return;
        }

        int startIndex = (page - 1) * itemsPerPage;
        int endIndex = Math.min(commands.size(), startIndex + itemsPerPage);

        MessageUtil.messagePlayer(sender, "");
        MessageUtil.messagePlayer(sender, "&eVulture Help &7(Page " + page + "/" + maxPages + ")");

        for (int i = startIndex; i < endIndex; i++) {
            MessageUtil.messagePlayer(sender, commands.get(i));
        }

        MessageUtil.messagePlayer(sender, "");
        MessageUtil.messagePlayer(sender, "&7Type &f/vulture help <page> &7for more commands.");
        MessageUtil.messagePlayer(sender, "");
    }
}
