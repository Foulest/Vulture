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
package net.foulest.vulture.processor.type;

import net.foulest.packetevents.event.eventtypes.CancellableEvent;
import net.foulest.packetevents.event.eventtypes.CancellableNMSPacketEvent;
import net.foulest.packetevents.event.impl.PacketPlayReceiveEvent;
import net.foulest.packetevents.packettype.PacketType;
import net.foulest.packetevents.packetwrappers.NMSPacket;
import net.foulest.packetevents.packetwrappers.play.in.abilities.WrappedPacketInAbilities;
import net.foulest.packetevents.packetwrappers.play.in.blockdig.WrappedPacketInBlockDig;
import net.foulest.packetevents.packetwrappers.play.in.blockplace.WrappedPacketInBlockPlace;
import net.foulest.packetevents.packetwrappers.play.in.chat.WrappedPacketInChat;
import net.foulest.packetevents.packetwrappers.play.in.clientcommand.WrappedPacketInClientCommand;
import net.foulest.packetevents.packetwrappers.play.in.custompayload.WrappedPacketInCustomPayload;
import net.foulest.packetevents.packetwrappers.play.in.entityaction.WrappedPacketInEntityAction;
import net.foulest.packetevents.packetwrappers.play.in.flying.WrappedPacketInFlying;
import net.foulest.packetevents.packetwrappers.play.in.helditemslot.WrappedPacketInHeldItemSlot;
import net.foulest.packetevents.packetwrappers.play.in.setcreativeslot.WrappedPacketInSetCreativeSlot;
import net.foulest.packetevents.packetwrappers.play.in.settings.WrappedPacketInSettings;
import net.foulest.packetevents.packetwrappers.play.in.spectate.WrappedPacketInSpectate;
import net.foulest.packetevents.packetwrappers.play.in.steervehicle.WrappedPacketInSteerVehicle;
import net.foulest.packetevents.packetwrappers.play.in.tabcomplete.WrappedPacketInTabComplete;
import net.foulest.packetevents.packetwrappers.play.in.transaction.WrappedPacketInTransaction;
import net.foulest.packetevents.packetwrappers.play.in.updatesign.WrappedPacketInUpdateSign;
import net.foulest.packetevents.packetwrappers.play.in.useentity.WrappedPacketInUseEntity;
import net.foulest.packetevents.packetwrappers.play.in.windowclick.WrappedPacketInWindowClick;
import net.foulest.packetevents.utils.player.ClientVersion;
import net.foulest.packetevents.utils.player.Direction;
import net.foulest.packetevents.utils.vector.Vector3d;
import net.foulest.packetevents.utils.vector.Vector3f;
import net.foulest.packetevents.utils.vector.Vector3i;
import net.foulest.vulture.action.ActionType;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.data.PlayerDataManager;
import net.foulest.vulture.event.MovementEvent;
import net.foulest.vulture.event.RotationEvent;
import net.foulest.vulture.processor.Processor;
import net.foulest.vulture.util.*;
import net.foulest.vulture.util.data.CustomLocation;
import net.foulest.packetevents.utils.vector.Vector2f;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.*;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Handles all incoming packets after they are decoded.
 *
 * @author Foulest
 */
public class PacketReceiveProcessor extends Processor {

