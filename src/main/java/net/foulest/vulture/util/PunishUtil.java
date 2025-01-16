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
package net.foulest.vulture.util;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.ProtocolPacketEvent;
import io.github.retrooper.packetevents.util.PlayerPingAccessorModern;
import io.github.retrooper.packetevents.util.SpigotReflectionUtil;
import lombok.Data;
import net.foulest.vulture.check.CheckInfoData;
import net.foulest.vulture.check.Violation;
import net.foulest.vulture.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Data
public class PunishUtil {

    /**
     * This method is used to flag the player and cancel an event.
     *
     * @param event   the event to cancel
     * @param verbose the optional data to include in the flag
     */
    public static void flag(@NotNull PlayerData playerData,
                            @NotNull CheckInfoData checkInfo,
                            @NotNull ProtocolPacketEvent event,
                            String... verbose) {
        event.setCancelled(true);
        flag(playerData, checkInfo, verbose);
    }

    /**
     * This method is used to flag the player with the given data.
     * <p>
     * When a player is flagged, all online staff members are alerted with the check they flagged and the data
     *
     * @param verbose the optional data to include in the flag
     */
    public static void flag(@NotNull PlayerData playerData,
                            @NotNull CheckInfoData checkInfo,
                            String @NotNull ... verbose) {
        Player player = playerData.getPlayer();

        // Checks the player for exemptions.
        if (playerData.isNewViolationsPaused()
                || KickUtil.isPlayerBeingKicked(player)
                || SpigotReflectionUtil.getTPS() < 18
                || !checkInfo.isEnabled()
                || !checkInfo.isPunishable()) {
            return;
        }

        @NotNull String verboseString = verbose.length == 0 ? "" : "&7[" + String.join(", ", verbose) + "]";

        // Handles adding a violation.
        handleNewViolation(playerData, checkInfo, verbose);

        // Handles sending the alert message.
        handleAlert(playerData, checkInfo, verboseString);

        // Handles the punishment to execute.
        handlePunishment(playerData, checkInfo, verboseString);
    }

    /**
     * Handles adding a new violation to the player's data.
     *
     * @param verbose The verbose to add to the violation.
     */
    public static void handleNewViolation(@NotNull PlayerData playerData,
                                          @NotNull CheckInfoData checkInfo,
                                          String @NotNull ... verbose) {
        Player player = playerData.getPlayer();
        Location location = player.getLocation();

        // Removes older violations before adding new ones.
        playerData.getViolations().removeIf(violation -> {
            long timestamp = violation.getTimestamp();
            return System.currentTimeMillis() - timestamp > Settings.resetViolations * 1000L;
        });

        int violations = getViolationCount(playerData, checkInfo) + 1;
        int ping = PacketEvents.getAPI().getPlayerManager().getPing(player);

        // Creates a new violation.

        @NotNull Violation violation = new Violation(
                checkInfo,
                verbose,
                violations,
                location,
                ping,
                SpigotReflectionUtil.getTPS(),
                System.currentTimeMillis()
        );

        // Adds the violation to the player's violations.
        playerData.getViolations().add(violation);
    }

    /**
     * Handles sending the alert message.
     *
     * @param verbose The verbose to add to the alert message.
     */
    public static void handleAlert(@NotNull PlayerData playerData,
                                   @NotNull CheckInfoData checkInfo,
                                   @NotNull String verbose) {
        Player player = playerData.getPlayer();
        String playerName = player.getName();
        int violations = getViolationCount(playerData, checkInfo);
        String checkName = checkInfo.getName();

        MessageUtil.sendAlert("&f" + playerName + " &7failed &f"
                + checkName + " &c(x" + violations + ")", verbose);
    }

    /**
     * Handles the punishment to execute.
     *
     * @param verbose The verbose to add to the punishment.
     */
    @SuppressWarnings("NestedMethodCall")
    public static void handlePunishment(@NotNull PlayerData playerData,
                                        @NotNull CheckInfoData checkInfo,
                                        @NotNull String verbose) {
        Player player = playerData.getPlayer();
        String playerName = player.getName();
        String banCommand = checkInfo.getBanCommand();
        String checkName = checkInfo.getName();
        int violations = getViolationCount(playerData, checkInfo);

        if (violations >= checkInfo.getMaxViolations()
                && !checkInfo.isExperimental()
                && !banCommand.isEmpty()) {
            // Pauses any new violations from being added.
            playerData.setNewViolationsPaused(true);

            boolean kicking = (banCommand.startsWith("vulture kick")
                    || banCommand.startsWith("kick"));

            // Sends the private punishment message.
            MessageUtil.sendAlert("&f" + playerName + " &7has been " + (kicking ? "kicked" : "banned")
                    + " for failing &f" + checkName + " &c(x" + violations + ")", verbose);

            // Sends the public punishment message, if one is set.
            // Punishment messages are not sent if the player is being kicked.
            if (!Settings.banMessage.isEmpty() && !kicking) {
                @NotNull List<String> banMessageEdited = new ArrayList<>(Settings.banMessage);
                banMessageEdited.replaceAll(s -> s.replace("%player%", playerName));
                banMessageEdited.replaceAll(s -> s.replace("%check%", checkName));
                MessageUtil.broadcast(banMessageEdited);
            }

            // Executes the punishment command.
            TaskUtil.runTask(() -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), banCommand
                    .replace("%player%", playerName)
                    .replace("%check%", checkName)));
        }
    }

    private static int getViolationCount(@NotNull PlayerData playerData,
                                         CheckInfoData checkInfo) {
        int violations = 0;

        // Increments the violations.
        for (@NotNull Violation violation : playerData.getViolations()) {
            if (violation.getCheckInfo() == checkInfo) {
                violations++;
            }
        }
        return violations;
    }
}
