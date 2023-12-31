package net.foulest.vulture.listeners;

import dev._2lstudios.hamsterapi.HamsterAPI;
import io.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.utils.player.ClientVersion;
import lombok.AllArgsConstructor;
import net.foulest.vulture.action.ActionType;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.data.PlayerDataManager;
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
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.*;

@AllArgsConstructor
public class PlayerDataListener implements Listener {

    /**
     * Handles player join events.
     *
     * @param event PlayerJoinEvent
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerDataManager.getPlayerData(player);
        ClientVersion clientVersion = PacketEvents.get().getPlayerUtils().getClientVersion(player);

        // Resolves the player's client version.
        if (!clientVersion.isResolved() || clientVersion == ClientVersion.UNKNOWN) {
            MessageUtil.debug(player.getName() + " failed to resolve client version");
            KickUtil.kickPlayer(player, "Failed to resolve client version", true);
        } else {
            MessageUtil.debug(player.getName() + " resolved client version: " + clientVersion);
            playerData.setVersion(clientVersion);
        }

        // Injects the player into HamsterAPI.
        if (!HamsterAPI.tryInject(playerData)) {
            MessageUtil.debug(player.getName() + " failed to inject player");
            KickUtil.kickPlayer(player, "Failed to inject player", true);
        }
    }

    /**
     * Handles player quit events.
     *
     * @param event PlayerQuitEvent
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onQuit(PlayerQuitEvent event) {
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
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerDataManager.getPlayerData(player);
        Block targetBlock = event.getBlock();

        // Returns if the block is null.
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

        // Sets digging to false.
        playerData.setDigging(false);
    }

    /**
     * Handles block place events.
     *
     * @param event BlockPlaceEvent
     */
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block targetBlock = event.getBlock();

        // Returns if the block is null.
        if (targetBlock == null) {
            return;
        }

        // Prevents players from placing blocks on liquids & air.
        if ((event.getBlockAgainst().getType() == Material.AIR
                || event.getBlockAgainst().getType() == Material.WATER
                || event.getBlockAgainst().getType() == Material.STATIONARY_WATER
                || event.getBlockAgainst().getType() == Material.LAVA
                || event.getBlockAgainst().getType() == Material.STATIONARY_LAVA)
                && event.getBlockPlaced().getType() != Material.WATER_LILY) {
            event.setCancelled(true);
            KickUtil.kickPlayer(player, "Tried to place block against invalid block");
        }
    }

    /**
     * Handles block damage events.
     *
     * @param event BlockDamageEvent
     */
    @EventHandler
    public void onBlockDamage(BlockDamageEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerDataManager.getPlayerData(player);
        Block targetBlock = event.getBlock();

        // Returns if the block is null.
        if (targetBlock == null) {
            return;
        }

        // Sets digging to true if the block is not insta-break.
        // This is used to reliably set digging to true 100% of the time.
        if (!event.getInstaBreak()) {
            playerData.setDigging(true);
            playerData.setTimestamp(ActionType.DIGGING);
        }
    }

    /**
     * Handles player item consume events.
     *
     * @param event PlayerItemConsumeEvent
     */
    @EventHandler
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
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
    public void onBedEnterEvent(PlayerBedEnterEvent event) {
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
    public void onBedLeaveEvent(PlayerBedLeaveEvent event) {
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
    public void onPlayerRespawn(PlayerRespawnEvent event) {
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
    public void onDamage(EntityDamageEvent event) {
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
