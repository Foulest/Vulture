package net.foulest.vulture.cmds;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.foulest.vulture.Vulture;
import net.foulest.vulture.check.type.clientbrand.type.PayloadType;
import net.foulest.vulture.data.DataManager;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.util.MessageUtil;
import net.foulest.vulture.util.Settings;
import net.foulest.vulture.util.command.Command;
import net.foulest.vulture.util.command.CommandArgs;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

/**
 * Command for reloading the plugin's config file.
 *
 * @author Foulest
 * @project Vulture
 */
@Getter
@Setter
public class VultureCmd {

    @Command(name = "vulture", description = "Reloads the plugin's config file.",
            permission = "vulture.main", usage = "/vulture")
    public void onCommand(@NonNull CommandArgs args) {
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
                PlayerData playerData = DataManager.getPlayerData(player);

                playerData.setAlertsEnabled(!playerData.isAlertsEnabled());
                MessageUtil.messagePlayer(player, Settings.prefix + " &7Alerts have been &f"
                        + (playerData.isAlertsEnabled() ? "enabled" : "disabled") + "&7.");
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

                Player target = Bukkit.getPlayer(args.getArgs(1));

                if (target == null || !target.isOnline()) {
                    MessageUtil.messagePlayer(sender, "&cPlayer not found.");
                    return;
                }

                PlayerData targetData = DataManager.getPlayerData(target);

                MessageUtil.messagePlayer(sender, "");
                MessageUtil.messagePlayer(sender, "&e" + target.getName() + "'s Info");
                MessageUtil.messagePlayer(sender, "");
                MessageUtil.messagePlayer(sender, "&7* &fVersion: &e" + targetData.getVersion().name());
                MessageUtil.messagePlayer(sender, "&7* &fPing: &e" + Vulture.instance.getPacketEvents().getPlayerUtils().getPing(target) + "ms");
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

    private void handleHelp(@NonNull CommandSender sender, @NonNull CommandArgs args) {
        if (!sender.hasPermission("vulture.help")) {
            MessageUtil.messagePlayer(sender, "&cNo permission.");
            return;
        }

        // A list of available commands with their usages.
        List<String> commands = Arrays.asList(
                "&7* &f/vulture alerts &7- Toggles alerts.",
                "&7* &f/vulture reload &7- Reloads Vulture.",
                "&7* &f/vulture info <player> &7- View player info."
        );

        int itemsPerPage = 5;
        int maxPages = (int) Math.ceil((double) commands.size() / itemsPerPage);
        int page = 1;

        if (args.length() > 1) {
            try {
                page = Integer.parseInt(args.getArgs(1));
            } catch (NumberFormatException ignored) {
            }
        }

        if (page > maxPages || page < 1) {
            MessageUtil.messagePlayer(sender, "&cInvalid page number. Choose between 1 and " + maxPages + ".");
            return;
        }

        int startIndex = (page - 1) * itemsPerPage;
        int endIndex = Math.min(commands.size(), startIndex + itemsPerPage);

        MessageUtil.messagePlayer(sender, "");
        MessageUtil.messagePlayer(sender, "&eVulture Help &7&o(Page " + page + "/" + maxPages + ")");

        for (int i = startIndex; i < endIndex; i++) {
            MessageUtil.messagePlayer(sender, commands.get(i));
        }

        MessageUtil.messagePlayer(sender, "");
        MessageUtil.messagePlayer(sender, "&7Type &f/vulture help <page> &7for more commands.");
        MessageUtil.messagePlayer(sender, "");
    }
}