    /**
     * Handles incoming packets after they are decoded.
     *
     * @param event PacketPlayReceiveEvent
     */
    @Override
    public void onPacketPlayReceive(@NotNull PacketPlayReceiveEvent event) {
        byte packetId = event.getPacketId();

        // Ignores invalid incoming packets.
        if (PacketType.getPacketFromId(packetId) == null) {
            return;
        }

        Player player = event.getPlayer();

        // Cancels incoming packets for invalid players.
        if (player == null) {
            event.setCancelled(true);
            return;
        }

        UUID uniqueId = player.getUniqueId();

        // Cancels incoming packets for invalid players.
        if (Bukkit.getPlayer(uniqueId) == null) {
            event.setCancelled(true);
            return;
        }

        // Cancels incoming packets for offline players.
        if (!player.isOnline()) {
            event.setCancelled(true);
            return;
        }

        // Cancels incoming packets for players being kicked.
        if (KickUtil.isPlayerBeingKicked(player)) {
            event.setCancelled(true);
            return;
        }

        NMSPacket nmsPacket = event.getNMSPacket();

        String playerName = player.getName();
        World world = player.getWorld();
        InventoryView openInventory = player.getOpenInventory();
        boolean insideVehicle = player.isInsideVehicle();
        Entity vehicle = player.getVehicle();
        GameMode gameMode = player.getGameMode();
        Location playerLoc = player.getLocation();

        PlayerData playerData = PlayerDataManager.getPlayerData(player);
        boolean playerFlightAllowed = playerData.isFlightAllowed();
        boolean inventoryOpen = playerData.isInventoryOpen();
        boolean digging = playerData.isDigging();
        boolean placingBlock = playerData.isPlacingBlock();

        switch (packetId) {
            case PacketType.Play.Client.ABILITIES:
                WrappedPacketInAbilities abilities = new WrappedPacketInAbilities(nmsPacket);
                boolean abilitiesFlying = abilities.isFlying();

                if (playerData.getVersion().isOlderThanOrEquals(ClientVersion.v_1_8_9)) {
                    boolean playerFlying = playerData.isFlying();

                    if (abilitiesFlying == playerFlying) {
                        KickUtil.kickPlayer(player, event, Settings.abilitiesDuplicateFlying,
                                "Sent Abilities packet with duplicate flying values"
                                        + " (flying=" + abilitiesFlying
                                        + " playerFlying=" + playerFlying + ")"
                        );
                        return;
                    }

                    if (abilitiesFlying && !playerFlightAllowed) {
                        KickUtil.kickPlayer(player, event, Settings.abilitiesInvalidFlying,
                                "Sent Abilities packet with invalid flying values"
                                        + " (flying=" + true
                                        + " playerFlightAllowed=" + false + ")"
                        );
                        return;
                    }

                    abilities.isFlightAllowed().ifPresent(flightAllowed -> {
                        if (Boolean.TRUE.equals(flightAllowed) != playerFlightAllowed) {
                            KickUtil.kickPlayer(player, event, Settings.abilitiesInvalidFlightAllowed,
                                    "Sent Abilities packet with invalid flight allowed values"
                                            + " (flightAllowed=" + flightAllowed
                                            + " playerFlightAllowed=" + playerFlightAllowed + ")"
                            );
                        }
                    });

                    abilities.canInstantlyBuild().ifPresent(instantBuild -> {
                        boolean playerInstantBuild = playerData.isInstantBuild();

                        if (Boolean.TRUE.equals(instantBuild) != playerInstantBuild) {
                            KickUtil.kickPlayer(player, event, Settings.abilitiesInvalidInstantBuild,
                                    "Sent Abilities packet with invalid instant build values"
                                            + " (instantBuild=" + instantBuild
                                            + " playerInstantBuild=" + playerInstantBuild + ")"
                            );
                        }
                    });

                    abilities.isVulnerable().ifPresent(vulnerable -> {
                        boolean playerVulnerable = playerData.isVulnerable();

                        if (Boolean.TRUE.equals(vulnerable) != playerVulnerable) {
                            KickUtil.kickPlayer(player, event, Settings.abilitiesInvalidInvulnerable,
                                    "Sent Abilities packet with invalid invulnerable values"
                                            + " (invulnerable=" + vulnerable
                                            + " playerInvulnerable=" + playerVulnerable + ")"
                            );
                        }
                    });
                }

                if (abilitiesFlying) {
                    playerData.setFlying(true);
                    playerData.setTimestamp(ActionType.START_FLYING);
                } else {
                    playerData.setFlying(false);
                    playerData.setTimestamp(ActionType.STOP_FLYING);
                }

                abilities.isFlightAllowed().ifPresent(playerData::setFlightAllowed);
                abilities.canInstantlyBuild().ifPresent(playerData::setInstantBuild);
                abilities.isVulnerable().ifPresent(playerData::setVulnerable);
                break;

            case PacketType.Play.Client.ARM_ANIMATION:
                playerData.setTimestamp(ActionType.ARM_ANIMATION);
                break;

            case PacketType.Play.Client.BLOCK_DIG:
                WrappedPacketInBlockDig blockDig = new WrappedPacketInBlockDig(nmsPacket);
                Vector3i digBlockPosition = blockDig.getBlockPosition();
                Direction digDirection = blockDig.getDirection();
                int digXPos = digBlockPosition.getX();
                int digYPos = digBlockPosition.getY();
                int digZPos = digBlockPosition.getZ();

                if (blockDig.getDigType() == null) {
                    break;
                }

                if (Settings.blockDigInvalidDistance
                        && digXPos != 0 && digYPos != 0 && digZPos != 0
                        && digXPos != -1 && digYPos != -1 && digZPos != -1) {
                    Location blockLocation = new Location(world, digXPos, digYPos, digZPos);
                    double distance = playerLoc.distance(blockLocation);

                    if (distance > 7.03) {
                        event.setCancelled(true);
                        return;
                    }
                }

                switch (blockDig.getDigType()) {
                    case START_DESTROY_BLOCK:
                        // Ignores players in creative mode.
                        if (gameMode == GameMode.CREATIVE) {
                            break;
                        }

                        // Checks the block being dug.
                        TaskUtil.runTask(() -> {
                            Block block = world.getBlockAt(digXPos, digYPos, digZPos);

                            // Checks for and ignores instantly breakable blocks.
                            switch (block.getType()) {
                                case AIR:
                                case BROWN_MUSHROOM:
                                case CARROT:
                                case CROPS:
                                case DEAD_BUSH:
                                case DOUBLE_PLANT:
                                case FLOWER_POT:
                                case LONG_GRASS:
                                case MELON_STEM:
                                case NETHER_WARTS:
                                case POTATO:
                                case PUMPKIN_STEM:
                                case REDSTONE_TORCH_OFF:
                                case REDSTONE_TORCH_ON:
                                case REDSTONE_WIRE:
                                case RED_MUSHROOM:
                                case RED_ROSE:
                                case SAPLING:
                                case SLIME_BLOCK:
                                case SUGAR_CANE_BLOCK:
                                case TNT:
                                case TORCH:
                                case TRIPWIRE:
                                case TRIPWIRE_HOOK:
                                case VINE:
                                case WATER_LILY:
                                case YELLOW_FLOWER:
                                    playerData.setTimestamp(ActionType.BLOCK_BREAK);
                                    break;

                                default:
                                    playerData.setDigging(true);
                                    break;
                            }
                        });
                        break;

                    case STOP_DESTROY_BLOCK:
                    case ABORT_DESTROY_BLOCK:
                        playerData.setTimestamp(ActionType.BLOCK_BREAK);
                        playerData.setDigging(false);
                        break;

                    case RELEASE_USE_ITEM:
                        if (!(digDirection == Direction.DOWN
                                && digXPos == 0 && digYPos == 0 && digZPos == 0)) {
                            KickUtil.kickPlayer(player, event, Settings.releaseUseItemInvalidData,
                                    "Sent ReleaseUseItem packet with invalid data"
                                            + " (direction=" + digDirection
                                            + " x=" + digXPos
                                            + " y=" + digYPos
                                            + " z=" + digZPos + ")"
                            );
                            return;
                        }

                        playerData.setBlocking(false);
                        playerData.setShootingBow(false);
                        playerData.setEating(false);
                        playerData.setDrinking(false);
                        playerData.setTimestamp(ActionType.RELEASE_USE_ITEM);
                        break;

                    case DROP_ITEM:
                    case DROP_ALL_ITEMS:
                        if (!(digDirection == Direction.DOWN
                                && digXPos == 0 && digYPos == 0 && digZPos == 0)) {
                            KickUtil.kickPlayer(player, event, Settings.itemDropInvalidData,
                                    "Sent ItemDrop packet with invalid data"
                                            + " (direction=" + digDirection
                                            + " x=" + digXPos
                                            + " y=" + digYPos
                                            + " z=" + digZPos + ")"
                            );
                            return;
                        }
                        break;

                    default:
                        break;
                }
                break;

            case PacketType.Play.Client.BLOCK_PLACE:
                WrappedPacketInBlockPlace blockPlace = new WrappedPacketInBlockPlace(nmsPacket);
                Vector3i placeBlockPosition = blockPlace.getBlockPosition();
                Direction placeDirection = blockPlace.getDirection();
                int placeXPos = placeBlockPosition.getX();
                int placeYPos = placeBlockPosition.getY();
                int placeZPos = placeBlockPosition.getZ();

                if (Settings.blockPlaceInvalidDistance
                        && placeXPos != -1 && placeYPos != -1 && placeZPos != -1) {
                    Location blockLocation = new Location(world, placeXPos, placeYPos, placeZPos);
                    double distance = playerLoc.distance(blockLocation);

                    if (distance > 7.03) {
                        event.setCancelled(true);
                        return;
                    }
                }

                if (blockPlace.getCursorPosition().isPresent()) {
                    Vector3f cursorPos = blockPlace.getCursorPosition().get();
                    float cursorPosX = cursorPos.getX();
                    float cursorPosY = cursorPos.getY();
                    float cursorPosZ = cursorPos.getZ();

                    if ((cursorPosX < 0.0 || cursorPosX > 1.0)
                            || (cursorPosY < 0.0 || cursorPosY > 1.0)
                            || (cursorPosZ < 0.0 || cursorPosZ > 1.0)) {
                        KickUtil.kickPlayer(player, event, Settings.blockPlaceInvalidCursorPosition,
                                "Sent BlockPlace packet with invalid cursor position"
                                        + " (x=" + cursorPosX
                                        + " y=" + cursorPosY
                                        + " z=" + cursorPosZ + ")"
                        );
                        return;
                    }

                    if (placeDirection == Direction.OTHER
                            && (cursorPosX != 0.0
                            || cursorPos.getY() != 0.0
                            || cursorPos.getZ() != 0.0)) {
                        KickUtil.kickPlayer(player, event, Settings.blockPlaceInvalidOtherCursorPosition,
                                "Sent BlockPlace packet with invalid OTHER cursor position"
                                        + " (x=" + cursorPosX
                                        + " y=" + cursorPosY
                                        + " z=" + cursorPosZ + ")"
                        );
                        return;
                    }
                }

                if (placeDirection == Direction.UP
                        && placeXPos == 0.0 && placeYPos == 0.0 && placeZPos == 0.0) {
                    KickUtil.kickPlayer(player, event, Settings.blockPlaceInvalidUpBlockPosition,
                            "Sent BlockPlace packet with invalid UP block position"
                                    + " (x=" + placeXPos
                                    + " y=" + placeYPos
                                    + " z=" + placeZPos + ")"
                    );
                    return;
                }

                if (placeDirection == Direction.OTHER
                        && placeXPos != -1 && placeYPos != -1 && placeZPos != -1) {
                    KickUtil.kickPlayer(player, event, Settings.blockPlaceInvalidOtherBlockPosition,
                            "Sent BlockPlace packet with invalid OTHER block position"
                                    + " (x=" + placeXPos
                                    + " y=" + placeYPos
                                    + " z=" + placeZPos + ")"
                    );
                    return;
                }

                if (blockPlace.getItemStack().isPresent()) {
                    ItemStack itemStack = blockPlace.getItemStack().get();
                    Material type = itemStack.getType();

                    if (blockPlace.getDirection() == Direction.OTHER) {
                        switch (type) {
                            case WOOD_SWORD:
                            case STONE_SWORD:
                            case IRON_SWORD:
                            case GOLD_SWORD:
                            case DIAMOND_SWORD:
                                playerData.setBlocking(true);
                                playerData.setTimestamp(ActionType.BLOCKING);
                                break;

                            case APPLE:
                            case MUSHROOM_SOUP:
                            case BREAD:
                            case PORK:
                            case GRILLED_PORK:
                            case RAW_FISH:
                            case COOKED_FISH:
                            case COOKIE:
                            case MELON:
                            case RAW_BEEF:
                            case COOKED_BEEF:
                            case RAW_CHICKEN:
                            case COOKED_CHICKEN:
                            case ROTTEN_FLESH:
                            case SPIDER_EYE:
                            case CARROT:
                            case POTATO:
                            case BAKED_POTATO:
                            case POISONOUS_POTATO:
                            case GOLDEN_CARROT:
                            case PUMPKIN_PIE:
                            case RABBIT:
                            case COOKED_RABBIT:
                            case RABBIT_STEW:
                            case MUTTON:
                            case COOKED_MUTTON:
                                if (player.getFoodLevel() < 20) {
                                    playerData.setEating(true);
                                }
                                break;

                            case GOLDEN_APPLE:
                                playerData.setEating(true);
                                break;

                            case POTION:
                                // Excludes splash potions.
                                if (itemStack.getDurability() == 0) {
                                    playerData.setDrinking(true);
                                }
                                break;

                            case BOW:
                                playerData.setShootingBow(true);
                                break;

                            default:
                                break;
                        }
                    } else if (type.isBlock() && type != Material.AIR) {
                        playerData.setPlacingBlock(true);
                        playerData.setTimestamp(ActionType.PLACING_BLOCK);
                    }
                }
                break;

            case PacketType.Play.Client.CHAT:
                WrappedPacketInChat chat = new WrappedPacketInChat(nmsPacket);

                if (inventoryOpen || digging || placingBlock) {
                    KickUtil.kickPlayer(player, event, Settings.chatInvalidConditions,
                            "Sent Chat packet with invalid conditions"
                                    + " (inventoryOpen=" + inventoryOpen
                                    + " digging=" + digging
                                    + " placingBlock=" + placingBlock + ")");
                    return;
                }

                String message = chat.getMessage();

                if (message.isEmpty()) {
                    KickUtil.kickPlayer(player, event, Settings.chatInvalidMessage,
                            "Sent Chat packet with invalid message"
                                    + " (message=" + message + ")"
                    );
                    return;
                }

                playerData.setTimestamp(ActionType.CHATTING);
                break;

            case PacketType.Play.Client.CLIENT_COMMAND:
                WrappedPacketInClientCommand clientCommand = new WrappedPacketInClientCommand(nmsPacket);

                switch (clientCommand.getClientCommand()) {
                    case OPEN_INVENTORY_ACHIEVEMENT:
                        playerData.setInventoryOpen(true);
                        playerData.setTimestamp(ActionType.INVENTORY_OPEN);
                        break;

                    case PERFORM_RESPAWN:
                        boolean dead = player.isDead();

                        if (inventoryOpen || placingBlock || digging || !dead) {
                            KickUtil.kickPlayer(player, event, Settings.respawnInvalidConditions,
                                    "Sent Respawn packet with invalid conditions"
                                            + " (inventoryOpen=" + inventoryOpen
                                            + " placingBlock=" + placingBlock
                                            + " digging=" + digging
                                            + " dead=" + dead + ")"
                            );
                            return;
                        }

                        playerData.setTimestamp(ActionType.RESPAWN);
                        break;

                    default:
                        break;
                }
                break;

            case PacketType.Play.Client.CLOSE_WINDOW:
                if (placingBlock || digging) {
                    KickUtil.kickPlayer(player, event, Settings.closeWindowInvalidConditions,
                            "Sent CloseWindow packet with invalid conditions"
                                    + " (placingBlock=" + placingBlock
                                    + " digging=" + digging + ")"
                    );
                    return;
                }

                playerData.setInventoryOpen(false);
                playerData.setTimestamp(ActionType.CLOSE_WINDOW);
                break;

            case PacketType.Play.Client.CUSTOM_PAYLOAD:
                WrappedPacketInCustomPayload customPayload = new WrappedPacketInCustomPayload(nmsPacket);
                String channelName = customPayload.getChannelName();
                byte[] rawData = customPayload.getData();
                int payloadSize = rawData.length;
                String data = new String(rawData, StandardCharsets.UTF_8);

                // Checks for payloads with invalid sizes.
                if (payloadSize > 15000 || payloadSize == 0) {
                    KickUtil.kickPlayer(player, event, Settings.customPayloadInvalidSize,
                            "Sent CustomPayload packet with invalid size"
                                    + " (size=" + payloadSize + ")"
                    );
                    return;
                }

                // Checks for invalid item name payloads.
                if (channelName.equals("MC|ItemName")) {
                    if (payloadSize > (playerData.getVersion().isOlderThanOrEquals(ClientVersion.v_1_8_9) ? 31 : 32)) {
                        KickUtil.kickPlayer(player, event, Settings.itemNameInvalidSize,
                                "Sent ItemName payload with invalid size"
                                        + " (size=" + payloadSize + ")"
                        );
                        return;
                    }

                    if (!(!data.isEmpty() && data.charAt(0) == '\b')
                            && !(!data.isEmpty() && data.charAt(0) == '\t')
                            && !(!data.isEmpty() && data.charAt(0) == '\n')
                            && !(!data.isEmpty() && data.charAt(0) == '\u0000')
                            && !(!data.isEmpty() && data.charAt(0) == '\u0001')
                            && !(!data.isEmpty() && data.charAt(0) == '\u0002')
                            && !(!data.isEmpty() && data.charAt(0) == '\u0003')
                            && !(!data.isEmpty() && data.charAt(0) == '\u0004')
                            && !(!data.isEmpty() && data.charAt(0) == '\u0005')
                            && !(!data.isEmpty() && data.charAt(0) == '\u0006')
                            && !(!data.isEmpty() && data.charAt(0) == '\u0007')
                            && !(!data.isEmpty() && data.charAt(0) == '\u000B')
                            && !(!data.isEmpty() && data.charAt(0) == '\f')
                            && !(!data.isEmpty() && data.charAt(0) == '\r')
                            && !(!data.isEmpty() && data.charAt(0) == '\u000E')
                            && !(!data.isEmpty() && data.charAt(0) == '\u000F')
                            && !(!data.isEmpty() && data.charAt(0) == '\u0010')
                            && !(!data.isEmpty() && data.charAt(0) == '\u0011')
                            && !(!data.isEmpty() && data.charAt(0) == '\u0012')
                            && !(!data.isEmpty() && data.charAt(0) == '\u0013')
                            && !(!data.isEmpty() && data.charAt(0) == '\u0014')
                            && !(!data.isEmpty() && data.charAt(0) == '\u0015')
                            && !(!data.isEmpty() && data.charAt(0) == '\u0016')
                            && !(!data.isEmpty() && data.charAt(0) == '\u0017')
                            && !(!data.isEmpty() && data.charAt(0) == '\u0018')
                            && !(!data.isEmpty() && data.charAt(0) == '\u0019')
                            && !(!data.isEmpty() && data.charAt(0) == '\u001A')
                            && !(!data.isEmpty() && data.charAt(0) == '\u001B')
                            && !(!data.isEmpty() && data.charAt(0) == '\u001C')
                            && !(!data.isEmpty() && data.charAt(0) == '\u001D')
                            && !(!data.isEmpty() && data.charAt(0) == '\u001E')) {
                        KickUtil.kickPlayer(player, event, Settings.itemNameInvalidData,
                                "Sent ItemName payload with invalid data"
                                        + " (data=" + data + ")"
                        );

                        // TODO: Remove this in production.
                        FileUtil.printDataToFile(data, "item-name-data.txt");
                        return;
                    }
                }

                // Checks for invalid trade select payloads.
                if (channelName.equals("MC|TrSel")
                        && !data.equals("\u0000\u0000\u0000\u0000")
                        && !data.equals("\u0000\u0000\u0000\u0001")
                        && !data.equals("\u0000\u0000\u0000\u0002")) {
                    KickUtil.kickPlayer(player, event, Settings.tradeSelectInvalidData,
                            "Sent TradeSelect payload with invalid data"
                                    + " (data=" + data + ")"
                    );
                    return;
                }

                // Checks for invalid pick item payloads.
                if (channelName.equals("MC|PickItem") && !data.equals("\t")) {
                    // TODO: Remove this in production.
                    FileUtil.printDataToFile(data, "pick-item-data.txt");
                }

                // Checks for invalid beacon payloads.
                if (channelName.equals("MC|Beacon")) {
                    InventoryType type = openInventory.getType();

                    if (type != InventoryType.BEACON) {
                        KickUtil.kickPlayer(player, event, Settings.beaconInvalidConditions,
                                "Sent Beacon payload with invalid conditions"
                                        + " (inventoryType=" + type + ")"
                        );
                        return;
                    }

                    BlockState beacon = (BlockState) openInventory.getTopInventory().getHolder();
                    Location beaconLoc = beacon.getLocation();
                    int beaconTier = BeaconUtil.getTier(beaconLoc);

                    if (data.charAt(0) == '\u0000'
                            && data.charAt(1) == '\u0000'
                            && data.charAt(2) == '\u0000'
                            && data.charAt(4) == '\u0000'
                            && data.charAt(5) == '\u0000'
                            && data.charAt(6) == '\u0000'
                            && (data.charAt(7) == '\u0000' || data.charAt(7) == '\n' || data.charAt(7) == '\u0001'
                            || data.charAt(7) == '\u0003' || data.charAt(7) == '\u000B' || data.charAt(7) == '\u0008'
                            || data.charAt(7) == '\u0005')) {
                        char beaconEffect = data.charAt(3);

                        // Checks for invalid beacon effect payloads.
                        if (beaconEffect != '\u0001' // Speed
                                && beaconEffect != '\u0003' // Haste
                                && beaconEffect != '\u000B' // Resistance
                                && beaconEffect != '\u0008' // Jump Boost
                                && beaconEffect != '\u0005' // Strength
                        ) {
                            KickUtil.kickPlayer(player, event, Settings.beaconInvalidEffect,
                                    "Sent Beacon payload with invalid effect"
                                            + " (effect=" + beaconEffect + ")"
                            );
                            return;
                        }

                        // Checks for players sending beacon effects that don't match the beacon tier.
                        switch (beaconTier) {
                            case 1:
                                if (beaconEffect == '\u000B'
                                        || beaconEffect == '\u0008'
                                        || beaconEffect == '\u0005') {
                                    KickUtil.kickPlayer(player, event, Settings.beaconInvalidTier,
                                            "Sent Beacon payload with invalid tier"
                                                    + " (tier=" + beaconTier
                                                    + " (data=" + data + ")"
                                    );
                                    return;
                                }
                                break;

                            case 2:
                                if (beaconEffect == '\u0005') {
                                    KickUtil.kickPlayer(player, event, Settings.beaconInvalidTier,
                                            "Sent Beacon payload with invalid tier"
                                                    + " (tier=" + beaconTier
                                                    + " (data=" + data + ")"
                                    );
                                    return;
                                }
                                break;

                            default:
                                break;
                        }

                    } else {
                        KickUtil.kickPlayer(player, event, Settings.beaconInvalidData,
                                "Sent Beacon payload with invalid data"
                                        + " (data=" + data + ")"
                        );
                        return;
                    }
                }

                // Checks for invalid book-related payloads.
                if (channelName.equals("MC|BOpen")
                        || channelName.equals("MC|BEdit")
                        || channelName.equals("MC|BSign")) {
                    ItemStack itemInHand = player.getInventory().getItemInHand();

                    // Checks for invalid book open payloads.
                    // (The client doesn't send this payload; the server does)
                    if (channelName.equals("MC|BOpen")) {
                        KickUtil.kickPlayer(player, event, Settings.bookOpenInvalidConditions,
                                "Sent BookOpen payload with invalid conditions"
                        );
                        return;
                    }

                    int length = data.length();

                    // Checks for invalid book edit payloads.
                    if (channelName.equals("MC|BEdit")) {
                        if (itemInHand == null || !itemInHand.getType().toString().toLowerCase(Locale.ROOT).contains("book")) {
                            KickUtil.kickPlayer(player, event, Settings.bookEditInvalidConditions,
                                    "Sent BookEdit payload with invalid conditions"
                                            + " (itemInHand=" + itemInHand + ")"
                            );
                            return;
                        }

                        if (!data.startsWith("\u0001ï¿½\u0001\u0000\u0000\n\u0000\u0000\t"
                                + "\u0000\u0005pages\b\u0000\u0000\u0000\u0001\u0000\f")
                                && !(!data.isEmpty() && data.charAt(length - 1) == '\u0000')) {
                            KickUtil.kickPlayer(player, event, Settings.bookEditInvalidData,
                                    "Sent BookEdit payload with invalid data"
                                            + " (" + data + ")"
                            );
                            return;
                        }
                    }

                    // Checks for invalid book sign payloads.
                    if (channelName.equals("MC|BSign")) {
                        if (itemInHand == null || !itemInHand.getType().toString().toLowerCase(Locale.ROOT).contains("book")) {
                            KickUtil.kickPlayer(player, event, Settings.bookSignInvalidConditions,
                                    "Sent BookSign payload with invalid conditions"
                                            + " (itemInHand=" + itemInHand + ")"
                            );
                            return;
                        }

                        if (!data.startsWith("\u0001ï¿½\u0001\u0000\u0000\n\u0000\u0000\t"
                                + "\u0000\u0005pages\b\u0000\u0000\u0000\u0001\u0000\f")
                                && !(!data.isEmpty() && data.charAt(length - 1) == '\u0000')
                                && !data.contains("\b\u0000\u0006author\u0000\u0007")
                                && !data.contains("\b\u0000\u0005title\u0000\u0004")) {
                            KickUtil.kickPlayer(player, event, Settings.bookSignInvalidData,
                                    "Sent BookSign payload with invalid data"
                                            + " (" + data + ")"
                            );
                            return;
                        }
                    }
                }

                boolean playerIsOp = player.isOp();

                if ((channelName.equals("MC|AdvCdm") || channelName.equals("MC|AutoCmd")) && !playerIsOp) {
                    KickUtil.kickPlayer(player, event, Settings.commandBlockInvalidConditions,
                            "Sent CommandBlock payload with invalid conditions"
                                    + " (op=" + false + ")"
                    );
                    return;
                }

                playerData.setTimestamp(ActionType.SENT_PAYLOAD);
                break;

            case PacketType.Play.Client.DIFFICULTY_CHANGE: // Players shouldn't be able to send this packet
                event.setCancelled(true);
                break;

            case PacketType.Play.Client.ENTITY_ACTION:
                WrappedPacketInEntityAction entityAction = new WrappedPacketInEntityAction(nmsPacket);
                WrappedPacketInEntityAction.PlayerAction playerAction = entityAction.getAction();
                int jumpBoost = entityAction.getJumpBoost();

                if (playerAction != WrappedPacketInEntityAction.PlayerAction.RIDING_JUMP && jumpBoost != 0) {
                    KickUtil.kickPlayer(player, event, Settings.entityActionInvalidJumpBoost,
                            "Send EntityAction packet with invalid jump boost");
                    return;
                }

                boolean sneaking = playerData.isSneaking();

                switch (playerAction) {
                    case START_SPRINTING:
                        playerData.setSprinting(true);
                        playerData.setTimestamp(ActionType.START_SPRINTING);
                        break;

                    case STOP_SPRINTING:
                        playerData.setSprinting(false);
                        playerData.setTimestamp(ActionType.STOP_SPRINTING);
                        break;

                    case START_SNEAKING:
                        if (sneaking) {
                            KickUtil.kickPlayer(player, event, Settings.startSneakingInvalidConditions,
                                    "Sent StartSneaking packet with invalid conditions"
                                            + " (sneaking=" + true + ")"
                            );
                            return;
                        }

                        playerData.setSneaking(true);
                        playerData.setTimestamp(ActionType.SNEAKING);
                        break;

                    case STOP_SNEAKING:
                        playerData.setSneaking(false);
                        playerData.setTimestamp(ActionType.SNEAKING);
                        break;

                    case OPEN_INVENTORY:
                        if (insideVehicle) {
                            if (vehicle instanceof Horse) {
                                Horse horse = (Horse) vehicle;

                                if (!horse.isTamed()) {
                                    break;
                                }
                            }
                        }

                        playerData.setInventoryOpen(true);
                        playerData.setTimestamp(ActionType.INVENTORY_OPEN);
                        break;

                    case STOP_SLEEPING:
                        boolean sleeping = player.isSleeping();

                        if (inventoryOpen || !sleeping) {
                            KickUtil.kickPlayer(player, event, Settings.stopSleepingInvalidConditions,
                                    "Sent StopSleeping packet with invalid conditions"
                                            + " (inventoryOpen=" + inventoryOpen
                                            + " sleeping=" + sleeping + ")"
                            );
                            return;
                        }
                        break;

                    case RIDING_JUMP:
                    case START_RIDING_JUMP:
                    case STOP_RIDING_JUMP:
                        EntityType entityType = vehicle.getType();

                        if (!insideVehicle || entityType != EntityType.HORSE) {
                            KickUtil.kickPlayer(player, event, Settings.ridingJumpInvalidConditions,
                                    "Sent RidingJump packet with invalid conditions"
                                            + " (insideVehicle=" + insideVehicle
                                            + " vehicle=" + (insideVehicle ? entityType : null) + ")"
                            );
                            return;
                        }

                        if (jumpBoost < 0 || jumpBoost > 100) {
                            KickUtil.kickPlayer(player, event, Settings.ridingJumpInvalidJumpBoost,
                                    "Sent RidingJump packet with invalid jump boost"
                                            + " (jumpBoost=" + jumpBoost + ")"
                            );
                            return;
                        }
                        break;

                    default:
                        break;
                }
                break;

            case PacketType.Play.Client.POSITION:
            case PacketType.Play.Client.POSITION_LOOK:
            case PacketType.Play.Client.LOOK:
            case PacketType.Play.Client.FLYING:
                WrappedPacketInFlying flying = new WrappedPacketInFlying(nmsPacket);
                Vector3d flyingPosition = flying.getPosition();
                double flyingXPos = flyingPosition.getX();
                double flyingYPos = flyingPosition.getY();
                double flyingZPos = flyingPosition.getZ();
                float flyingYaw = flying.getYaw();
                float flyingPitch = flying.getPitch();

                // Handles invalid Y data.
                if (Math.abs(flyingYPos) > 1.0E9) {
                    KickUtil.kickPlayer(player, event, Settings.flyingInvalidYData,
                            "Sent Flying packet with invalid Y data"
                                    + " (y=" + flyingYPos + ")"
                    );
                    return;
                }

                // Handles empty Flying packets.
                if (!flying.isMoving() && !flying.isRotating()) {
                    if (!(flyingXPos == 0 && flyingYPos == 0 && flyingZPos == 0)) {
                        KickUtil.kickPlayer(player, event, Settings.flyingInvalidPositionData,
                                "Sent Flying packet with invalid position data"
                                        + " (x=" + flyingXPos
                                        + " y=" + flyingYPos
                                        + " z=" + flyingZPos + ")"
                        );
                        return;
                    }

                    if (!(flyingYaw == 0.0 && flyingPitch == 0.0)) {
                        KickUtil.kickPlayer(player, event, Settings.flyingInvalidRotationData,
                                "Sent Flying packet with invalid rotation data"
                                        + " (yaw=" + flyingYaw
                                        + " pitch=" + flyingPitch + ")"
                        );
                        return;
                    }
                }

                // Handles setting last locations.
                Location currentLocation = playerData.getLocation();

                if (currentLocation == null) {
                    currentLocation = playerLoc;
                }

                float yaw = currentLocation.getYaw();
                float pitch = currentLocation.getPitch();
                Location location = new Location(world, flyingXPos, flyingYPos, flyingZPos, yaw, pitch);

                playerData.setLocation(location);

                // Handles player packet data (from Dusk).
                // Bug fix: Ignores Flying packets sent after blocking/releasing.
                // Bug fix: Ignores Flying packets sent after breaking blocks.
                // Bug fix: Ignores Flying packets sent after being teleported.
                if (playerData.getTicksSince(ActionType.BLOCKING) > 1
                        && playerData.getTicksSince(ActionType.RELEASE_USE_ITEM) > 1
                        && playerData.getTicksSince(ActionType.BLOCK_BREAK) > 1
                        && playerData.getTicksSince(ActionType.TELEPORT) > 2) {
                    if (!flying.isMoving() && !flying.isRotating()) {
                        playerData.handlePlayerPacket(new CustomLocation(null, null));
                    } else if (!flying.isMoving() && flying.isRotating()) {
                        playerData.handlePlayerPacket(new CustomLocation(null, new Vector2f(flyingYaw, flyingPitch)));
                    } else if (flying.isMoving() && !flying.isRotating()) {
                        playerData.handlePlayerPacket(new CustomLocation(new Vector3d(flyingXPos, flyingYPos, flyingZPos), null));
                    } else {
                        playerData.handlePlayerPacket(new CustomLocation(new Vector3d(flyingXPos, flyingYPos, flyingZPos), new Vector2f(flyingYaw, flyingPitch)));
                    }
                }

                // Handles player rotations.
                if (flying.isRotating()) {
                    if (Math.abs(flyingPitch) > 90.0) {
                        KickUtil.kickPlayer(player, event, Settings.flyingInvalidPitch,
                                "Sent Flying packet with invalid pitch"
                                        + " (pitch=" + flyingPitch + ")"
                        );
                        return;
                    }

                    WrappedPacketInFlying lastRotationPacket = playerData.getLastRotationPacket();

                    if (lastRotationPacket != null) {
                        float fromPitch = lastRotationPacket.getPitch();
                        float fromYaw = lastRotationPacket.getYaw();

                        // Checks if the player has rotated.
                        if (Math.abs(flyingPitch - fromPitch) != 0.0
                                || Math.abs(flyingYaw - fromYaw) != 0.0) {
                            // Ignores teleport packets.
                            handleRotationChecks(playerData, new RotationEvent(flying, lastRotationPacket));
                        }
                    }

                    playerData.setLastRotationPacket(flying);
                }

                // Handles player movement.
                if (flying.isMoving()) {
                    int enterVehicleTicks = playerData.getTicksSince(ActionType.ENTER_VEHICLE);

                    if (insideVehicle && enterVehicleTicks > 2) {
                        event.setCancelled(true);
                        vehicle.eject();
                        MessageUtil.debug("Flying packet ignored for " + playerName + " (inside vehicle) " + enterVehicleTicks);
                    }

                    WrappedPacketInFlying lastPositionPacket = playerData.getLastPositionPacket();

                    if (lastPositionPacket != null) {
                        Vector3d fromPosition = lastPositionPacket.getPosition();
                        double fromXPos = fromPosition.getX();
                        double fromYPos = fromPosition.getY();
                        double fromZPos = fromPosition.getZ();

                        // Checks if the player has moved.
                        if (Math.abs(flyingXPos - fromXPos) != 0.0
                                || Math.abs(flyingYPos - fromYPos) != 0.0
                                || Math.abs(flyingZPos - fromZPos) != 0.0) {

                            // Ignores teleport packets.
                            playerData.setMoving(true);
                            handleMovementChecks(playerData, new MovementEvent(playerData,
                                    flying, lastPositionPacket, event));
                        } else {
                            playerData.setMoving(false);
                        }
                    }

                    playerData.setLastPositionPacket(flying);
                }

                if (playerData.getTicksSince(ActionType.ARM_ANIMATION) > 0) {
                    playerData.setDigging(false);
                }

                int totalTicks = playerData.getTotalTicks();
                int lastFlyingTicks = playerData.getLastFlyingTicks();
                int lastDroppedPackets = playerData.getLastDroppedPackets();

                playerData.setPlacingBlock(false);
                playerData.setLastDroppedPackets(totalTicks - lastFlyingTicks > 2 ? totalTicks : lastDroppedPackets);
                playerData.setLastFlyingTicks(totalTicks);
                playerData.setTimestamp(ActionType.FLYING_PACKET);
                break;

            case PacketType.Play.Client.HELD_ITEM_SLOT:
                WrappedPacketInHeldItemSlot heldItemSlotPacket = new WrappedPacketInHeldItemSlot(nmsPacket);
                int heldItemSlot = heldItemSlotPacket.getCurrentSelectedSlot();
                int inventoryOpenTicks = playerData.getTicksSince(ActionType.INVENTORY_OPEN);

                if (inventoryOpen && inventoryOpenTicks > 5) {
                    KickUtil.kickPlayer(player, event, Settings.heldItemSlotInvalidConditions,
                            "Sent HeldItemSlot packet with invalid conditions"
                                    + " (timeSinceInventoryOpen=" + inventoryOpenTicks + ")"
                    );
                    return;
                }

                int currentSlot = playerData.getCurrentSlot();

                if (heldItemSlot == currentSlot
                        && playerData.getVersion().isOlderThanOrEquals(ClientVersion.v_1_8_9)
                        && playerData.getTicksSince(ActionType.LOGIN) > 20) {
                    KickUtil.kickPlayer(player, event, Settings.heldItemSlotInvalidSlotChange,
                            "Sent HeldItemSlot packet with invalid slot change"
                                    + " (slot=" + heldItemSlot + ")"
                                    + " (currentSlot=" + currentSlot + ")"
                    );
                    return;
                }

                if (heldItemSlot < 0 || heldItemSlot > 8) {
                    KickUtil.kickPlayer(player, event, Settings.heldItemSlotInvalidSlot,
                            "Sent HeldItemSlot packet with invalid slot"
                                    + " (slot=" + heldItemSlot + ")"
                    );
                    return;
                }

                playerData.setCurrentSlot(heldItemSlot);
                playerData.setTimestamp(ActionType.CHANGE_SLOT);
                break;

            case PacketType.Play.Client.SET_CREATIVE_SLOT:
                WrappedPacketInSetCreativeSlot setCreativeSlot = new WrappedPacketInSetCreativeSlot(nmsPacket);
                int creativeSlot = setCreativeSlot.getSlot();

                if (gameMode != GameMode.CREATIVE) {
                    KickUtil.kickPlayer(player, event, Settings.setCreativeSlotInvalidConditions,
                            "Sent SetCreativeSlot packet with invalid conditions"
                                    + " (gamemode=" + gameMode + ")"
                    );
                    return;
                }

                if ((creativeSlot < -1 || creativeSlot > 44) && creativeSlot != -999) {
                    KickUtil.kickPlayer(player, event, Settings.setCreativeSlotInvalidSlot,
                            "Sent SetCreativeSlot packet with invalid slot"
                                    + " (slot=" + creativeSlot + ")"
                    );
                    return;
                }
                break;

            case PacketType.Play.Client.SETTINGS:
                WrappedPacketInSettings settings = new WrappedPacketInSettings(nmsPacket);
                String locale = settings.getLocale();
                int localeLength = locale.length();
                int viewDistance = settings.getViewDistance();

                if (localeLength < 3 || localeLength > 8) {
                    KickUtil.kickPlayer(player, event, Settings.settingsInvalidLocale,
                            "Sent Settings packet with invalid locale"
                                    + " (locale=" + locale + ")"
                                    + " (length=" + localeLength + ")"
                    );
                    return;
                }

                if (viewDistance < 2 || viewDistance > 48) {
                    KickUtil.kickPlayer(player, event, Settings.settingsInvalidViewDistance,
                            "Sent Settings packet with invalid view distance"
                                    + " (viewDistance=" + viewDistance + ")"
                    );
                    return;
                }
                break;

            case PacketType.Play.Client.SPECTATE:
                WrappedPacketInSpectate spectate = new WrappedPacketInSpectate(nmsPacket);
                UUID spectateUUID = spectate.getUUID();
                Player spectatePlayer = Bukkit.getServer().getPlayer(spectateUUID);

                if (gameMode != GameMode.SPECTATOR
                        || spectatePlayer == null) {
                    KickUtil.kickPlayer(player, event, Settings.spectateInvalidConditions,
                            "Sent Spectate packet with invalid conditions"
                                    + " (gamemode=" + gameMode + ")"
                                    + " (player=" + spectatePlayer + ")"
                    );
                    return;
                }
                break;

            case PacketType.Play.Client.STEER_VEHICLE:
                WrappedPacketInSteerVehicle steerVehicle = new WrappedPacketInSteerVehicle(nmsPacket);
                float sidewaysValue = steerVehicle.getSideValue();
                float forwardValue = steerVehicle.getForwardValue();
                boolean jump = steerVehicle.isJump();

                // Ignores vehicle dismount packets.
                if (sidewaysValue == 0.0f && forwardValue == 0.0f && !jump && !insideVehicle) {
                    if (player.getNearbyEntities(3, 3, 3).stream().anyMatch(Vehicle.class::isInstance)) {
                        playerData.setTimestamp(ActionType.STEER_VEHICLE);
                        break;
                    }

                    KickUtil.kickPlayer(player, event, Settings.steerVehicleInvalidConditions,
                            "Sent SteerVehicle packet with invalid conditions"
                                    + " (sidewaysValue=" + 0.0f
                                    + " forwardValue=" + 0.0f
                                    + " jump=" + false
                                    + " insideVehicle=" + false + ")"
                    );
                    return;

                } else if (!insideVehicle) {
                    event.setCancelled(true);
                    MessageUtil.debug("SteerVehicle packet ignored for " + playerName + " (not inside vehicle)");
                    return;
                }

                // Checks for invalid sideways & forward values
                steerVehicleCheck(player, event, steerVehicle, sidewaysValue);
                steerVehicleCheck(player, event, steerVehicle, forwardValue);

                playerData.setTimestamp(ActionType.STEER_VEHICLE);
                break;

            case PacketType.Play.Client.TAB_COMPLETE:
                WrappedPacketInTabComplete tabComplete = new WrappedPacketInTabComplete(nmsPacket);
                String tabCompleteText = tabComplete.getText();

                if (tabCompleteText.isEmpty()) {
                    KickUtil.kickPlayer(player, event, Settings.tabCompleteInvalidMessage,
                            "Sent TabComplete packet with invalid message"
                                    + " (message=" + tabCompleteText + ")"
                    );
                    return;
                }
                break;

            case PacketType.Play.Client.TRANSACTION:
                WrappedPacketInTransaction transaction = new WrappedPacketInTransaction(nmsPacket);
                int transactionWindowId = transaction.getWindowId();
                short transactionActionNumber = transaction.getActionNumber();

                // If the client has sent a Transaction packet that was not accepted, kick them.
                if (!transaction.isAccepted()) {
                    KickUtil.kickPlayer(player, event, Settings.transactionNotAccepted,
                            "Sent Transaction packet that was not accepted"
                    );
                    return;
                }

                // If the client has sent a Transaction packet with an invalid window id, kick them.
                if (transactionWindowId != 0 && !inventoryOpen) {
                    KickUtil.kickPlayer(player, event, Settings.transactionInvalidWindowId,
                            "Sent Transaction packet with an invalid window ID"
                                    + " (windowId=" + transactionWindowId + ")"
                    );
                    return;
                }

                if (playerData.getTransactionTime().containsKey(transactionActionNumber)) {
                    long transactionStamp = playerData.getTransactionTime().get(transactionActionNumber);
                    playerData.setTransPing(System.currentTimeMillis() - transactionStamp);
                    playerData.getTransactionSentMap().remove(transactionActionNumber);
                }
                break;

            case PacketType.Play.Client.UPDATE_SIGN:
                WrappedPacketInUpdateSign updateSign = new WrappedPacketInUpdateSign(nmsPacket);

                for (String line : updateSign.getTextLines()) {
                    int length = line.length();

                    if (length > 45) {
                        KickUtil.kickPlayer(player, event, Settings.updateSignInvalidData,
                                "Sent UpdateSign packet with invalid data"
                                        + " (length=" + length + ")"
                        );
                        return;
                    }
                }
                break;

            case PacketType.Play.Client.USE_ENTITY:
                WrappedPacketInUseEntity useEntity = new WrappedPacketInUseEntity(nmsPacket);
                WrappedPacketInUseEntity.EntityUseAction useEntityAction = useEntity.getAction();
                Entity entity = useEntity.getEntity();
                int entityId = useEntity.getEntityId();

                if (entity == null
                        || entity.isDead()
                        || entity.getWorld() != world) {
                    event.setCancelled(true);
                    return;
                }

                if (Settings.useEntityInvalidDistance) {
                    Location entityLoc = entity.getLocation();
                    double distance = entityLoc.distance(playerLoc);

                    if (distance > 7.03) {
                        event.setCancelled(true);
                        return;
                    }
                }

                boolean entityIsPlayer = entity.equals(player);

                if (placingBlock
                        || entityIsPlayer
                        || entityId < 0) {
                    KickUtil.kickPlayer(player, event, Settings.useEntityInvalidConditions,
                            "Sent UseEntity packet with invalid conditions"
                                    + " (placingBlock=" + placingBlock
                                    + " entityEqualsPlayer=" + entityIsPlayer
                                    + " entityId=" + entityId + ")"
                    );
                    return;
                }

                if (useEntityAction != null) {
                    switch (useEntityAction) {
                        case INTERACT:
                        case INTERACT_AT:
                            playerData.setTimestamp(ActionType.ENTITY_INTERACT);
                            break;

                        default:
                            break;
                    }
                }
                break;

            case PacketType.Play.Client.WINDOW_CLICK:
                WrappedPacketInWindowClick windowClick = new WrappedPacketInWindowClick(nmsPacket);
                int windowId = windowClick.getWindowId();
                int windowSlot = windowClick.getWindowSlot();
                int windowMode = windowClick.getMode();
                int windowButton = windowClick.getWindowButton();

                if (windowId == 0) {
                    int diff = windowSlot - (openInventory.countSlots() - 1);

                    if (windowSlot > 44 || diff > 4
                            || (windowSlot != -999 && windowSlot < -1)) {
                        KickUtil.kickPlayer(player, event, Settings.windowClickInvalidSlot,
                                "Sent WindowClick packet with invalid slot"
                                        + " (slot=" + windowSlot
                                        + " diff=" + diff + ")"
                        );
                        return;
                    }
                }

                switch (windowMode) {
                    case 0:
                        if (windowButton != 0 && windowButton != 1) {
                            KickUtil.kickPlayer(player, event, Settings.windowClickInvalidPickupButton,
                                    "Sent WindowClick packet invalid Pickup button"
                                            + " (button=" + windowButton + ")"
                            );
                            return;
                        }
                        break;

                    case 1:
                        if (windowButton != 0 && windowButton != 1
                                && windowButton != 2 && windowButton != 5) {
                            KickUtil.kickPlayer(player, event, Settings.windowClickInvalidQuickMoveButton,
                                    "Sent WindowClick packet with invalid QuickMove button"
                                            + " (button=" + windowButton + ")"
                            );
                            return;
                        }
                        break;

                    case 2:
                        if (windowButton != 0 && windowButton != 1
                                && windowButton != 2 && windowButton != 3
                                && windowButton != 4 && windowButton != 7
                                && windowButton != 8 && windowButton != 40) {
                            KickUtil.kickPlayer(player, event, Settings.windowClickInvalidSwapButton,
                                    "Sent WindowClick packet with invalid Swap button"
                                            + " (button=" + windowButton + ")"
                            );
                            return;
                        }
                        break;

                    case 3:
                        if (windowButton != 0 && windowButton != 1
                                && windowButton != 2 && windowButton != 4
                                && windowButton != 5) {
                            KickUtil.kickPlayer(player, event, Settings.windowClickInvalidCloneButton,
                                    "Sent WindowClick packet with invalid Clone button"
                                            + " (button=" + windowButton + ")"
                            );
                            return;
                        }
                        break;

                    case 4:
                        if (windowButton != 0 && windowButton != 1
                                && windowButton != 2 && windowButton != 4
                                && windowButton != 5 && windowButton != 6) {
                            KickUtil.kickPlayer(player, event, Settings.windowClickInvalidThrowButton,
                                    "Sent WindowClick packet with invalid Throw button"
                                            + " (button=" + windowButton + ")"
                            );
                            return;
                        }
                        break;

                    case 5:
                        if (windowButton != 0 && windowButton != 1
                                && windowButton != 2 && windowButton != 4
                                && windowButton != 5 && windowButton != 6
                                && windowButton != 8 && windowButton != 9
                                && windowButton != 10) {
                            KickUtil.kickPlayer(player, event, Settings.windowClickInvalidQuickCraftButton,
                                    "Sent WindowClick packet with invalid QuickCraft button"
                                            + " (button=" + windowButton + ")"
                            );
                            return;
                        }
                        break;

                    case 6:
                        if (windowButton != 0 && windowButton != 1
                                && windowButton != 2 && windowButton != 3
                                && windowButton != 4 && windowButton != 5
                                && windowButton != 6) {
                            KickUtil.kickPlayer(player, event, Settings.windowClickInvalidPickupAllButton,
                                    "Sent WindowClick packet with invalid PickupAll button"
                                            + " (button=" + windowButton + ")"
                            );
                            return;
                        }
                        break;

                    default:
                        break;
                }

                playerData.setTimestamp(ActionType.WINDOW_CLICK);
                break;

            case PacketType.Play.Client.ENCHANT_ITEM:
            case PacketType.Play.Client.KEEP_ALIVE:
            case PacketType.Play.Client.RESOURCE_PACK_STATUS: // These packets are ignored here
                break;

            default:
                MessageUtil.debug("Unhandled packet: " + packetId);
                break;
        }

        // Handles packet checks.
        handlePacketChecks(playerData, event);
    }

