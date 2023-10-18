package net.foulest.vulture.listeners;

import lombok.Getter;
import lombok.Setter;
import net.foulest.vulture.action.ActionType;
import net.foulest.vulture.data.DataManager;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.util.KickUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * Listener for exploit related events.
 *
 * @author Foulest
 * @project Vulture
 */
@Getter
@Setter
public class BukkitListener implements Listener {

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

    @EventHandler
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = DataManager.getPlayerData(player);

        playerData.setEating(false);
        playerData.setDrinking(false);
    }

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

    @EventHandler(ignoreCancelled = true)
    public void onBedEnterEvent(PlayerBedEnterEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = DataManager.getPlayerData(player);
        playerData.setInBed(true);
    }

    @EventHandler
    public void onBedLeaveEvent(PlayerBedLeaveEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = DataManager.getPlayerData(player);
        playerData.setInBed(false);
    }

    @EventHandler
    public void onBucketEmptyEvent(PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();
        Block targetBlock = event.getBlockClicked();

        if (targetBlock == null) {
            return;
        }

        Block relative = event.getBlockClicked().getRelative(event.getBlockFace());
        String materialName = relative.getType().name();

        // Prevents players from emptying buckets into end portals and breaking them.
        if (materialName.contains("END_PORTAL")
                || materialName.contains("ENDER_PORTAL")
                || materialName.contains("END_GATEWAY")) {
            event.setCancelled(true);
            player.updateInventory();
        }
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        Entity[] entities = event.getChunk().getEntities();

        for (Entity entity : entities) {
            if (entity instanceof InventoryHolder) {
                InventoryHolder inventoryHolder = (InventoryHolder) entity;
                Inventory inventory = inventoryHolder.getInventory();

                if (inventory != null) {
                    List<HumanEntity> viewers = inventory.getViewers();

                    for (HumanEntity viewer : viewers) {
                        viewer.closeInventory();
                    }
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityTeleport(EntityTeleportEvent event) {
        Entity entity = event.getEntity();

        if (entity.getWorld().getEnvironment().equals(World.Environment.THE_END) && !entity.isEmpty()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Player player = event.getPlayer();
            PlayerInventory inventory = player.getInventory();
            ItemStack heldItem = inventory.getItem(inventory.getHeldItemSlot());

            if (heldItem != null) {
                String heldItemTypeName = heldItem.getType().name();

                if (heldItemTypeName.endsWith("_BED") || heldItemTypeName.equals("BED")) {
                    Block clickedBlock = event.getClickedBlock();

                    if (clickedBlock != null) {
                        Collection<Block> adjacentBlocks = new HashSet<>();
                        Location searchLocation = clickedBlock.getLocation().add(event.getClickedBlock().getLocation());

                        adjacentBlocks.add(searchLocation.add(0, -1, 0).getBlock()); // Down
                        adjacentBlocks.add(searchLocation.add(0, 2, 0).getBlock()); // Up
                        adjacentBlocks.add(searchLocation.add(-1, -1, 0).getBlock()); // Left
                        adjacentBlocks.add(searchLocation.add(2, 0, 0).getBlock()); // Right
                        adjacentBlocks.add(searchLocation.add(-1, 0, 1).getBlock()); // Forward
                        adjacentBlocks.add(searchLocation.add(0, 0, -2).getBlock()); // Downward

                        for (Block block : adjacentBlocks) {
                            String blockTypeName = block.getType().name();

                            if (blockTypeName.equals("WHEAT") || blockTypeName.equals("POTATOES")
                                    || blockTypeName.equals("POTATO") || blockTypeName.equals("CARROTS")
                                    || blockTypeName.equals("CARROT") || blockTypeName.equals("NETHER_WARTS")
                                    || blockTypeName.equals("BEETROOTS")) {
                                event.setCancelled(true);
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onPlayerShearEntity(PlayerShearEntityEvent event) {
        if (event.getEntity().isDead()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        Entity entity = event.getEntity();

        if (entity == null) {
            return;
        }

        if (entity.getVelocity().lengthSquared() > 15) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onStructureGrow(StructureGrowEvent event) {
        World world = event.getWorld();

        for (BlockState blockBefore : event.getBlocks()) {
            Block block = world.getBlockAt(blockBefore.getLocation());
            String type = block.getType().name();

            if (type.contains("ENDER_PORTAL") || type.contains("END_PORTAL") || type.contains("END_GATEWAY")) {
                event.setCancelled(true);
            }
        }
    }
}
