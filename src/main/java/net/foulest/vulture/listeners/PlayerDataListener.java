package net.foulest.vulture.listeners;

import io.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.utils.player.ClientVersion;
import lombok.AllArgsConstructor;
import net.foulest.vulture.action.ActionType;
import net.foulest.vulture.data.DataManager;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.util.KickUtil;
import net.foulest.vulture.util.MessageUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.*;

import java.util.logging.Level;

@AllArgsConstructor
public class PlayerDataListener implements Listener {

    /**
     * Handles player join events.
     *
     * @param event The event.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = DataManager.getPlayerData(player);
        ClientVersion clientVersion = PacketEvents.get().getPlayerUtils().getClientVersion(player);

        if (!clientVersion.isResolved() || clientVersion == ClientVersion.UNKNOWN) {
            MessageUtil.log(Level.INFO, player.getName() + " failed to resolve client version");
            KickUtil.kickPlayer(player, "Failed to resolve client version", false);
        } else {
            MessageUtil.log(Level.INFO, player.getName() + " resolved client version: " + clientVersion);
            playerData.setVersion(clientVersion);
        }
    }

    /**
     * Handles player quit events.
     *
     * @param event The event.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        DataManager.removePlayerData(player);
    }

    /**
     * Handles player respawn events.
     *
     * @param event The event.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = DataManager.getPlayerData(player);

        playerData.setSprinting(false);
        playerData.setSneaking(false);
    }

    /**
     * Handles block break events.
     *
     * @param event The event.
     */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = DataManager.getPlayerData(player);
        Block targetBlock = event.getBlock();

        if (targetBlock == null) {
            return;
        }

        // Prevents players from breaking liquids & air.
        if (targetBlock.getType() == Material.AIR
                || targetBlock.getType() == Material.WATER
                || targetBlock.getType() == Material.STATIONARY_WATER
                || targetBlock.getType() == Material.LAVA
                || targetBlock.getType() == Material.STATIONARY_LAVA) {
            event.setCancelled(true);
            KickUtil.kickPlayer(player, "Tried to break invalid block");
            return;
        }

        playerData.setDigging(false);
    }

    /**
     * Handles block place events.
     *
     * @param event The event.
     */
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block targetBlock = event.getBlock();

        if (targetBlock == null) {
            return;
        }

        // Prevents players from placing blocks on liquids & air.
        if (event.getBlockAgainst().getType() == Material.AIR
                || event.getBlockAgainst().getType() == Material.WATER
                || event.getBlockAgainst().getType() == Material.STATIONARY_WATER
                || event.getBlockAgainst().getType() == Material.LAVA
                || event.getBlockAgainst().getType() == Material.STATIONARY_LAVA) {
            if (event.getBlockPlaced().getType() != Material.WATER_LILY) {
                event.setCancelled(true);
                KickUtil.kickPlayer(player, "Tried to place block against invalid block");
            }
        }
    }

    /**
     * Handles block damage events.
     *
     * @param event The event.
     */
    @EventHandler
    public void onBlockDamage(BlockDamageEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = DataManager.getPlayerData(player);
        Block targetBlock = event.getBlock();

        if (targetBlock == null) {
            return;
        }

        if (!event.getInstaBreak()) {
            playerData.setDigging(true);
            playerData.setTimestamp(ActionType.DIGGING);
        }
    }

    /**
     * Handles player item consume events.
     *
     * @param event The event.
     */
    @EventHandler
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = DataManager.getPlayerData(player);

        playerData.setEating(false);
        playerData.setDrinking(false);
    }

    /**
     * Handles player bed enter events.
     *
     * @param event The event.
     */
    @EventHandler
    public void onBedEnterEvent(PlayerBedEnterEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = DataManager.getPlayerData(player);
        playerData.setInBed(true);
    }

    /**
     * Handles player bed leave events.
     *
     * @param event The event.
     */
    @EventHandler
    public void onBedLeaveEvent(PlayerBedLeaveEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = DataManager.getPlayerData(player);
        playerData.setInBed(false);
    }
}
