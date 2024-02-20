package net.foulest.vulture.listeners;

import dev._2lstudios.hamsterapi.HamsterAPI;
import io.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.utils.player.ClientVersion;
import lombok.AllArgsConstructor;
import net.foulest.vulture.action.ActionType;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.data.PlayerDataManager;
import net.foulest.vulture.util.KickUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.*;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
public class PlayerDataListener implements Listener {

    /**
     * Handles player join events.
     *
     * @param event PlayerJoinEvent
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(@NotNull PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerDataManager.getPlayerData(player);
        ClientVersion clientVersion = PacketEvents.get().getPlayerUtils().getClientVersion(player);

        // Returns if the player's data is null.
        if (playerData == null) {
            KickUtil.kickPlayer(player, "Failed to load player data", true);
            return;
        }

        // Resolves the player's client version.
        if (!clientVersion.isResolved() || clientVersion == ClientVersion.UNKNOWN) {
            KickUtil.kickPlayer(player, "Failed to resolve client version", true);
        } else {
            playerData.setVersion(clientVersion);
        }

        // Injects the player into HamsterAPI.
        if (!HamsterAPI.tryInject(playerData)) {
            KickUtil.kickPlayer(player, "Failed to inject player", true);
        }
    }

    /**
     * Handles player quit events.
     *
     * @param event PlayerQuitEvent
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onQuit(@NotNull PlayerQuitEvent event) {
        Player player = event.getPlayer();

        // Removes the player's data from the map.
        PlayerDataManager.removePlayerData(player);
    }

    /**
     * Handles block break events.
     *
     * @param event BlockBreakEvent
     */
    @EventHandler
    public void onBlockBreak(@NotNull BlockBreakEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerDataManager.getPlayerData(player);

        // Sets digging to false.
        playerData.setDigging(false);
    }

    /**
     * Handles player item consume events.
     *
     * @param event PlayerItemConsumeEvent
     */
    @EventHandler
    public void onPlayerItemConsume(@NotNull PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerDataManager.getPlayerData(player);

        // Sets eating and drinking to false.
        playerData.setEating(false);
        playerData.setDrinking(false);
    }

    /**
     * Handles player bed enter events.
     *
     * @param event PlayerBedEnterEvent
     */
    @EventHandler
    public void onBedEnterEvent(@NotNull PlayerBedEnterEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerDataManager.getPlayerData(player);

        // Sets inBed to true.
        playerData.setInBed(true);
    }

    /**
     * Handles player bed leave events.
     *
     * @param event PlayerBedLeaveEvent
     */
    @EventHandler
    public void onBedLeaveEvent(@NotNull PlayerBedLeaveEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerDataManager.getPlayerData(player);

        // Sets inBed to false.
        playerData.setInBed(false);
    }

    /**
     * Handles player respawn events.
     *
     * @param event PlayerRespawnEvent
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(@NotNull PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerDataManager.getPlayerData(player);

        // Sets sprinting and sneaking to false.
        playerData.setSprinting(false);
        playerData.setSneaking(false);
    }

    /**
     * Handles player damage events.
     *
     * @param event EntityDamageEvent
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDamage(@NotNull EntityDamageEvent event) {
        // Returns if the entity is not a player.
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();
        PlayerData playerData = PlayerDataManager.getPlayerData(player);

        // Sets the player's last damage timestamp.
        playerData.setTimestamp(ActionType.DAMAGE);
    }
}
