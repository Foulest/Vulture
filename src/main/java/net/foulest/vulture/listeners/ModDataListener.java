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
package net.foulest.vulture.listeners;

import net.foulest.vulture.Vulture;
import net.foulest.vulture.check.type.clientbrand.type.ModType;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.data.PlayerDataManager;
import net.foulest.vulture.util.KickUtil;
import net.foulest.vulture.util.MessageUtil;
import net.foulest.vulture.util.Settings;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRegisterChannelEvent;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class ModDataListener implements Listener, PluginMessageListener {

    private static final byte[] HANDSHAKE_PART_1 = {-2, 0};
    private static final byte[] HANDSHAKE_PART_2 = {0, 2, 0, 0, 0, 0};
    private static final byte[] HANDSHAKE_PART_3 = {2, 0, 0, 0, 0};

    public ModDataListener() {
        // Registers the plugin's Forge plugin channels.
        MessageUtil.log(Level.INFO, "Registering Plugin Channels...");
        Vulture.getInstance().getServer().getMessenger().registerIncomingPluginChannel(Vulture.getInstance(), "FML|HS", this);
        Vulture.getInstance().getServer().getMessenger().registerOutgoingPluginChannel(Vulture.getInstance(), "FML|HS");
    }

    /**
     * Listens for the player registering a channel.
     *
     * @param event The event
     */
    @EventHandler
    public static void onChannel(@NotNull PlayerRegisterChannelEvent event) {
        String channel = event.getChannel();

        // Sends the handshake to the player.
        if (channel.equals("FORGE")) {
            event.getPlayer().sendPluginMessage(Vulture.getInstance(), "FML|HS", HANDSHAKE_PART_1);
            event.getPlayer().sendPluginMessage(Vulture.getInstance(), "FML|HS", HANDSHAKE_PART_2);
            event.getPlayer().sendPluginMessage(Vulture.getInstance(), "FML|HS", HANDSHAKE_PART_3);
        }
    }

    /**
     * Listens for the player sending their mod list.
     *
     * @param channel The channel the message was sent on (FML|HS)
     * @param player The player who sent the message
     * @param data The message data
     */
    @Override
    public void onPluginMessageReceived(String channel, Player player, byte @NotNull [] data) {
        if (data[0] == 2) {
            PlayerData playerData = PlayerDataManager.getPlayerData(player);
            Map<String, String> modData = getModData(data);

            for (Map.Entry<String, String> entry : modData.entrySet()) {
                String name = entry.getKey();
                String version = entry.getValue();

                // Adds the mod to the player's data.
                ModType modType = new ModType(name, version);
                playerData.getMods().add(modType);
            }

            // Check if the player has any blacklisted mods.
            playerData.getMods().forEach(modType -> {
                String modName = modType.getName();

                if (Settings.blockedMods.contains(modName)) {
                    KickUtil.kickPlayer(player, "Blocked Mod: " + modName,
                            "&c" + modName + " is not allowed on this server.");
                }
            });
        }
    }

    /**
     * Gets the mod data from the byte array.
     *
     * @param data Byte array
     * @return Mod data
     */
    private static @NotNull Map<String, String> getModData(byte @NotNull [] data) {
        Map<String, String> mods = new HashMap<>();
        boolean store = false;
        String tempName = null;

        for (int i = 2; i < data.length; store = !store) {
            int end = i + data[i] + 1;

            if (end > data.length) {
                throw new IllegalArgumentException("Invalid data structure");
            }

            byte[] range = Arrays.copyOfRange(data, i + 1, end);
            String parsedName = new String(range, StandardCharsets.UTF_8);

            if (store) {
                mods.put(tempName, parsedName);
            } else {
                tempName = parsedName;
            }

            i = end;
        }
        return mods;
    }
}
