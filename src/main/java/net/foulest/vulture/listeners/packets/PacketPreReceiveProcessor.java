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
package net.foulest.vulture.listeners.packets;

import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.SimplePacketListenerAbstract;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.data.PlayerDataManager;
import net.foulest.vulture.util.KickUtil;
import net.foulest.vulture.util.Settings;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Handles all incoming packets before they are processed.
 *
 * @author Foulest
 */
public class PacketPreReceiveProcessor extends SimplePacketListenerAbstract {

    public PacketPreReceiveProcessor() {
        super(PacketListenerPriority.HIGHEST);
    }

    /**
     * Handles incoming packets.
     *
     * @param event PacketDecodeEvent
     */
    @Override
    public void onPacketPlayReceive(@NotNull PacketPlayReceiveEvent event) {
        PacketTypeCommon packetType = event.getPacketType();
        Player player = event.getPlayer();

        // Ignores incoming packets for invalid/offline players.
        if (player == null || !player.isOnline()) {
            return;
        }

        // Cancels incoming packets from players being kicked.
        if (KickUtil.isPlayerBeingKicked(player)) {
            event.setCancelled(true);
            return;
        }

        // Get player data and increment packets sent per tick.
        PlayerData playerData = PlayerDataManager.getPlayerData(player);
        int packetsSentPerTick = playerData.incrementPacketsSentPerTick();
        int packetsSentPerSecond = playerData.incrementPacketsSentPerSecond();
        int smoothedPacketsPerSecond = playerData.getSmoothedPacketsPerSecond();

        // If the player has sent too many packets in one tick, kick them.
        if (Settings.maxPacketsPerTick > 0 && packetsSentPerTick >= Settings.maxPacketsPerTick) {
            event.setCancelled(true);
            KickUtil.kickPlayer(player, "Sent too many packets in one tick (count="
                    + packetsSentPerTick + ")", true);
            return;
        }

        // If the player has sent too many packets in one second, kick them.
        if (Settings.maxPacketsPerSecond > 0 && packetsSentPerSecond >= Settings.maxPacketsPerSecond) {
            event.setCancelled(true);
            KickUtil.kickPlayer(player, "Sent too many packets in one second (count="
                    + packetsSentPerSecond + ")", true);
            return;
        }

        // If the player has sent too many packets per smoothed average, kick them.
        if (Settings.maxPacketsSmoothed > 0 && playerData.getSmoothedSentPerSecond().isFull()
                && smoothedPacketsPerSecond >= Settings.maxPacketsSmoothed) {
            event.setCancelled(true);
            KickUtil.kickPlayer(player, "Sent too many packets per smoothed average (count="
                    + smoothedPacketsPerSecond + ")", true);
        }
    }
}