    /**
     * Handle the checks for the given packet event.
     *
     * @param playerData The player data.
     * @param event      The packet event.
     */
    private static void handlePacketChecks(@NotNull PlayerData playerData,
                                           @NotNull CancellableNMSPacketEvent event) {
        long timestamp = System.currentTimeMillis();
        NMSPacket nmsPacket = event.getNMSPacket();
        List<Check> checks = playerData.getChecks();

        // Create a copy of the checks list to avoid ConcurrentModificationException
        if (checks != null) {
            Iterable<Check> checksCopy = new ArrayList<>(checks);

            for (Check check : checksCopy) {
                if (check.getCheckInfo().enabled()) {
                    byte packetId = event.getPacketId();
                    Object rawNMSPacket = nmsPacket.getRawNMSPacket();

                    check.handle(event, packetId, nmsPacket, rawNMSPacket, timestamp);
                }
            }
        }
    }

    /**
     * Handle the checks for the given rotation event.
     *
     * @param playerData The player data.
     * @param event      The rotation event.
     */
    private static void handleRotationChecks(@NotNull PlayerData playerData, RotationEvent event) {
        long timestamp = System.currentTimeMillis();
        List<Check> checks = playerData.getChecks();

        // Create a copy of the checks list to avoid ConcurrentModificationException
        if (checks != null) {
            Iterable<Check> checksCopy = new ArrayList<>(checks);

            for (Check check : checksCopy) {
                if (check.getCheckInfo().enabled()) {
                    check.handle(event, timestamp);
                }
            }
        }
    }

