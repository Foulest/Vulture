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
package net.foulest.vulture.cmds;

import lombok.Data;
import net.foulest.packetevents.PacketEvents;
import net.foulest.packetevents.utils.player.ClientVersion;
import net.foulest.packetevents.utils.player.PlayerUtils;
import net.foulest.vulture.Vulture;
import net.foulest.vulture.check.type.clientbrand.type.DataType;
import net.foulest.vulture.check.type.clientbrand.type.ModType;
import net.foulest.vulture.check.type.clientbrand.type.PayloadType;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.data.PlayerDataManager;
import net.foulest.vulture.util.ConstantUtil;
import net.foulest.vulture.util.KickUtil;
import net.foulest.vulture.util.MessageUtil;
import net.foulest.vulture.util.Settings;
import net.foulest.vulture.util.command.Command;
import net.foulest.vulture.util.command.CommandArgs;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Main command for Vulture.
 *
 * @author Foulest
 */
@Data
public class VultureCmd {

    @SuppressWarnings("NestedMethodCall")
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
        switch (subCommand.toLowerCase(Locale.ROOT)) {
            case "alerts": {
                if (!(sender instanceof Player)) {
                    MessageUtil.messagePlayer(sender, "&cOnly players can use this command!");
                    return;
                }

                if (!sender.hasPermission("vulture.alerts")) {
                    MessageUtil.messagePlayer(sender, ConstantUtil.NO_PERMISSION);
                    return;
                }

                if (args.length() != 1) {
                    MessageUtil.messagePlayer(sender, "&cUsage: /vulture alerts");
                    return;
                }

                Player player = args.getPlayer();

                if (player == null) {
                    MessageUtil.messagePlayer(sender, "&cOnly players can use this command.");
                    return;
                }

                PlayerData playerData = PlayerDataManager.getPlayerData(player);
                boolean alertsEnabled = playerData.isAlertsEnabled();

                playerData.setAlertsEnabled(!alertsEnabled);

                MessageUtil.messagePlayer(player, Settings.prefix + " &7Alerts have been &f"
                        + (alertsEnabled ? "enabled" : "disabled") + "&7.");
                break;
            }

            case "debug": {
                if (!sender.hasPermission("vulture.debug")) {
                    MessageUtil.messagePlayer(sender, ConstantUtil.NO_PERMISSION);
                    return;
                }

                if (args.length() != 1) {
                    MessageUtil.messagePlayer(sender, "&cUsage: /vulture debug");
                    return;
                }

                Vulture.instance.debugMode = !Vulture.instance.debugMode;

                MessageUtil.messagePlayer(sender, Settings.prefix + " &7Debug mode has been &f"
                        + (Vulture.instance.debugMode ? "enabled" : "disabled") + "&7.");
                break;
            }

            case "verbose": {
                if (!(sender instanceof Player)) {
                    MessageUtil.messagePlayer(sender, "&cOnly players can use this command!");
                    return;
                }

                if (!sender.hasPermission("vulture.verbose")) {
                    MessageUtil.messagePlayer(sender, ConstantUtil.NO_PERMISSION);
                    return;
                }

                if (args.length() != 1) {
                    MessageUtil.messagePlayer(sender, "&cUsage: /vulture verbose");
                    return;
                }

                Player player = args.getPlayer();

                if (player == null) {
                    MessageUtil.messagePlayer(sender, "&cOnly players can use this command.");
                    return;
                }

                PlayerData playerData = PlayerDataManager.getPlayerData(player);
                boolean verboseEnabled = playerData.isVerboseEnabled();

                playerData.setVerboseEnabled(!verboseEnabled);

                MessageUtil.messagePlayer(sender, Settings.prefix + " &7Verbose mode has been &f"
                        + (verboseEnabled ? "enabled" : "disabled") + "&7.");
                break;
            }

            case "info": {
                if (!sender.hasPermission("vulture.info")) {
                    MessageUtil.messagePlayer(sender, ConstantUtil.NO_PERMISSION);
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

                PacketEvents packetEvents = Vulture.instance.getPacketEvents();
                PlayerUtils playerUtils = packetEvents.getPlayerUtils();

                PlayerData targetData = PlayerDataManager.getPlayerData(target);
                String targetName = target.getName();
                ClientVersion targetVersion = targetData.getVersion();
                String versionName = targetVersion.getDisplayName();
                int targetPing = playerUtils.getPing(target);

                MessageUtil.messagePlayer(sender, "");
                MessageUtil.messagePlayer(sender, "&e" + targetName + "'s Info");
                MessageUtil.messagePlayer(sender, "&7* &fVersion: &e" + versionName);
                MessageUtil.messagePlayer(sender, "&7* &fPing: &e" + targetPing + "ms");

                if (!targetData.getPayloads().isEmpty()) {
                    MessageUtil.messagePlayer(sender, "");
                    MessageUtil.messagePlayer(sender, "&fPayloads (" + targetData.getPayloads().size() + "):");

                    StringBuilder payloadBuilder = new StringBuilder();
                    payloadBuilder.append("&7* ");

                    for (PayloadType payloadType : targetData.getPayloads()) {
                        String payloadName = payloadType.getName();
                        DataType dataType = payloadType.getDataType();
                        String dataTypeName = dataType.getName();

                        payloadBuilder.append("&e").append(payloadName)
                                .append(" &7(").append(dataTypeName).append(")&f, ");
                    }

                    // Trim the last comma and space.
                    String payloads = payloadBuilder.toString().trim();
                    payloads = payloads.substring(0, payloads.length() - 1);

                    MessageUtil.messagePlayer(sender, payloads);
                }

                if (!targetData.getMods().isEmpty()) {
                    MessageUtil.messagePlayer(sender, "");
                    MessageUtil.messagePlayer(sender, "&fMods (" + targetData.getMods().size() + "):");

                    StringBuilder modBuilder = new StringBuilder();
                    modBuilder.append("&7* ");

                    for (ModType modType : targetData.getMods()) {
                        modBuilder.append("&e").append(modType.getName())
                                .append(" &7(").append(modType.getVersion()).append(")&f, ");
                    }

                    // Trim the last comma and space.
                    String mods = modBuilder.toString().trim();
                    mods = mods.substring(0, mods.length() - 1);

                    MessageUtil.messagePlayer(sender, mods);
                }

                MessageUtil.messagePlayer(sender, "");
                break;
            }

            case "kick": {
                if (!sender.hasPermission("vulture.kick")) {
                    MessageUtil.messagePlayer(sender, ConstantUtil.NO_PERMISSION);
                    return;
                }

                if (args.length() < 3) {
                    MessageUtil.messagePlayer(sender, "&cUsage: /vulture kick <player> <reason>");
                    return;
                }

                Player target = Bukkit.getPlayer(args.getArgs(1));

                if (target == null || !target.isOnline()) {
                    MessageUtil.messagePlayer(sender, "&cPlayer not found.");
                    return;
                }

                StringBuilder reasonBuilder = new StringBuilder();

                for (int i = 2; i < args.length(); i++) {
                    String s = args.getArgs(i);
                    reasonBuilder.append(s).append(" ");
                }

                String reason = reasonBuilder.toString().trim();
                KickUtil.kickPlayer(target, reason);
                break;
            }

            case "whitelist": {
                if (!sender.hasPermission("vulture.whitelist")) {
                    MessageUtil.messagePlayer(sender, ConstantUtil.NO_PERMISSION);
                    return;
                }

                if (!Settings.ipWhitelistEnabled) {
                    MessageUtil.messagePlayer(sender, "&cIP whitelist is disabled in the config.");
                    return;
                }

                if (args.length() != 3 && args.length() != 4) {
                    MessageUtil.messagePlayer(sender, "&cUsage: /vulture whitelist add/remove <player> [IP]");
                    return;
                }

                String playerName = args.getArgs(2);
                Player target = Bukkit.getPlayer(playerName);
                UUID targetUUID = null;

                if (target != null && target.isOnline()) {
                    targetUUID = target.getUniqueId();
                } else {
                    // Try to get the player's UUID from the saved whitelist
                    for (UUID uuid : Settings.ipWhitelist.keySet()) {
                        if (Bukkit.getOfflinePlayer(uuid).getName().equalsIgnoreCase(playerName)) {
                            targetUUID = uuid;
                            break;
                        }
                    }
                }

                if (targetUUID == null) {
                    MessageUtil.messagePlayer(sender, "&cPlayer not found.");
                    break;
                }

                String ipAddress = null;

                if (args.length() == 4) {
                    // Validate the IP address.
                    if (!args.getArgs(3).matches("^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}$")) {
                        MessageUtil.messagePlayer(sender, "&cInvalid IP address.");
                        return;
                    }

                    ipAddress = args.getArgs(3);
                } else if (target != null && target.isOnline()) {
                    ipAddress = target.getAddress().getAddress().getHostAddress();
                }

                switch (args.getArgs(1)) {
                    case "add":
                        if (ipAddress == null) {
                            MessageUtil.messagePlayer(sender, "&cIP address is required when adding a whitelist entry.");
                            return;
                        }

                        // Check if the player's IP is already whitelisted.
                        List<String> playerIpsAdd = Settings.ipWhitelist.getOrDefault(targetUUID, new ArrayList<>());
                        if (playerIpsAdd.contains(ipAddress)) {
                            MessageUtil.messagePlayer(sender, "&cThe player's IP is already whitelisted.");
                            return;
                        }

                        // Adds the IP address to the whitelist.
                        playerIpsAdd.add(ipAddress);
                        Settings.ipWhitelist.put(targetUUID, playerIpsAdd);

                        // Saves the new whitelist.
                        ConfigurationSection whitelistSectionAdd = Settings.config.createSection("vulture.ip-whitelist.whitelist");
                        for (Map.Entry<UUID, List<String>> entry : Settings.ipWhitelist.entrySet()) {
                            String uuidString = entry.getKey().toString();
                            List<String> ipString = entry.getValue();
                            whitelistSectionAdd.set(uuidString + ".ips", ipString);
                        }
                        Settings.saveConfig();

                        // Reloads the settings file.
                        Settings.loadSettings();

                        MessageUtil.messagePlayer(sender, "&aAdded " + playerName + "'s IP address to the whitelist.");
                        break;

                    case "remove":
                        // Check if the player has any whitelisted IPs.
                        List<String> playerIpsRemove = Settings.ipWhitelist.getOrDefault(targetUUID, new ArrayList<>());
                        if (playerIpsRemove.isEmpty()) {
                            MessageUtil.messagePlayer(sender, "&cThe player has no whitelisted IP addresses.");
                            return;
                        }

                        if (ipAddress != null) {
                            // Remove the specific IP address from the whitelist.
                            if (!playerIpsRemove.contains(ipAddress)) {
                                MessageUtil.messagePlayer(sender, "&cThe specified IP address is not whitelisted for the player.");
                                return;
                            }

                            playerIpsRemove.remove(ipAddress);
                            if (playerIpsRemove.isEmpty()) {
                                Settings.ipWhitelist.remove(targetUUID);
                            } else {
                                Settings.ipWhitelist.put(targetUUID, playerIpsRemove);
                            }

                            MessageUtil.messagePlayer(sender, "&aRemoved " + playerName + "'s specific IP address from the whitelist.");
                        } else {
                            // Remove all whitelisted IP addresses for the player.
                            Settings.ipWhitelist.remove(targetUUID);

                            MessageUtil.messagePlayer(sender, "&aRemoved all whitelisted IP addresses for " + playerName + ".");
                        }

                        // Saves the new whitelist.
                        ConfigurationSection whitelistSectionRemove = Settings.config.createSection("vulture.ip-whitelist.whitelist");
                        for (Map.Entry<UUID, List<String>> entry : Settings.ipWhitelist.entrySet()) {
                            String uuidString = entry.getKey().toString();
                            List<String> ipString = entry.getValue();
                            whitelistSectionRemove.set(uuidString + ".ips", ipString);
                        }
                        Settings.saveConfig();

                        // Reloads the settings file.
                        Settings.loadSettings();

                        break;

                    default:
                        MessageUtil.messagePlayer(sender, "&cUsage: /vulture whitelist add/remove <player> [IP]");
                        break;
                }
                break;
            }

            case "reload": {
                if (!sender.hasPermission("vulture.reload")) {
                    MessageUtil.messagePlayer(sender, ConstantUtil.NO_PERMISSION);
                    return;
                }

                if (args.length() != 1) {
                    MessageUtil.messagePlayer(sender, "&cUsage: /vulture reload");
                    return;
                }

                Settings.loadSettings();
                MessageUtil.messagePlayer(sender, "&aReloaded the config files successfully.");
                break;
            }

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
    @SuppressWarnings("MethodMayBeStatic")
    private void handleHelp(@NotNull CommandSender sender, CommandArgs args) {
        if (!sender.hasPermission("vulture.main")) {
            MessageUtil.messagePlayer(sender, ConstantUtil.NO_PERMISSION);
            return;
        }

        // A list of available commands with their usages.
        List<String> commands = Arrays.asList(
                "&f/vulture alerts &7- Toggles alerts.",
                "&f/vulture reload &7- Reloads the config.",
                "&f/vulture info <player> &7- View player info.",
                "&f/vulture kick <player> <reason> &7- Kicks a player.",
                "&f/vulture whitelist add/remove <player> [IP] &7- Manages the IP whitelist.",
                "&f/vulture verbose &7- Toggles verbose mode.",
                "&f/vulture debug &7- Toggles debug mode."
        );

        int itemsPerPage = 4;
        int sizeOfAll = commands.size();
        int maxPages = (int) Math.ceil((double) sizeOfAll / itemsPerPage);
        int page = 1;

        if (args.length() > 1) {
            try {
                String pageNumber = args.getArgs(1);
                page = Integer.parseInt(pageNumber);
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
        int endIndex = Math.min(sizeOfAll, startIndex + itemsPerPage);

        MessageUtil.messagePlayer(sender, "");
        MessageUtil.messagePlayer(sender, "&eVulture Help &7(Page " + page + "/" + maxPages + ")");

        for (int i = startIndex; i < endIndex; i++) {
            String line = commands.get(i);
            MessageUtil.messagePlayer(sender, line);
        }

        MessageUtil.messagePlayer(sender, "");
        MessageUtil.messagePlayer(sender, "&7Type &f/vulture help <page> &7for more commands.");
        MessageUtil.messagePlayer(sender, "");
    }
}
