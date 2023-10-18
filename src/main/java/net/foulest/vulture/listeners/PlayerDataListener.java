package net.foulest.vulture.listeners;

import lombok.AllArgsConstructor;
import net.foulest.vulture.data.DataManager;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.util.MessageUtil;
import net.foulest.vulture.util.Settings;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.util.Vector;

@AllArgsConstructor
public class PlayerDataListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = DataManager.getPlayerData(player);

        // Reset the player's velocity to fix a bug with the Flight check.
        player.setVelocity(new Vector(0, 0, 0));

        // Enables alerts for players with the alerts permission.
        if (player.hasPermission("vulture.alerts")) {
            playerData.setAlertsEnabled(true);
            MessageUtil.messagePlayer(player, Settings.prefix + " &7Alerts have been &fenabled&7.");
        }

        // Reset the player's after kick data.
        playerData.setKicking(false);
        playerData.setNewViolationsPaused(false);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onQuit(PlayerQuitEvent event) {
        DataManager.removePlayerData(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        PlayerData playerData = DataManager.getPlayerData(event.getPlayer());

        playerData.setSprinting(false);
        playerData.setSneaking(false);
    }
}