    /**
     * Handle the checks for the given movement event.
     *
     * @param playerData The player data.
     * @param event      The movement event.
     */
    private static void handleMovementChecks(@NotNull PlayerData playerData, MovementEvent event) {
        long timestamp = System.currentTimeMillis();
        List<Check> checks = playerData.getChecks();

        // Create a copy of the checks list to avoid ConcurrentModificationException
        if (checks != null) {
            Iterable<Check> checksCopy = new ArrayList<>(checks);

            for (Check check : checksCopy) {
                if (check.getCheckInfo().enabled()) {
                    check.handle(event, timestamp);
                }
            }
        }
    }

    /**
     * Checks for valid SteerVehicle packets.
     *
     * @param player       The player.
     * @param event        The packet event.
     * @param steerVehicle The SteerVehicle packet.
     * @param value        The value to check.
     */
    private static void steerVehicleCheck(Player player, CancellableEvent event,
                                          WrappedPacketInSteerVehicle steerVehicle, float value) {
        if (Math.abs(value) == 0.98f) {
            if (steerVehicle.isDismount()) {
                KickUtil.kickPlayer(player, event, Settings.steerVehicleInvalidDismountValue,
                        "Sent SteerVehicle packet with invalid dismount value"
                );
            }
        } else if (Math.abs(value) == 0.29400003f) {
            if (!steerVehicle.isDismount()) {
                KickUtil.kickPlayer(player, event, Settings.steerVehicleInvalidNonDismountValue,
                        "Sent SteerVehicle packet with invalid non-dismount value"
                );
            }
        } else if (value != 0.0f) {
            KickUtil.kickPlayer(player, event, Settings.steerVehicleInvalidValue,
                    "Sent SteerVehicle packet with invalid value"
            );
        }
    }
}
