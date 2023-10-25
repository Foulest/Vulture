package net.foulest.vulture.listeners;

import io.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.utils.player.ClientVersion;
import lombok.AllArgsConstructor;
import net.foulest.vulture.Vulture;
import net.foulest.vulture.data.DataManager;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.util.KickUtil;
import net.foulest.vulture.util.MessageUtil;
import net.foulest.vulture.util.Settings;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

@AllArgsConstructor
public class PlayerDataListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = DataManager.getPlayerData(player);

        if (PacketEvents.get().getPlayerUtils().getClientVersion(player) == ClientVersion.UNRESOLVED
                || PacketEvents.get().getPlayerUtils().getClientVersion(player) == ClientVersion.UNKNOWN) {
            KickUtil.kickPlayer(player, "Failed to resolve client version", false);
        }

        Bukkit.getScheduler().runTaskLater(Vulture.instance, () -> {
            if (player.isOnline()) {
                // Enables alerts for players with the alerts permission.
                if (player.hasPermission("vulture.alerts")) {
                    playerData.setAlertsEnabled(true);
                    MessageUtil.messagePlayer(player, Settings.prefix + " &7Alerts have been &fenabled&7.");
                }
            }
        }, 20L);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        DataManager.removePlayerData(player);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = DataManager.getPlayerData(player);

        playerData.setSprinting(false);
        playerData.setSneaking(false);
    }
}
