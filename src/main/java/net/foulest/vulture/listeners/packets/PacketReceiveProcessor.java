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

import com.github.retrooper.packetevents.event.CancellableEvent;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.ProtocolPacketEvent;
import com.github.retrooper.packetevents.event.SimplePacketListenerAbstract;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.protocol.ConnectionState;
import com.github.retrooper.packetevents.protocol.PacketSide;
import com.github.retrooper.packetevents.protocol.chat.LastSeenMessages;
import com.github.retrooper.packetevents.protocol.chat.MessageSignature;
import com.github.retrooper.packetevents.protocol.chat.RemoteChatSession;
import com.github.retrooper.packetevents.protocol.chat.SignedCommandArgument;
import com.github.retrooper.packetevents.protocol.item.book.BookType;
import com.github.retrooper.packetevents.protocol.item.type.ItemType;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.protocol.player.*;
import com.github.retrooper.packetevents.protocol.recipe.RecipeDisplayId;
import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.github.retrooper.packetevents.protocol.world.JointType;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3f;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.util.crypto.MessageSignData;
import com.github.retrooper.packetevents.util.crypto.SaltSignature;
import com.github.retrooper.packetevents.wrapper.common.client.WrapperCommonClientSettings;
import com.github.retrooper.packetevents.wrapper.play.client.*;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDebugSample;
import net.foulest.vulture.action.ActionType;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.data.PlayerDataManager;
import net.foulest.vulture.event.MovementEvent;
import net.foulest.vulture.event.RotationEvent;
import net.foulest.vulture.util.*;
import net.foulest.vulture.util.data.CustomLocation;
import net.foulest.vulture.util.data.Vector2f;
import org.bukkit.GameMode;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.*;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.time.Instant;
import java.util.*;

/**
 * Handles all incoming packets sent to the server.
 *
 * @author Foulest
 */
@SuppressWarnings("deprecation")
public class PacketReceiveProcessor extends SimplePacketListenerAbstract {

    private final Set<Character> disallowedItemNameChars = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList('\b', '\t', '\n', '\u0000', '\u0001', '\u0002',
                    '\u0003', '\u0004', '\u0005', '\u0006', '\u0007', '\u000B', '\f', '\r',
                    '\u000E', '\u000F', '\u0010', '\u0011', '\u0012', '\u0013', '\u0014',
                    '\u0015', '\u0016', '\u0017', '\u0018', '\u0019', '\u001A', '\u001B',
                    '\u001C', '\u001D', '\u001E'))
    );

    public PacketReceiveProcessor() {
        super(PacketListenerPriority.HIGH);
    }

    /**
     * Handles incoming packets.
     *
     * @param event PacketPlayReceiveEvent
     */
    @Override
    public void onPacketPlayReceive(@NotNull PacketPlayReceiveEvent event) {
        PacketTypeCommon packetType = event.getPacketType();
        String packetName = packetType.getName();
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

        PlayerData playerData = PlayerDataManager.getPlayerData(player);
        @NotNull ClientVersion version = playerData.getVersion();

        int packetId = event.getPacketId();
        ConnectionState connectionState = event.getConnectionState();

        // Ignores invalid incoming packets.
        if (PacketType.getById(PacketSide.CLIENT, connectionState, version, packetId) == null) {
            KickUtil.kickPlayer(player, event, Settings.invalidPacketSentToServer,
                    "Invalid packet sent to server (name=" + packetName + ")"
            );
            return;
        }

        String playerName = player.getName();
        World world = player.getWorld();
        InventoryView openInventory = player.getOpenInventory();
        Entity vehicle = player.getVehicle();
        GameMode gameMode = player.getGameMode();
        Location playerLoc = player.getLocation();

        boolean insideVehicle = player.isInsideVehicle();
        boolean inventoryOpen = playerData.isInventoryOpen();
        boolean digging = playerData.isDigging();
        boolean placingBlock = playerData.isPlacingBlock();

        // Handles PLAYER_ABILITIES packets.
        if (packetType == PacketType.Play.Client.PLAYER_ABILITIES) {
            @NotNull WrapperPlayClientPlayerAbilities packet = new WrapperPlayClientPlayerAbilities(event);
            boolean flying = packet.isFlying();

            Optional<Boolean> godMode = packet.isInGodMode();
            Optional<Boolean> flightAllowed = packet.isFlightAllowed();
            Optional<Boolean> creativeMode = packet.isInCreativeMode();

            @NotNull String godModeValue = godMode.map(Object::toString).orElse("null");
            @NotNull String flightAllowedValue = flightAllowed.map(Object::toString).orElse("null");
            @NotNull String creativeModeValue = creativeMode.map(Object::toString).orElse("null");

            // Prints debug information.
            MessageUtil.debug(packetName
                    + " flying=" + flying
                    + " godMode=" + godModeValue
                    + " flightAllowed=" + flightAllowedValue
                    + " creativeMode=" + creativeModeValue
            );

            if (playerData.getVersion().isOlderThanOrEquals(ClientVersion.V_1_8)) {
                boolean playerFlightAllowed = player.getAllowFlight();
                boolean playerFlying = player.isFlying();

                if (flying == playerFlying) {
                    KickUtil.kickPlayer(player, event, Settings.abilitiesDuplicateFlying,
                            "Sent " + packetName + " with duplicate flying values"
                                    + " (flying=" + flying
                                    + " playerFlying=" + playerFlying + ")"
                    );
                    return;
                }

                if (flying && !playerFlightAllowed) {
                    KickUtil.kickPlayer(player, event, Settings.abilitiesInvalidFlying,
                            "Sent " + packetName + " with invalid flying values"
                                    + " (flying=" + true
                                    + " playerFlightAllowed=" + false + ")"
                    );
                    return;
                }

                flightAllowed.ifPresent(value -> {
                    if (Boolean.TRUE.equals(value) != playerFlightAllowed) {
                        KickUtil.kickPlayer(player, event, Settings.abilitiesInvalidFlightAllowed,
                                "Sent " + packetName + " with invalid flight allowed values"
                                        + " (flightAllowed=" + value
                                        + " playerFlightAllowed=" + playerFlightAllowed + ")"
                        );
                    }

                    playerData.setFlightAllowed(value);
                });

                creativeMode.ifPresent(value -> {
                    boolean inCreativeMode = playerData.isCreativeMode();

                    if (Boolean.TRUE.equals(value) != inCreativeMode) {
                        KickUtil.kickPlayer(player, event, Settings.abilitiesInvalidCreativeMode,
                                "Sent " + packetName + " with invalid creative mode values"
                                        + " (clientCreativeMode=" + value
                                        + " serverCreativeMode=" + inCreativeMode + ")"
                        );
                    }

                    playerData.setCreativeMode(value);
                });

                godMode.ifPresent(value -> {
                    boolean inGodMode = playerData.isGodMode();

                    if (Boolean.TRUE.equals(value) != inGodMode) {
                        KickUtil.kickPlayer(player, event, Settings.abilitiesInvalidGodMode,
                                "Sent " + packetName + " with invalid god mode values"
                                        + " (clientGodMode=" + value
                                        + " serverGodMode=" + inGodMode + ")"
                        );
                    }

                    playerData.setGodMode(value);
                });
            }

            if (flying) {
                playerData.setFlying(true);
                playerData.setTimestamp(ActionType.START_FLYING);
            } else {
                playerData.setFlying(false);
                playerData.setTimestamp(ActionType.STOP_FLYING);
            }

            handlePacketChecks(playerData, event);
            return;
        }

        // Handles ANIMATION packets.
        if (packetType == PacketType.Play.Client.ANIMATION) {
            playerData.setTimestamp(ActionType.ANIMATION);
            handlePacketChecks(playerData, event);
            return;
        }

        // Handles PLAYER_DIGGING packets.
        if (packetType == PacketType.Play.Client.PLAYER_DIGGING) {
            @NotNull WrapperPlayClientPlayerDigging packet = new WrapperPlayClientPlayerDigging(event);
            @Nullable Vector3i blockPosition = packet.getBlockPosition();

            // Checks for players sending null block positions.
            if (blockPosition == null) {
                KickUtil.kickPlayer(player, event, Settings.diggingInvalidBlockPosition,
                        "Sent " + packetName + " with invalid block position"
                                + " (blockPosition=" + null + ")"
                );
                return;
            }

            int blockX = blockPosition.getX();
            int blockY = blockPosition.getY();
            int blockZ = blockPosition.getZ();

            DiggingAction action = packet.getAction();
            BlockFace blockFace = packet.getBlockFace();
            int sequence = packet.getSequence();

            // Prints debug information.
            MessageUtil.debug(packetName
                    + " action=" + action
                    + " blockFace=" + blockFace
                    + " sequence=" + sequence
                    + " blockX=" + blockX
                    + " blockY=" + blockY
                    + " blockZ=" + blockZ
            );

            if (blockX != 0 && blockY != 0 && blockZ != 0 && blockY != 4095
                    && blockX != -1 && blockY != -1 && blockZ != -1) {
                @NotNull Location blockLocation = new Location(world, blockX, blockY, blockZ);
                double distance = playerLoc.distance(blockLocation);

                // Checks for packets with an invalid distance.
                if (distance > 7.03) {
                    KickUtil.kickPlayer(player, event, Settings.diggingInvalidDistance,
                            "Sent " + packetName + " with invalid distance"
                                    + " (distance=" + distance + ")"
                    );
                    return;
                }
            }

            switch (action) {
                case START_DIGGING:
                    // Ignores players in creative mode.
                    if (gameMode == GameMode.CREATIVE) {
                        break;
                    }

                    // Checks the block being dug.
                    TaskUtil.runTask(() -> {
                        Block block = world.getBlockAt(blockX, blockY, blockZ);
                        Material material = block.getType();

                        // Checks for and ignores instantly breakable blocks.
                        switch (material) {
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
                                playerData.setDigging(false);
                                break;

                            case WATER:
                            case STATIONARY_WATER:
                            case LAVA:
                            case STATIONARY_LAVA:
                            case FIRE:
                                KickUtil.kickPlayer(player, event, Settings.diggingInvalidBlockType,
                                        "Tried to dig invalid block"
                                                + " (type=" + material
                                                + " blockPosition=" + blockX + ", " + blockY + ", " + blockZ + ")"
                                );
                                break;

                            default:
                                playerData.setDigging(true);
                                break;
                        }
                    });
                    break;

                case FINISHED_DIGGING:
                case CANCELLED_DIGGING:
                    playerData.setTimestamp(ActionType.BLOCK_BREAK);
                    playerData.setDigging(false);
                    break;

                case RELEASE_USE_ITEM:
                    // Checks for ReleaseUseItem packets with invalid data.
                    if (!(blockFace == BlockFace.DOWN && blockX == 0 && blockY == 0 && blockZ == 0)) {
                        KickUtil.kickPlayer(player, event, Settings.releaseUseItemInvalidData,
                                "Sent RELEASE_USE_ITEM with invalid data"
                                        + " (blockFace=" + blockFace
                                        + " x=" + blockX
                                        + " y=" + blockY
                                        + " z=" + blockZ + ")"
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
                case DROP_ITEM_STACK:
                    // Checks for DropItem packets with invalid data.
                    if (!(blockFace == BlockFace.DOWN && blockX == 0 && blockY == 0 && blockZ == 0)) {
                        KickUtil.kickPlayer(player, event, Settings.dropItemInvalidData,
                                "Sent DROP_ITEM/STACK with invalid data"
                                        + " (blockFace=" + blockFace
                                        + " x=" + blockX
                                        + " y=" + blockY
                                        + " z=" + blockZ + ")"
                        );
                        return;
                    }
                    break;

                case SWAP_ITEM_WITH_OFFHAND:
                    MessageUtil.debug("SWAP_ITEM_WITH_OFFHAND");
                    break;

                default:
                    break;
            }

            handlePacketChecks(playerData, event);
            return;
        }

        // Handles PLAYER_BLOCK_PLACEMENT packets.
        if (packetType == PacketType.Play.Client.PLAYER_BLOCK_PLACEMENT) {
            @NotNull WrapperPlayClientPlayerBlockPlacement packet = new WrapperPlayClientPlayerBlockPlacement(event);
            @Nullable Vector3i blockPosition = packet.getBlockPosition();

            // Checks for packets with null block positions.
            if (blockPosition == null) {
                KickUtil.kickPlayer(player, event, Settings.blockPlacementInvalidBlockPosition,
                        "Sent " + packetName + " with invalid block position"
                                + " (blockPosition=" + null + ")"
                );
                return;
            }

            BlockFace face = packet.getFace();
            int blockX = blockPosition.getX();
            int blockY = blockPosition.getY();
            int blockZ = blockPosition.getZ();

            Vector3f cursorPosition = packet.getCursorPosition();
            float cursorX = cursorPosition.getX();
            float cursorY = cursorPosition.getY();
            float cursorZ = cursorPosition.getZ();

            Optional<com.github.retrooper.packetevents.protocol.item.ItemStack> itemStack = packet.getItemStack();
            String itemStackValue = itemStack.isPresent() ? itemStack.get().toString() : "null";

            // 1.9+ values; currently unused.
            Optional<Boolean> insideBlock = packet.getInsideBlock();
            Optional<Boolean> worldBorderHit = packet.getWorldBorderHit();
            @NotNull String insideBlockValue = insideBlock.map(Object::toString).orElse("null");
            @NotNull String worldBorderHitValue = worldBorderHit.map(Object::toString).orElse("null");
            int sequence = packet.getSequence();
            InteractionHand hand = packet.getHand();

            float yaw = playerLoc.getYaw();
            float pitch = playerLoc.getPitch();

            // Normalizes the yaw value.
            yaw = yaw % 360;
            yaw = (yaw + 360) % 360;
            if (yaw > 180) {
                yaw -= 360;
            }

            Location eyeLocation = player.getEyeLocation();

            // Prints debug information.
            MessageUtil.debug(packetName
                    + " yaw=" + yaw
                    + " pitch=" + pitch
                    + " blockFace=" + face
                    + " eyePos=" + eyeLocation
                    + " cursorPos=(" + cursorX + ", " + cursorY + ", " + cursorZ + ")"
                    + " blockPos=(" + blockX + ", " + blockY + ", " + blockZ + ")"
                    + " itemStack=" + itemStackValue
                    + " insideBlock=" + insideBlockValue
                    + " worldBorderHit=" + worldBorderHitValue
                    + " sequence=" + sequence
                    + " hand=" + hand
            );

            // Checks for packets with an invalid block placeement distance.
            if (blockX != -1 && blockY != -1 && blockZ != -1) {
                @NotNull Location blockLocation = new Location(world, blockX, blockY, blockZ);
                double distance = playerLoc.distance(blockLocation);

                if (distance > 7.03) {
                    KickUtil.kickPlayer(player, event, Settings.blockPlacementInvalidDistance,
                            "Sent " + packetName + " with invalid distance"
                                    + " (distance=" + distance + ")"
                    );
                    return;
                }
            }

            // Checks for players sending out-of-bounds cursor positions.
            if (cursorX < 0.0 || cursorX > 1.0
                    || cursorY < 0.0 || cursorY > 1.0
                    || cursorZ < 0.0 || cursorZ > 1.0) {
                KickUtil.kickPlayer(player, event, Settings.blockPlacementInvalidOtherCursorPosition,
                        "Sent " + packetName + " with invalid cursor position"
                                + " (x=" + cursorX
                                + " y=" + cursorY
                                + " z=" + cursorZ + ")"
                );
                return;
            }

            switch (face) {
                case UP:
                    // Checks for invalid UP cursor bounds.
                    if (cursorX > 0.9375 || cursorZ > 0.9375) {
                        KickUtil.kickPlayer(player, event, Settings.blockPlacementInvalidUpCursorBounds,
                                "Sent " + packetName + " with invalid UP cursor bounds"
                                        + " (x=" + cursorX + ")"
                                        + " (z=" + cursorZ + ")"
                        );
                        return;
                    }

                    // Checks for invalid UP cursor positions.
                    // The cursorY value can either be 1.0 or a multiple of 0.015625, excluding 0.0.
                    if (cursorY != 1.0 && (cursorY % 0.015625 != 0 || cursorY == 0.0)) {
                        KickUtil.kickPlayer(player, event, Settings.blockPlacementInvalidUpCursorPosition,
                                "Sent " + packetName + " with invalid UP cursor position"
                                        + " (y=" + cursorY + ")"
                        );
                        return;
                    }

                    // Checks for invalid UP pitches.
                    if (pitch < 0.0) {
                        KickUtil.kickPlayer(player, event, Settings.blockPlacementInvalidUpPitch,
                                "Sent " + packetName + " with invalid UP pitch"
                                        + " (pitch=" + pitch + ")"
                        );
                        return;
                    }
                    break;

                case DOWN:
                    // Checks for invalid DOWN cursor bounds.
                    if (cursorX > 0.9375 || cursorZ > 0.9375) {
                        KickUtil.kickPlayer(player, event, Settings.blockPlacementInvalidDownCursorBounds,
                                "Sent " + packetName + " with invalid DOWN cursor bounds"
                                        + " (x=" + cursorX + ")"
                                        + " (z=" + cursorZ + ")"
                        );
                        return;
                    }

                    // Checks for invalid DOWN cursor positions.
                    // The cursorY value can either be 0.0 or a multiple of 0.015625, excluding 1.0.
                    if (cursorY != 0.0 && (cursorY % 0.015625 != 0 || cursorY == 1.0)) {
                        KickUtil.kickPlayer(player, event, Settings.blockPlacementInvalidDownCursorPosition,
                                "Sent " + packetName + " with invalid DOWN cursor position"
                                        + " (y=" + cursorY + ")"
                        );
                        return;
                    }

                    // Checks for invalid DOWN pitches.
                    if (pitch > 0.0) {
                        KickUtil.kickPlayer(player, event, Settings.blockPlacementInvalidDownPitch,
                                "Sent " + packetName + " with invalid DOWN pitch"
                                        + " (pitch=" + pitch + ")"
                        );
                        return;
                    }
                    break;

                case EAST:
                    // Checks for invalid EAST cursor bounds.
                    if (cursorY > 0.9375 || cursorZ > 0.9375) {
                        KickUtil.kickPlayer(player, event, Settings.blockPlacementInvalidEastCursorBounds,
                                "Sent " + packetName + " with invalid EAST cursor bounds"
                                        + " (y=" + cursorY + ")"
                                        + " (z=" + cursorZ + ")"
                        );
                        return;
                    }

                    // Checks for invalid EAST cursor positions.
                    // The cursorX value can either be 1.0 or a multiple of 0.015625, excluding 0.0.
                    if (cursorX != 1.0 && (cursorX % 0.015625 != 0 || cursorX == 0.0)) {
                        KickUtil.kickPlayer(player, event, Settings.blockPlacementInvalidEastCursorPosition,
                                "Sent " + packetName + " with invalid EAST cursor position"
                                        + " (x=" + cursorX + ")"
                        );
                        return;
                    }

                    // Checks for invalid EAST yaw values.
                    // Valid yaw range: 0.0 to 180.0
                    if (yaw < 0.0) {
                        KickUtil.kickPlayer(player, event, Settings.blockPlacementInvalidEastYaw,
                                "Sent " + packetName + " with invalid EAST yaw"
                                        + " (yaw=" + yaw + ")"
                        );
                        return;
                    }
                    break;

                case WEST:
                    // Checks for invalid WEST cursor bounds.
                    if (cursorY > 0.9375 || cursorZ > 0.9375) {
                        KickUtil.kickPlayer(player, event, Settings.blockPlacementInvalidWestCursorBounds,
                                "Sent " + packetName + " with invalid WEST cursor bounds"
                                        + " (y=" + cursorY + ")"
                                        + " (z=" + cursorZ + ")"
                        );
                        return;
                    }

                    // Checks for invalid WEST cursor positions.
                    // The cursorX value can either be 0.0 or a multiple of 0.015625, excluding 1.0.
                    if (cursorX != 0.0 && (cursorX % 0.015625 != 0 || cursorX == 1.0)) {
                        KickUtil.kickPlayer(player, event, Settings.blockPlacementInvalidWestCursorPosition,
                                "Sent " + packetName + " with invalid WEST cursor position"
                                        + " (x=" + cursorX + ")"
                        );
                        return;
                    }

                    // Checks for invalid WEST yaw values.
                    // Valid yaw range: 0.0 to -180.0
                    if (yaw > 0.0) {
                        KickUtil.kickPlayer(player, event, Settings.blockPlacementInvalidWestYaw,
                                "Sent " + packetName + " with invalid WEST yaw"
                                        + " (yaw=" + yaw + ")"
                        );
                        return;
                    }
                    break;

                case NORTH:
                    // Checks for invalid NORTH cursor bounds.
                    if (cursorX > 0.9375 || cursorY > 0.9375) {
                        KickUtil.kickPlayer(player, event, Settings.blockPlacementInvalidNorthCursorBounds,
                                "Sent " + packetName + " with invalid NORTH cursor bounds"
                                        + " (x=" + cursorX + ")"
                                        + " (y=" + cursorY + ")"
                        );
                        return;
                    }

                    // Checks for invalid NORTH cursor positions.
                    // The cursorZ value can either be 0.0 or a multiple of 0.015625, excluding 1.0.
                    if (cursorZ != 0.0 && (cursorZ % 0.015625 != 0 || cursorZ == 1.0)) {
                        KickUtil.kickPlayer(player, event, Settings.blockPlacementInvalidNorthCursorPosition,
                                "Sent " + packetName + " with invalid NORTH cursor position"
                                        + " (z=" + cursorZ + ")"
                        );
                        return;
                    }

                    // Checks for invalid NORTH yaw values.
                    // Valid yaw range: -90.0 to 90.0
                    if (Math.abs(yaw) > 90.0) {
                        KickUtil.kickPlayer(player, event, Settings.blockPlacementInvalidNorthYaw,
                                "Sent " + packetName + " with invalid NORTH yaw"
                                        + " (yaw=" + yaw + ")"
                        );
                        return;
                    }
                    break;

                case SOUTH:
                    // Checks for invalid SOUTH cursor bounds.
                    if (cursorX > 0.9375 || cursorY > 0.9375) {
                        KickUtil.kickPlayer(player, event, Settings.blockPlacementInvalidSouthCursorBounds,
                                "Sent " + packetName + " with invalid SOUTH cursor bounds"
                                        + " (x=" + cursorX + ")"
                                        + " (y=" + cursorY + ")"
                        );
                        return;
                    }

                    // Checks for invalid SOUTH cursor positions.
                    // The cursorZ value can either be 1.0 or a multiple of 0.015625, excluding 0.0.
                    if (cursorZ != 1.0 && (cursorZ % 0.015625 != 0 || cursorZ == 0.0)) {
                        KickUtil.kickPlayer(player, event, Settings.blockPlacementInvalidSouthCursorPosition,
                                "Sent " + packetName + " with invalid SOUTH cursor position"
                                        + " (z=" + cursorZ + ")"
                        );
                        return;
                    }

                    // Checks for invalid SOUTH yaw values.
                    // Valid yaw range: -90.0 to -180.0, then immediately 180.0 to 90.0,
                    // since any value over -180 snaps to 180, and any value over 90.0 snaps to -90.0.
                    if ((yaw < 0 && yaw > -90.0) || (yaw > 0 && yaw < 90.0)) {
                        KickUtil.kickPlayer(player, event, Settings.blockPlacementInvalidSouthYaw,
                                "Sent " + packetName + " with invalid SOUTH yaw"
                                        + " (yaw=" + yaw + ")"
                        );
                        return;
                    }
                    break;

                case OTHER:
                    // Checks for invalid OTHER cursor positions.
                    if (cursorX != 0.0 || cursorY != 0.0 || cursorZ != 0.0) {
                        KickUtil.kickPlayer(player, event, Settings.blockPlacementInvalidOtherCursorPosition,
                                "Sent " + packetName + " with invalid OTHER cursor position"
                                        + " (x=" + cursorX
                                        + " y=" + cursorY
                                        + " z=" + cursorZ + ")"
                        );
                        return;
                    }

                    // Checks for invalid OTHER block positions.
                    // The only valid blockX and blockZ value is -1.
                    // blockY can be either -1 or 4095.
                    if (blockX != -1 || (blockY != -1 && blockY != 4095) || blockZ != -1) {
                        KickUtil.kickPlayer(player, event, Settings.blockPlacementInvalidOtherBlockPosition,
                                "Sent " + packetName + " with invalid OTHER block position"
                                        + " (x=" + blockX
                                        + " y=" + blockY
                                        + " z=" + blockZ + ")"
                        );
                        return;
                    }
                    break;
            }

            // Handles item placement.
            if (packet.getItemStack().isPresent()) {
                com.github.retrooper.packetevents.protocol.item.@NotNull ItemStack itemStackGet = packet.getItemStack().get();
                ItemType material = itemStackGet.getType();

                if (packet.getFace() == BlockFace.OTHER) {
                    // Sets the eating state.
                    if (material.hasAttribute(ItemTypes.ItemAttribute.EDIBLE)) {
                        if (player.getFoodLevel() < 20 || material == ItemTypes.GOLDEN_APPLE) {
                            playerData.setEating(true);
                        }
                    }

                    // Sets the drinking state.
                    if (material.equals(ItemTypes.POTION)) {
                        playerData.setDrinking(true);
                    }

                    // Sets the blocking state.
                    if (material.hasAttribute(ItemTypes.ItemAttribute.SWORD)) {
                        playerData.setBlocking(true);
                    }

                    // Sets the bow shooting state.
                    if (material.equals(ItemTypes.BOW)) {
                        playerData.setShootingBow(true);
                    }

                } else if (material.getPlacedType() != null && material != ItemTypes.AIR) {
                    // Sets the placing block state.
                    playerData.setPlacingBlock(true);
                    playerData.setTimestamp(ActionType.PLACING_BLOCK);
                }
            }

            handlePacketChecks(playerData, event);
            return;
        }

        // Handles CHAT_MESSAGE packets.
        if (packetType == PacketType.Play.Client.CHAT_MESSAGE) {
            @NotNull WrapperPlayClientChatMessage packet = new WrapperPlayClientChatMessage(event);
            String message = packet.getMessage();

            Optional<MessageSignData> messageSignData = packet.getMessageSignData();
            LastSeenMessages.@Nullable Update update = packet.getLastSeenMessages();
            LastSeenMessages.@Nullable LegacyUpdate legacyUpdate = packet.getLegacyLastSeenMessages();

            // Prints debug information.
            @NotNull StringBuilder debugMessage = new StringBuilder(packetName);
            debugMessage.append(" message=").append(message);

            if (messageSignData.isPresent()) {
                @NotNull MessageSignData signData = messageSignData.get();
                SaltSignature saltSignature = signData.getSaltSignature();
                long salt = saltSignature.getSalt();
                byte[] signature = saltSignature.getSignature();
                Instant timestamp = signData.getTimestamp();
                boolean signedPreview = signData.isSignedPreview();

                debugMessage.append(" salt=").append(salt);
                debugMessage.append(" signature=").append(Arrays.toString(signature));
                debugMessage.append(" timestamp=").append(timestamp);
                debugMessage.append(" signedPreview=").append(signedPreview);
            }

            if (update != null) {
                int offset = update.getOffset();
                BitSet acknowledged = update.getAcknowledged();

                debugMessage.append(" offset=").append(offset);
                debugMessage.append(" acknowledged=").append(acknowledged);
            }

            if (legacyUpdate != null) {
                LastSeenMessages lastSeenMessages = legacyUpdate.getLastSeenMessages();
                List<LastSeenMessages.Entry> entries = lastSeenMessages.getEntries();
                @Nullable LastSeenMessages.Entry lastReceived = legacyUpdate.getLastReceived();

                debugMessage.append(" lastSeenMessages=").append(entries);

                if (lastReceived != null) {
                    UUID uuid = lastReceived.getUUID();
                    byte[] lastVerifier = lastReceived.getLastVerifier();

                    debugMessage.append(" uuid=").append(uuid);
                    debugMessage.append(" lastVerifier=").append(Arrays.toString(lastVerifier));
                }
            }

            MessageUtil.debug(String.valueOf(debugMessage));

            // Checks for packets with invalid conditions.
            if (inventoryOpen || digging || placingBlock) {

                // Ignores WorldEdit CUI.
                if (!message.equals("/we cui") && !message.equals("/worldedit cui")) {
                    KickUtil.kickPlayer(player, event, Settings.chatMessageInvalidConditions,
                            "Sent " + packetName + " with invalid conditions"
                                    + " (inventoryOpen=" + inventoryOpen
                                    + " digging=" + digging
                                    + " placingBlock=" + placingBlock
                                    + " message=" + message + ")"
                    );
                    return;
                }
            }

            // Checks for packets with empty messages.
            if (message.isEmpty()) {
                KickUtil.kickPlayer(player, event, Settings.chatMessageInvalidMessage,
                        "Sent " + packetName + " with invalid message"
                                + " (message=" + message + ")"
                );
                return;
            }

            playerData.setTimestamp(ActionType.CHATTING);
            handlePacketChecks(playerData, event);
            return;
        }

        // Handles CLIENT_STATUS packets.
        if (packetType == PacketType.Play.Client.CLIENT_STATUS) {
            @NotNull WrapperPlayClientClientStatus clientStatus = new WrapperPlayClientClientStatus(event);
            WrapperPlayClientClientStatus.Action action = clientStatus.getAction();

            switch (action) {
                case OPEN_INVENTORY_ACHIEVEMENT:
                    playerData.setInventoryOpen(true);
                    playerData.setTimestamp(ActionType.INVENTORY_OPEN);
                    break;

                case PERFORM_RESPAWN:
                    boolean dead = player.isDead();

                    // Checks for invalid respawn conditions.
                    if (inventoryOpen || placingBlock || digging || !dead) {
                        KickUtil.kickPlayer(player, event, Settings.respawnInvalidConditions,
                                "Sent PERFORM_RESPAWN packet with invalid conditions"
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

            handlePacketChecks(playerData, event);
            return;
        }

        // Handles CLOSE_WINDOW packets.
        if (packetType == PacketType.Play.Client.CLOSE_WINDOW) {
            if (placingBlock || digging) {
                KickUtil.kickPlayer(player, event, Settings.closeWindowInvalidConditions,
                        "Sent " + packetName + " with invalid conditions"
                                + " (placingBlock=" + placingBlock
                                + " digging=" + digging + ")"
                );
                return;
            }

            playerData.setInventoryOpen(false);
            playerData.setTimestamp(ActionType.CLOSE_WINDOW);
            handlePacketChecks(playerData, event);
            return;
        }

        // Handles PLUGIN_MESSAGE packets.
        if (packetType == PacketType.Play.Client.PLUGIN_MESSAGE) {
            @NotNull WrapperPlayClientPluginMessage pluginMessage = new WrapperPlayClientPluginMessage(event);
            @Nullable String channelName = pluginMessage.getChannelName();

            // Checks for packets with invalid channel names.
            if (channelName == null) {
                KickUtil.kickPlayer(player, event, Settings.pluginMessageInvalidChannelName,
                        "Sent " + packetName + " with invalid channel name"
                                + " (channelName=" + null + ")"
                );
                return;
            }

            byte[] rawData = pluginMessage.getData();
            int payloadSize = rawData.length;
            @NotNull String data = new String(rawData, StandardCharsets.UTF_8);

            // Checks for packets with invalid payload sizes.
            if (payloadSize > 15000 || payloadSize == 0) {
                KickUtil.kickPlayer(player, event, Settings.pluginMessageInvalidSize,
                        "Sent " + packetName + " with invalid size"
                                + " (size=" + payloadSize + ")"
                );
                return;
            }

            switch (channelName) {
                case "MC|ItemName":
                    // Checks for ItemName payloads with invalid sizes.
                    if (payloadSize > (playerData.getVersion().isOlderThanOrEquals(ClientVersion.V_1_8) ? 31 : 32)) {
                        KickUtil.kickPlayer(player, event, Settings.itemNameInvalidSize,
                                "Sent ItemName payload with invalid size"
                                        + " (size=" + payloadSize + ")"
                        );
                        return;
                    }

                    // Checks for ItemName payloads with invalid data.
                    if (!data.isEmpty() && !disallowedItemNameChars.contains(data.charAt(0))) {
                        KickUtil.kickPlayer(player, event, Settings.itemNameInvalidData,
                                "Sent ItemName payload with invalid data"
                                        + " (data=" + data + ")"
                        );

                        // TODO: Remove this in production.
                        FileUtil.printDataToFile(data, "item-name-data.txt");
                    }
                    break;

                case "MC|TrSel":
                    // Checks for TradeSelect payloads with invalid data.
                    if (!data.equals("\u0000\u0000\u0000\u0000")
                            && !data.equals("\u0000\u0000\u0000\u0001")
                            && !data.equals("\u0000\u0000\u0000\u0002")) {
                        KickUtil.kickPlayer(player, event, Settings.tradeSelectInvalidData,
                                "Sent TradeSelect payload with invalid data"
                                        + " (data=" + data + ")"
                        );
                        return;
                    }
                    break;

                case "MC|PickItem":
                    if (!data.equals("\t")) {
                        // TODO: Remove this in production.
                        FileUtil.printDataToFile(data, "pick-item-data.txt");
                    }
                    break;

                case "MC|Beacon":
                    InventoryType type = openInventory.getType();

                    // Checks for sending Beacon payloads with invalid inventory types.
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
                    break;

                case "MC|BOpen":
                case "MC|BEdit":
                case "MC|BSign":
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
                    break;

                case "MC|AdvCdm":
                case "MC|AutoCmd":
                    if (!player.isOp()) {
                        KickUtil.kickPlayer(player, event, Settings.commandBlockInvalidConditions,
                                "Sent CommandBlock payload with invalid conditions"
                                        + " (op=" + false + ")"
                        );
                        return;
                    }
                    break;

                default:
                    break;
            }

            playerData.setTimestamp(ActionType.SENT_PAYLOAD);
            handlePacketChecks(playerData, event);
            return;
        }

        // Handles ENTITY_ACTION packets.
        if (packetType == PacketType.Play.Client.ENTITY_ACTION) {
            @NotNull WrapperPlayClientEntityAction packet = new WrapperPlayClientEntityAction(event);
            WrapperPlayClientEntityAction.Action playerAction = packet.getAction();

            int entityId = packet.getEntityId();
            int jumpBoost = packet.getJumpBoost();

            // Checks if the entity ID doesn't match the player.
            if (entityId != player.getEntityId()) {
                KickUtil.kickPlayer(player, event, Settings.entityActionInvalidEntityID,
                        "Sent " + packetName + " with invalid entity ID"
                                + " (entityId=" + entityId + ")"
                );
                return;
            }

            // Prints debug information.
            MessageUtil.debug(packetName
                    + " entityId=" + entityId
                    + " action=" + playerAction
                    + " jumpBoost=" + jumpBoost
            );

            // Checks for invalid jump boost values.
            if (playerAction != WrapperPlayClientEntityAction.Action.START_JUMPING_WITH_HORSE && jumpBoost != 0) {
                KickUtil.kickPlayer(player, event, Settings.entityActionInvalidJumpBoost,
                        "Send " + packetName + " with invalid jump boost");
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
                    // Checks for invalid sneaking conditions.
                    if (sneaking) {
                        KickUtil.kickPlayer(player, event, Settings.startSneakingInvalidConditions,
                                "Sent START_SNEAKING with invalid conditions"
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

                case OPEN_HORSE_INVENTORY:
                    // Checks for players sending OpenHorseInventory packets while not in a vehicle.
                    if (!insideVehicle) {
                        KickUtil.kickPlayer(player, event, Settings.openHorseInventoryInvalidConditions,
                                "Sent OPEN_HORSE_INVENTORY with invalid conditions"
                                        + " (insideVehicle=" + false + ")"
                        );
                        return;
                    }

                    // Checks for players sending OpenHorseInventory packets while not riding a horse.
                    if (!(vehicle instanceof Horse)) {
                        KickUtil.kickPlayer(player, event, Settings.openHorseInventoryInvalidVehicle,
                                "Sent OPEN_HORSE_INVENTORY with invalid vehicle"
                                        + " (vehicle=" + vehicle + ")"
                        );
                        return;
                    }

                    @NotNull Horse horse = (Horse) vehicle;

                    // Checks for players sending OpenHorseInventory packets while not riding a tamed horse.
                    if (!horse.isTamed()) {
                        KickUtil.kickPlayer(player, event, Settings.openHorseInventoryInvalidTamed,
                                "Sent OPEN_HORSE_INVENTORY with invalid tamed"
                                        + " (tamed=" + false + ")"
                        );
                    }

                    playerData.setInventoryOpen(true);
                    playerData.setTimestamp(ActionType.INVENTORY_OPEN);
                    break;

                case LEAVE_BED:
                    boolean sleeping = player.isSleeping();

                    // Checks for players sending LeaveBed packets while not sleeping.
                    if (inventoryOpen || !sleeping) {
                        KickUtil.kickPlayer(player, event, Settings.stopSleepingInvalidConditions,
                                "Sent LEAVE_BED with invalid conditions"
                                        + " (inventoryOpen=" + inventoryOpen
                                        + " sleeping=" + sleeping + ")"
                        );
                        return;
                    }
                    break;

                case START_JUMPING_WITH_HORSE:
                case STOP_JUMPING_WITH_HORSE:
                    EntityType entityType = vehicle.getType();

                    // Checks for players sending RidingJump packets while not riding a horse.
                    if (!insideVehicle || entityType != EntityType.HORSE) {
                        KickUtil.kickPlayer(player, event, Settings.ridingJumpInvalidConditions,
                                "Sent RIDING_JUMP with invalid conditions"
                                        + " (insideVehicle=" + insideVehicle
                                        + " vehicle=" + (insideVehicle ? entityType : null) + ")"
                        );
                        return;
                    }

                    // Checks for players sending RidingJump packets with invalid jump boost values.
                    if (jumpBoost < 0 || jumpBoost > 100) {
                        KickUtil.kickPlayer(player, event, Settings.ridingJumpInvalidJumpBoost,
                                "Sent RIDING_JUMP with invalid jump boost"
                                        + " (jumpBoost=" + jumpBoost + ")"
                        );
                        return;
                    }
                    break;

                case START_FLYING_WITH_ELYTRA:
                    MessageUtil.debug("START_FLYING_WITH_ELYTRA");
                    break;

                default:
                    break;
            }

            handlePacketChecks(playerData, event);
            return;
        }

        // Handles FLYING packets.
        if (packetType == PacketType.Play.Client.PLAYER_POSITION
                || packetType == PacketType.Play.Client.PLAYER_ROTATION
                || packetType == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION
                || packetType == PacketType.Play.Client.PLAYER_FLYING) {
            @NotNull WrapperPlayClientPlayerFlying flying = new WrapperPlayClientPlayerFlying(event);
            boolean positionChanged = flying.hasPositionChanged();
            boolean rotationChanged = flying.hasRotationChanged();
            com.github.retrooper.packetevents.protocol.world.Location location = flying.getLocation();
            double flyingXPos = location.getX();
            double flyingYPos = location.getY();
            double flyingZPos = location.getZ();
            float flyingYaw = location.getYaw();
            float flyingPitch = location.getPitch();

            // Handles invalid Y data.
            if (Math.abs(flyingYPos) > 1.0E9) {
                KickUtil.kickPlayer(player, event, Settings.flyingInvalidYData,
                        "Sent " + packetName + " with invalid Y data"
                                + " (y=" + flyingYPos + ")"
                );
                return;
            }

            // Handles empty Flying packets.
            if (!positionChanged && !rotationChanged) {
                if (!(flyingXPos == 0.0 && flyingYPos == 0.0 && flyingZPos == 0.0)) {
                    KickUtil.kickPlayer(player, event, Settings.flyingInvalidPositionData,
                            "Sent " + packetName + " with invalid position data"
                                    + " (x=" + flyingXPos
                                    + " y=" + flyingYPos
                                    + " z=" + flyingZPos + ")"
                    );
                    return;
                }

                if (!(flyingYaw == 0.0 && flyingPitch == 0.0)) {
                    KickUtil.kickPlayer(player, event, Settings.flyingInvalidRotationData,
                            "Sent " + packetName + " with invalid rotation data"
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
            @NotNull Location newLocation = new Location(world, flyingXPos, flyingYPos, flyingZPos, yaw, pitch);

            playerData.setLocation(newLocation);

            // Handles player packet data (from Dusk).
            // Bug fix: Ignores Flying packets sent after blocking/releasing.
            // Bug fix: Ignores Flying packets sent after breaking blocks.
            // Bug fix: Ignores Flying packets sent after being teleported.
            if (playerData.getTicksSince(ActionType.BLOCKING) > 1
                    && playerData.getTicksSince(ActionType.RELEASE_USE_ITEM) > 1
                    && playerData.getTicksSince(ActionType.BLOCK_BREAK) > 1
                    && playerData.getTicksSince(ActionType.TELEPORT) > 2) {
                if (!positionChanged && !rotationChanged) {
                    playerData.handlePlayerPacket(new CustomLocation(null, null));
                } else if (!positionChanged) {
                    playerData.handlePlayerPacket(new CustomLocation(null, new Vector2f(flyingYaw, flyingPitch)));
                } else if (!rotationChanged) {
                    playerData.handlePlayerPacket(new CustomLocation(new Vector3d(flyingXPos, flyingYPos, flyingZPos), null));
                } else {
                    playerData.handlePlayerPacket(new CustomLocation(new Vector3d(flyingXPos, flyingYPos, flyingZPos), new Vector2f(flyingYaw, flyingPitch)));
                }
            }

            // Handles player rotations.
            if (rotationChanged) {
                if (Math.abs(flyingPitch) > 90.0) {
                    KickUtil.kickPlayer(player, event, Settings.flyingInvalidPitch,
                            "Sent " + packetName + " with invalid pitch"
                                    + " (pitch=" + flyingPitch + ")"
                    );
                    return;
                }

                WrapperPlayClientPlayerFlying lastRotationPacket = playerData.getLastRotationPacket();

                if (lastRotationPacket != null) {
                    float fromPitch = lastRotationPacket.getLocation().getPitch();
                    float fromYaw = lastRotationPacket.getLocation().getYaw();

                    // Checks if the player has rotated.
                    if (Math.abs(flyingPitch - fromPitch) != 0.0
                            || Math.abs(flyingYaw - fromYaw) != 0.0) {
                        handleRotationChecks(playerData, new RotationEvent(flying, lastRotationPacket));
                    }
                }

                playerData.setLastRotationPacket(flying);
            }

            // Handles player movement.
            if (positionChanged) {
                int enterVehicleTicks = playerData.getTicksSince(ActionType.ENTER_VEHICLE);

                if (insideVehicle && enterVehicleTicks > 4) {
                    MessageUtil.debug("Cancelled" + packetName + " for " + playerName + " (inside vehicle)");
                    event.setCancelled(true);
                    vehicle.eject();
                    return;
                }

                WrapperPlayClientPlayerFlying lastPositionPacket = playerData.getLastPositionPacket();

                if (lastPositionPacket != null) {
                    @NotNull Vector3d fromPosition = lastPositionPacket.getLocation().getPosition();
                    double fromXPos = fromPosition.getX();
                    double fromYPos = fromPosition.getY();
                    double fromZPos = fromPosition.getZ();

                    // Checks if the player has moved.
                    if (Math.abs(flyingXPos - fromXPos) != 0.0
                            || Math.abs(flyingYPos - fromYPos) != 0.0
                            || Math.abs(flyingZPos - fromZPos) != 0.0) {

                        // Ignores teleport packets.
                        playerData.setMoving(true);
                        handleMovementChecks(playerData, new MovementEvent(playerData, flying, lastPositionPacket, event));
                    } else {
                        playerData.setMoving(false);
                    }
                }

                playerData.setLastPositionPacket(flying);
            }

            if (playerData.getTicksSince(ActionType.ANIMATION) > 0) {
                playerData.setDigging(false);
            }

            int totalTicks = playerData.getTotalTicks();
            int lastFlyingTicks = playerData.getLastFlyingTicks();
            int lastDroppedPackets = playerData.getLastDroppedPackets();

            playerData.setPlacingBlock(false);
            playerData.setLastDroppedPackets(totalTicks - lastFlyingTicks > 2 ? totalTicks : lastDroppedPackets);
            playerData.setLastFlyingTicks(totalTicks);
            playerData.setTimestamp(ActionType.FLYING_PACKET);

            handlePacketChecks(playerData, event);
            return;
        }

        // Handles SLOT_STATE_CHANGE packets.
        if (packetType == PacketType.Play.Client.SLOT_STATE_CHANGE) {
            @NotNull WrapperPlayClientSlotStateChange packet = new WrapperPlayClientSlotStateChange(event);

            int slot = packet.getSlot();
            int windowId = packet.getWindowId();
            boolean state = packet.isState();

            // Prints debug information.
            MessageUtil.debug(packetName
                    + " slot=" + slot
                    + " windowId=" + windowId
                    + " state=" + state
            );

            handlePacketChecks(playerData, event);
            return;
        }

        // Handles CREATIVE_INVENTORY_ACTION packets.
        if (packetType == PacketType.Play.Client.CREATIVE_INVENTORY_ACTION) {
            @NotNull WrapperPlayClientCreativeInventoryAction packet = new WrapperPlayClientCreativeInventoryAction(event);
            int slot = packet.getSlot();

            // Checks for packets sent while not in Creative mode.
            if (gameMode != GameMode.CREATIVE) {
                KickUtil.kickPlayer(player, event, Settings.creativeInventoryActionInvalidConditions,
                        "Sent " + packetName + " with invalid conditions"
                                + " (gamemode=" + gameMode + ")"
                );
                return;
            }

            // Checks for packets with invalid slots.
            if ((slot < -1 || slot > 44) && slot != -999) {
                KickUtil.kickPlayer(player, event, Settings.creativeInventoryActionInvalidSlot,
                        "Sent " + packetName + " with invalid slot"
                                + " (slot=" + slot + ")"
                );
                return;
            }

            @Nullable ItemStack oldItem = null;
            com.github.retrooper.packetevents.protocol.item.ItemStack newItem = packet.getItemStack();

            // Cancels packets that set an already empty slot to null.
            if (slot != -1 && slot != -999) {
                oldItem = openInventory.getItem(slot);

                if (oldItem.getType() == Material.AIR && newItem.isEmpty()) {
                    MessageUtil.debug("Cancelled " + packetName + " for " + playerName + " (empty slot)");
                    event.setCancelled(true);
                    return;
                }
            }

            // Prints debug information.
            MessageUtil.debug(packetName
                    + " slot=" + slot
                    + " oldItem=" + oldItem
                    + " newItem=" + newItem
            );

            handlePacketChecks(playerData, event);
            return;
        }

        // Handles CLIENT_SETTINGS packets.
        if (packetType == PacketType.Play.Client.CLIENT_SETTINGS) {
            @NotNull WrapperPlayClientSettings packet = new WrapperPlayClientSettings(event);
            WrapperPlayClientSettings lastPacket = playerData.getLastSettingsPacket();

            // Current packet data
            String locale = packet.getLocale();
            int localeLength = locale.length();
            int viewDistance = packet.getViewDistance();
            WrapperCommonClientSettings.ChatVisibility chatVisibility = packet.getChatVisibility();
            boolean chatColors = packet.isChatColors();
            byte skinMask = packet.getSkinMask();
            HumanoidArm mainHand = packet.getMainHand();
            boolean textFilteringEnabled = packet.isTextFilteringEnabled();
            boolean allowServerListings = packet.isServerListingAllowed();
            WrapperCommonClientSettings.ParticleStatus particleStatus = packet.getParticleStatus();
            byte ignoredDifficulty = packet.getIgnoredDifficulty();

            // Last packet data
            if (lastPacket != null) {
                String lastLocale = lastPacket.getLocale();
                int lastViewDistance = lastPacket.getViewDistance();
                WrapperCommonClientSettings.ChatVisibility lastChatVisibility = lastPacket.getChatVisibility();
                boolean lastChatColors = lastPacket.isChatColors();
                byte lastSkinMask = lastPacket.getSkinMask();
                HumanoidArm lastMainHand = lastPacket.getMainHand();
                boolean lastTextFilteringEnabled = lastPacket.isTextFilteringEnabled();
                boolean lastAllowServerListings = lastPacket.isServerListingAllowed();
                WrapperCommonClientSettings.ParticleStatus lastParticleStatus = lastPacket.getParticleStatus();
                byte lastIgnoredDifficulty = lastPacket.getIgnoredDifficulty();

                // Ignores duplicate SETTINGS packets to save resources.
                if (locale.equals(lastLocale)
                        && viewDistance == lastViewDistance
                        && chatVisibility == lastChatVisibility
                        && chatColors == lastChatColors
                        && skinMask == lastSkinMask
                        && mainHand == lastMainHand
                        && textFilteringEnabled == lastTextFilteringEnabled
                        && allowServerListings == lastAllowServerListings
                        && particleStatus == lastParticleStatus
                        && ignoredDifficulty == lastIgnoredDifficulty) {
                    MessageUtil.debug("Cancelled " + packetName + " for " + playerName + " (duplicate packet)");
                    event.setCancelled(true);
                    return;
                }
            }

            // Prints debug information.
            MessageUtil.debug(packetName
                    + " locale=" + locale
                    + " localeLength=" + localeLength
                    + " viewDistance=" + viewDistance
                    + " chatVisibility=" + chatVisibility
                    + " chatColors=" + chatColors
                    + " skinMask=" + skinMask
                    + " mainHand=" + mainHand
                    + " textFilteringEnabled=" + textFilteringEnabled
                    + " allowServerListings=" + allowServerListings
                    + " particleStatus=" + particleStatus
                    + " ignoredDifficulty=" + ignoredDifficulty
            );

            // Checks for packets with invalid locale lengths.
            if (localeLength < 2 || localeLength > 8) {
                KickUtil.kickPlayer(player, event, Settings.settingsInvalidLocale,
                        "Sent " + packetName + " with invalid locale"
                                + " (locale=" + locale + ")"
                                + " (length=" + localeLength + ")"
                );
                return;
            }

            // Checks for packets with invalid view distances.
            if (viewDistance < 2 || viewDistance > 48) {
                KickUtil.kickPlayer(player, event, Settings.settingsInvalidViewDistance,
                        "Sent " + packetName + " with invalid view distance"
                                + " (viewDistance=" + viewDistance + ")"
                );
                return;
            }

            playerData.setLastSettingsPacket(packet);
            handlePacketChecks(playerData, event);
            return;
        }

        // Handles SPECTATE packets.
        if (packetType == PacketType.Play.Client.SPECTATE) {
            @NotNull WrapperPlayClientSpectate packet = new WrapperPlayClientSpectate(event);
            UUID targetUUID = packet.getTargetUUID();

            // Prints debug information.
            MessageUtil.debug(packetName
                    + " target=" + targetUUID
            );

            // Checks for packets sent while not in Spectator mode.
            if (gameMode != GameMode.SPECTATOR) {
                KickUtil.kickPlayer(player, event, Settings.spectateInvalidConditions,
                        "Sent " + packetName + " with invalid conditions"
                                + " (gamemode=" + gameMode + ")"
                );
                return;
            }

            boolean validTarget = false;

            for (@NotNull Entity entity : world.getEntities()) {
                if (entity.getUniqueId().equals(targetUUID)) {
                    validTarget = true;
                    break;
                }
            }

            // Checks for packets with invalid target UUIDs.
            if (!validTarget) {
                KickUtil.kickPlayer(player, event, Settings.spectateInvalidTarget,
                        "Sent " + packetName + " with invalid target"
                                + " (target=" + targetUUID + ")"
                );
                return;
            }

            handlePacketChecks(playerData, event);
            return;
        }

        // Handles STEER_VEHICLE packets.
        if (packetType == PacketType.Play.Client.STEER_VEHICLE) {
            @NotNull WrapperPlayClientSteerVehicle packet = new WrapperPlayClientSteerVehicle(event);
            float sideways = packet.getSideways();
            float forward = packet.getForward();
            boolean jump = packet.isJump();

            // Prints debug information.
            MessageUtil.debug(packetName
                    + " sideways=" + sideways
                    + " forward=" + forward
                    + " jump=" + jump
            );

            // Checks for packets with a jump value while not riding a Horse.
            if (jump && !(vehicle instanceof Horse)) {
                KickUtil.kickPlayer(player, event, Settings.steerVehicleInvalidJump,
                        "Sent " + packetName + " with invalid jump"
                                + " (jump=" + true + ")"
                );
                return;
            }

            // Ignores vehicle dismount packets.
            if (sideways == 0.0f && forward == 0.0f && !jump && !insideVehicle) {
                if (player.getNearbyEntities(3, 3, 3).stream().noneMatch(Vehicle.class::isInstance)) {
                    KickUtil.kickPlayer(player, event, Settings.steerVehicleInvalidConditions,
                            "Sent " + packetName + " with invalid conditions"
                                    + " (sideways=" + 0.0f
                                    + " forward=" + 0.0f
                                    + " jump=" + false
                                    + " insideVehicle=" + false + ")"
                    );
                    return;
                }
            } else if (!insideVehicle) {
                MessageUtil.debug("Cancelled " + packetName + " for " + playerName + " (not inside vehicle)");
                event.setCancelled(true);
                return;
            }

            // Checks for invalid sideways & forward values
            steerVehicleCheck(player, event, packet, sideways);
            steerVehicleCheck(player, event, packet, forward);

            playerData.setTimestamp(ActionType.STEER_VEHICLE);
            handlePacketChecks(playerData, event);
            return;
        }

        // Handles TAB_COMPLETE packets.
        if (packetType == PacketType.Play.Client.TAB_COMPLETE) {
            @NotNull WrapperPlayClientTabComplete packet = new WrapperPlayClientTabComplete(event);
            Optional<Integer> transactionId = packet.getTransactionId();
            boolean assumeCommand = packet.isAssumeCommand();
            String text = packet.getText();
            Optional<Vector3i> blockPosition = packet.getBlockPosition();

            // Prints debug information.
            @NotNull StringBuilder debugMessage = new StringBuilder(packetName);
            transactionId.ifPresent(id -> debugMessage.append(" transactionId=").append(id));
            debugMessage.append(" assumeCommand=").append(assumeCommand);
            debugMessage.append(" text=").append(text);
            blockPosition.ifPresent(pos -> debugMessage.append(" blockPosition=").append(pos));
            MessageUtil.debug(String.valueOf(debugMessage));

            // Checks for packets with empty text.
            if (text.isEmpty()) {
                KickUtil.kickPlayer(player, event, Settings.tabCompleteInvalidMessage,
                        "Sent " + packetName + " with invalid text"
                                + " (text=" + text + ")"
                );
                return;
            }

            handlePacketChecks(playerData, event);
            return;
        }

        // Handles WINDOW_CONFIRMATION packets.
        if (packetType == PacketType.Play.Client.WINDOW_CONFIRMATION) {
            @NotNull WrapperPlayClientWindowConfirmation packet = new WrapperPlayClientWindowConfirmation(event);
            int windowId = packet.getWindowId();
            short actionId = packet.getActionId();
            boolean accepted = packet.isAccepted();

            // Checks for packets that were not accepted by the server.
            if (!accepted) {
                KickUtil.kickPlayer(player, event, Settings.windowConfirmationNotAccepted,
                        "Sent " + packetName + " that was not accepted"
                );
                return;
            }

            // Checks for packets with invalid window IDs.
            if (windowId != 0 && !inventoryOpen) {
                KickUtil.kickPlayer(player, event, Settings.windowConfirmationInvalidWindowId,
                        "Sent " + packetName + " with an invalid window ID"
                                + " (windowId=" + windowId + ")"
                );
                return;
            }

            // Calculates transaction ping.
            if (playerData.getTransactionTime().containsKey(actionId)) {
                long transactionStamp = playerData.getTransactionTime().get(actionId);
                playerData.setTransPing(System.currentTimeMillis() - transactionStamp);
                playerData.getTransactionSentMap().remove(actionId);
            }

            handlePacketChecks(playerData, event);
            return;
        }

        // Handles UPDATE_SIGN packets.
        if (packetType == PacketType.Play.Client.UPDATE_SIGN) {
            @NotNull WrapperPlayClientUpdateSign packet = new WrapperPlayClientUpdateSign(event);
            Vector3i blockPosition = packet.getBlockPosition();
            String[] textLines = packet.getTextLines();
            boolean isFrontText = packet.isFrontText();

            MessageUtil.debug(packetName
                    + " blockPosition=" + blockPosition
                    + " textLines=" + Arrays.toString(textLines)
                    + " isFrontText=" + isFrontText
            );

            for (@NotNull String line : textLines) {
                int length = line.length();

                // Checks for packets with invalid text lengths.
                if (length > 45) {
                    KickUtil.kickPlayer(player, event, Settings.updateSignInvalidData,
                            "Sent " + packetName + " with invalid length"
                                    + " (length=" + length + ")"
                    );
                    return;
                }
            }

            handlePacketChecks(playerData, event);
            return;
        }

        // Handles INTERACT_ENTITY packets.
        if (packetType == PacketType.Play.Client.INTERACT_ENTITY) {
            @NotNull WrapperPlayClientInteractEntity packet = new WrapperPlayClientInteractEntity(event);
            int entityId = packet.getEntityId();
            WrapperPlayClientInteractEntity.InteractAction interactAction = packet.getAction();
            Optional<Vector3f> target = packet.getTarget();
            InteractionHand interactionHand = packet.getHand();
            Optional<Boolean> sneaking = packet.isSneaking();

            @Nullable Entity targetEntity = null;

            for (@NotNull Entity entity : world.getEntities()) {
                if (entity.getEntityId() == entityId) {
                    targetEntity = entity;
                    break;
                }
            }

            if (targetEntity == null
                    || targetEntity.isDead()
                    || targetEntity.getWorld() != world) {
                event.setCancelled(true);
                return;
            }

            // Prints debug information.
            @NotNull StringBuilder debugMessage = new StringBuilder(packetName);
            debugMessage.append(" entityId=").append(entityId);
            debugMessage.append(" interactAction=").append(interactAction);
            target.ifPresent(pos -> debugMessage.append(" target=").append(pos));
            debugMessage.append(" interactionHand=").append(interactionHand);
            sneaking.ifPresent(sneak -> debugMessage.append(" sneaking=").append(sneak));
            MessageUtil.debug(String.valueOf(debugMessage));

            if (Settings.interactEntityInvalidDistance) {
                Location entityLoc = targetEntity.getLocation();
                double distance = entityLoc.distance(playerLoc);

                if (distance > 7.03) {
                    MessageUtil.debug("Cancelled " + packetName + " for " + playerName + " (distance=" + distance + ")");
                    event.setCancelled(true);
                    return;
                }
            }

            boolean entityIsPlayer = targetEntity.equals(player);

            // Checks for packets with invalid conditions.
            if (placingBlock || entityIsPlayer || entityId < 0) {
                KickUtil.kickPlayer(player, event, Settings.interactEntityInvalidConditions,
                        "Sent " + packetName + " with invalid conditions"
                                + " (placingBlock=" + placingBlock
                                + " entityEqualsPlayer=" + entityIsPlayer
                                + " entityId=" + entityId + ")"
                );
                return;
            }

            // Sets timestamps for the player's action.
            if (interactAction != null) {
                switch (interactAction) {
                    case INTERACT:
                    case INTERACT_AT:
                        playerData.setTimestamp(ActionType.ENTITY_INTERACT);
                        break;

                    case ATTACK:
                        playerData.setTimestamp(ActionType.ENTITY_ATTACK);
                        break;

                    default:
                        break;
                }
            }

            handlePacketChecks(playerData, event);
            return;
        }

        // Handles CLICK_WINDOW packets.
        if (packetType == PacketType.Play.Client.CLICK_WINDOW) {
            // Cancels this packet if the player is in Spectator mode.
            if (gameMode == GameMode.SPECTATOR) {
                MessageUtil.debug("Cancelled " + packetName + " for " + playerName + " (in Spectator mode)");
                event.setCancelled(true);
                return;
            }

            @NotNull WrapperPlayClientClickWindow packet = new WrapperPlayClientClickWindow(event);
            int windowId = packet.getWindowId();
            Optional<Integer> stateId = packet.getStateId();
            int slot = packet.getSlot();
            int button = packet.getButton();
            Optional<Integer> actionNumber = packet.getActionNumber();
            WrapperPlayClientClickWindow.WindowClickType windowClickType = packet.getWindowClickType();
            Optional<Map<Integer, com.github.retrooper.packetevents.protocol.item.ItemStack>> slots = packet.getSlots();
            com.github.retrooper.packetevents.protocol.item.ItemStack carriedItemStack = packet.getCarriedItemStack();

            // Prints debug information.
            @NotNull StringBuilder debugMessage = new StringBuilder(packetName);
            debugMessage.append(" windowId=").append(windowId);
            stateId.ifPresent(id -> debugMessage.append(" stateId=").append(id));
            debugMessage.append(" slot=").append(slot);
            debugMessage.append(" button=").append(button);
            actionNumber.ifPresent(number -> debugMessage.append(" actionNumber=").append(number));
            debugMessage.append(" windowClickType=").append(windowClickType);
            slots.ifPresent(slotMap -> debugMessage.append(" slots=").append(slotMap));
            debugMessage.append(" carriedItemStack=").append(carriedItemStack);
            MessageUtil.debug(String.valueOf(debugMessage));

            if (windowId == 0) {
                int diff = slot - (openInventory.countSlots() - 1);

                // Checks for packets with invalid slots.
                if (slot > 44 || diff > 4 || (slot != -999 && slot < -1)) {
                    KickUtil.kickPlayer(player, event, Settings.windowClickInvalidSlot,
                            "Sent " + packetName + " with invalid slot"
                                    + " (slot=" + slot
                                    + " diff=" + diff + ")"
                    );
                    return;
                }
            }

            switch (windowClickType) {
                case PICKUP:
                    // Checks for packets with invalid Pickup button values.
                    if (button != 0 && button != 1) {
                        KickUtil.kickPlayer(player, event, Settings.windowClickInvalidPickupButton,
                                "Sent " + packetName + " with invalid PICKUP button"
                                        + " (button=" + button + ")"
                        );
                        return;
                    }
                    break;

                case QUICK_MOVE:
                    // Checks for packets with invalid QuickMove button values.
                    if (button != 0 && button != 1 && button != 2 && button != 5) {
                        KickUtil.kickPlayer(player, event, Settings.windowClickInvalidQuickMoveButton,
                                "Sent " + packetName + " with invalid QUICK_MOVE button"
                                        + " (button=" + button + ")"
                        );
                        return;
                    }
                    break;

                case SWAP:
                    // Checks for packets with invalid Swap button values.
                    if (button != 0 && button != 1
                            && button != 2 && button != 3
                            && button != 4 && button != 5
                            && button != 6 && button != 7
                            && button != 8 && button != 40) {
                        KickUtil.kickPlayer(player, event, Settings.windowClickInvalidSwapButton,
                                "Sent " + packetName + " with invalid SWAP button"
                                        + " (button=" + button + ")"
                        );
                        return;
                    }
                    break;

                case CLONE:
                    // Checks for packets with invalid Clone button values.
                    if (button != 0 && button != 1 && button != 2 && button != 4 && button != 5) {
                        KickUtil.kickPlayer(player, event, Settings.windowClickInvalidCloneButton,
                                "Sent " + packetName + " with invalid CLONE button"
                                        + " (button=" + button + ")"
                        );
                        return;
                    }
                    break;

                case THROW:
                    // Checks for packets with invalid Throw button values.
                    if (button != 0 && button != 1
                            && button != 2 && button != 4
                            && button != 5 && button != 6) {
                        KickUtil.kickPlayer(player, event, Settings.windowClickInvalidThrowButton,
                                "Sent " + packetName + " with invalid THROW button"
                                        + " (button=" + button + ")"
                        );
                        return;
                    }
                    break;

                case QUICK_CRAFT:
                    // Checks for packets with invalid QuickCraft button values.
                    if (button != 0 && button != 1
                            && button != 2 && button != 4
                            && button != 5 && button != 6
                            && button != 8 && button != 9
                            && button != 10) {
                        KickUtil.kickPlayer(player, event, Settings.windowClickInvalidQuickCraftButton,
                                "Sent " + packetName + " with invalid QUICK_CRAFT button"
                                        + " (button=" + button + ")"
                        );
                        return;
                    }
                    break;

                case PICKUP_ALL:
                    // Checks for packets with invalid PickupAll button values.
                    if (button != 0 && button != 1
                            && button != 2 && button != 3
                            && button != 4 && button != 5
                            && button != 6) {
                        KickUtil.kickPlayer(player, event, Settings.windowClickInvalidPickupAllButton,
                                "Sent " + packetName + " with invalid PICKUP_ALL button"
                                        + " (button=" + button + ")"
                        );
                        return;
                    }
                    break;

                case UNKNOWN:
                    // Checks for packets with unknown WindowClick types.
                    KickUtil.kickPlayer(player, event, Settings.windowClickInvalidType,
                            "Sent " + packetName + " with invalid type"
                                    + " (type=" + windowClickType + ")"
                    );
                    break;

                default:
                    break;
            }

            playerData.setTimestamp(ActionType.WINDOW_CLICK);
            handlePacketChecks(playerData, event);
            return;
        }

        // Handles CHAT_PREVIEW packets.
        if (packetType == PacketType.Play.Client.CHAT_PREVIEW) {
            @NotNull WrapperPlayClientChatPreview packet = new WrapperPlayClientChatPreview(event);
            int queryId = packet.getQueryId();
            String message = packet.getMessage();

            MessageUtil.debug(packetName
                    + " queryId=" + queryId
                    + " message=" + message
            );

            handlePacketChecks(playerData, event);
            return;
        }

        // Handles TELEPORT_CONFIRM packets.
        if (packetType == PacketType.Play.Client.TELEPORT_CONFIRM) {
            @NotNull WrapperPlayClientTeleportConfirm packet = new WrapperPlayClientTeleportConfirm(event);
            int teleportId = packet.getTeleportId();

            MessageUtil.debug(packetName
                    + " teleportId=" + teleportId
            );

            handlePacketChecks(playerData, event);
            return;
        }

        // Handles QUERY_BLOCK_NBT packets.
        if (packetType == PacketType.Play.Client.QUERY_BLOCK_NBT) {
            @NotNull WrapperPlayClientQueryBlockNBT packet = new WrapperPlayClientQueryBlockNBT(event);
            int transactionId = packet.getTransactionId();
            Vector3i blockPosition = packet.getBlockPosition();

            MessageUtil.debug(packetName
                    + " transactionId=" + transactionId
                    + " blockPosition=" + blockPosition
            );

            handlePacketChecks(playerData, event);
            return;
        }

        // Handles PONG packets.
        if (packetType == PacketType.Play.Client.PONG) {
            @NotNull WrapperPlayClientPong packet = new WrapperPlayClientPong(event);
            int id = packet.getId();

            MessageUtil.debug(packetName
                    + " id=" + id
            );

            handlePacketChecks(playerData, event);
            return;
        }

        // Handles CLICK_WINDOW_BUTTON packets.
        if (packetType == PacketType.Play.Client.CLICK_WINDOW_BUTTON) {
            @NotNull WrapperPlayClientClickWindowButton packet = new WrapperPlayClientClickWindowButton(event);
            int windowId = packet.getWindowId();
            int buttonId = packet.getButtonId();

            MessageUtil.debug(packetName
                    + " windowId=" + windowId
                    + " buttonId=" + buttonId
            );

            handlePacketChecks(playerData, event);
            return;
        }

        // Handles EDIT_BOOK packets.
        if (packetType == PacketType.Play.Client.EDIT_BOOK) {
            @NotNull WrapperPlayClientEditBook packet = new WrapperPlayClientEditBook(event);
            int slot = packet.getSlot();
            List<String> pages = packet.getPages();
            @Nullable String title = packet.getTitle();

            @NotNull StringBuilder debugMessage = new StringBuilder(packetName);
            debugMessage.append(" slot=").append(slot);
            debugMessage.append(" pages=").append(pages);

            if (title != null) {
                debugMessage.append(" title=").append(title);
            }

            MessageUtil.debug(String.valueOf(debugMessage));
            handlePacketChecks(playerData, event);
            return;
        }

        // Handles QUERY_ENTITY_NBT packets.
        if (packetType == PacketType.Play.Client.QUERY_ENTITY_NBT) {
            @NotNull WrapperPlayClientQueryEntityNBT packet = new WrapperPlayClientQueryEntityNBT(event);
            int transactionId = packet.getTransactionId();
            int entityId = packet.getEntityId();

            MessageUtil.debug(packetName
                    + " transactionId=" + transactionId
                    + " entityId=" + entityId
            );

            handlePacketChecks(playerData, event);
            return;
        }

        // Handles GENERATE_STRUCTURE packets.
        if (packetType == PacketType.Play.Client.GENERATE_STRUCTURE) {
            @NotNull WrapperPlayClientGenerateStructure packet = new WrapperPlayClientGenerateStructure(event);
            Vector3i blockPosition = packet.getBlockPosition();
            int levels = packet.getLevels();
            boolean keepJigsaws = packet.isKeepingJigsaws();

            MessageUtil.debug(packetName
                    + " blockPosition=" + blockPosition
                    + " levels=" + levels
                    + " keepJigsaws=" + keepJigsaws
            );

            handlePacketChecks(playerData, event);
            return;
        }

        // Handles VEHICLE_MOVE packets.
        if (packetType == PacketType.Play.Client.VEHICLE_MOVE) {
            @NotNull WrapperPlayClientVehicleMove packet = new WrapperPlayClientVehicleMove(event);
            Vector3d position = packet.getPosition();
            float yaw = packet.getYaw();
            float pitch = packet.getPitch();
            boolean onGround = packet.isOnGround();

            MessageUtil.debug(packetName
                    + " position=" + position
                    + " yaw=" + yaw
                    + " pitch=" + pitch
                    + " onGround=" + onGround
            );

            handlePacketChecks(playerData, event);
            return;
        }

        // Handles STEER_BOAT packets.
        if (packetType == PacketType.Play.Client.STEER_BOAT) {
            @NotNull WrapperPlayClientSteerBoat packet = new WrapperPlayClientSteerBoat(event);
            boolean leftPaddleTurning = packet.isLeftPaddleTurning();
            boolean rightPaddleTurning = packet.isRightPaddleTurning();

            MessageUtil.debug(packetName
                    + " leftPaddleTurning=" + leftPaddleTurning
                    + " rightPaddleTurning=" + rightPaddleTurning
            );

            handlePacketChecks(playerData, event);
            return;
        }

        // Handles PICK_ITEM packets.
        if (packetType == PacketType.Play.Client.PICK_ITEM) {
            @NotNull WrapperPlayClientPickItem packet = new WrapperPlayClientPickItem(event);
            int slot = packet.getSlot();

            MessageUtil.debug(packetName
                    + " slot=" + slot
            );

            handlePacketChecks(playerData, event);
            return;
        }

        // Handles CRAFT_RECIPE_REQUEST packets.
        if (packetType == PacketType.Play.Client.CRAFT_RECIPE_REQUEST) {
            @NotNull WrapperPlayClientCraftRecipeRequest packet = new WrapperPlayClientCraftRecipeRequest(event);
            int windowId = packet.getWindowId();
            ResourceLocation recipeKey = packet.getRecipeKey();
            RecipeDisplayId recipeId = packet.getRecipeId();
            int recipeIdInt = recipeId.getId();
            boolean makeAll = packet.isMakeAll();

            MessageUtil.debug(packetName
                    + " windowId=" + windowId
                    + " recipeKey=" + recipeKey
                    + " recipeId=" + recipeIdInt
                    + " makeAll=" + makeAll
            );

            handlePacketChecks(playerData, event);
            return;
        }

        // Handles SET_DISPLAYED_RECIPE packets.
        if (packetType == PacketType.Play.Client.SET_DISPLAYED_RECIPE) {
            @NotNull WrapperPlayClientSetDisplayedRecipe packet = new WrapperPlayClientSetDisplayedRecipe(event);
            ResourceLocation recipe = packet.getRecipe();
            RecipeDisplayId recipeId = packet.getRecipeId();
            int recipeIdInt = recipeId.getId();

            MessageUtil.debug(packetName
                    + " recipe=" + recipe
                    + " recipeId=" + recipeIdInt
            );

            handlePacketChecks(playerData, event);
            return;
        }

        // Handles SET_RECIPE_BOOK_STATE packets.
        if (packetType == PacketType.Play.Client.SET_RECIPE_BOOK_STATE) {
            @NotNull WrapperPlayClientSetRecipeBookState packet = new WrapperPlayClientSetRecipeBookState(event);
            BookType bookType = packet.getBookType();
            boolean bookOpen = packet.isBookOpen();
            boolean filterActive = packet.isFilterActive();

            MessageUtil.debug(packetName
                    + " bookType=" + bookType
                    + " bookOpen=" + bookOpen
                    + " filterActive=" + filterActive
            );

            handlePacketChecks(playerData, event);
            return;
        }

        // Handles NAME_ITEM packets.
        if (packetType == PacketType.Play.Client.NAME_ITEM) {
            @NotNull WrapperPlayClientNameItem packet = new WrapperPlayClientNameItem(event);
            String itemName = packet.getItemName();

            MessageUtil.debug(packetName
                    + " itemName=" + itemName
            );

            handlePacketChecks(playerData, event);
            return;
        }

        // Handles ADVANCEMENT_TAB packets.
        if (packetType == PacketType.Play.Client.ADVANCEMENT_TAB) {
            @NotNull WrapperPlayClientAdvancementTab packet = new WrapperPlayClientAdvancementTab(event);
            WrapperPlayClientAdvancementTab.Action action = packet.getAction();
            Optional<String> tabId = packet.getTabId();
            @NotNull String tabIdValue = tabId.orElse("null");

            MessageUtil.debug(packetName
                    + " action=" + action
                    + " tabId=" + tabIdValue
            );

            handlePacketChecks(playerData, event);
            return;
        }

        // Handles SELECT_TRADE packets.
        if (packetType == PacketType.Play.Client.SELECT_TRADE) {
            @NotNull WrapperPlayClientSelectTrade packet = new WrapperPlayClientSelectTrade(event);
            int slot = packet.getSlot();

            MessageUtil.debug(packetName
                    + " slot=" + slot
            );

            handlePacketChecks(playerData, event);
            return;
        }

        // Handles SET_BEACON_EFFECT packets.
        if (packetType == PacketType.Play.Client.SET_BEACON_EFFECT) {
            @NotNull WrapperPlayClientSetBeaconEffect packet = new WrapperPlayClientSetBeaconEffect(event);
            int primaryEffect = packet.getPrimaryEffect();
            int secondaryEffect = packet.getSecondaryEffect();

            MessageUtil.debug(packetName
                    + " primaryEffect=" + primaryEffect
                    + " secondaryEffect=" + secondaryEffect
            );

            handlePacketChecks(playerData, event);
            return;
        }

        // Handles HELD_ITEM_CHANGE packets.
        if (packetType == PacketType.Play.Client.HELD_ITEM_CHANGE) {
            @NotNull WrapperPlayClientHeldItemChange packet = new WrapperPlayClientHeldItemChange(event);
            int slot = packet.getSlot();
            int inventoryOpenTicks = playerData.getTicksSince(ActionType.INVENTORY_OPEN);

            // Checks for packets with invalid conditions.
            if (inventoryOpen && inventoryOpenTicks > 5) {
                KickUtil.kickPlayer(player, event, Settings.heldItemChangeInvalidConditions,
                        "Sent " + packetName + " with invalid conditions"
                                + " (timeSinceInventoryOpen=" + inventoryOpenTicks + ")"
                );
                return;
            }

            int currentSlot = playerData.getCurrentSlot();

            // Checks for packets with invalid slot changes.
            if (slot == currentSlot
                    && playerData.getVersion().isOlderThanOrEquals(ClientVersion.V_1_8)
                    && playerData.getTicksSince(ActionType.LOGIN) > 20) {
                KickUtil.kickPlayer(player, event, Settings.heldItemChangeInvalidSlotChange,
                        "Sent " + packetName + " with invalid slot change"
                                + " (slot=" + slot + ")"
                                + " (currentSlot=" + currentSlot + ")"
                );
                return;
            }

            // Checks for packets with invalid slots.
            if (slot < 0 || slot > 8) {
                KickUtil.kickPlayer(player, event, Settings.heldItemChangeInvalidSlot,
                        "Sent " + packetName + " with invalid slot"
                                + " (slot=" + slot + ")"
                );
                return;
            }

            playerData.setCurrentSlot(slot);
            playerData.setTimestamp(ActionType.CHANGE_SLOT);
            handlePacketChecks(playerData, event);
            return;
        }

        // Handles UPDATE_COMMAND_BLOCK packets.
        if (packetType == PacketType.Play.Client.UPDATE_COMMAND_BLOCK) {
            @NotNull WrapperPlayClientUpdateCommandBlock packet = new WrapperPlayClientUpdateCommandBlock(event);
            Vector3i position = packet.getPosition();
            String command = packet.getCommand();
            WrapperPlayClientUpdateCommandBlock.CommandBlockMode mode = packet.getMode();
            boolean doesTrackOutput = packet.isDoesTrackOutput();
            boolean conditional = packet.isConditional();
            boolean automatic = packet.isAutomatic();
            short flags = packet.getFlags();

            MessageUtil.debug(packetName
                    + " position=" + position
                    + " command=" + command
                    + " mode=" + mode
                    + " doesTrackOutput=" + doesTrackOutput
                    + " conditional=" + conditional
                    + " automatic=" + automatic
                    + " flags=" + flags
            );

            handlePacketChecks(playerData, event);
            return;
        }

        // Handles UPDATE_COMMAND_BLOCK_MINECART packets.
        if (packetType == PacketType.Play.Client.UPDATE_COMMAND_BLOCK_MINECART) {
            @NotNull WrapperPlayClientUpdateCommandBlockMinecart packet = new WrapperPlayClientUpdateCommandBlockMinecart(event);
            int entityId = packet.getEntityId();
            String command = packet.getCommand();
            boolean trackOutput = packet.isTrackOutput();

            MessageUtil.debug(packetName
                    + " entityId=" + entityId
                    + " command=" + command
                    + " trackOutput=" + trackOutput
            );

            handlePacketChecks(playerData, event);
            return;
        }

        // Handles UPDATE_JIGSAW_BLOCK packets.
        if (packetType == PacketType.Play.Client.UPDATE_JIGSAW_BLOCK) {
            @NotNull WrapperPlayClientUpdateJigsawBlock packet = new WrapperPlayClientUpdateJigsawBlock(event);
            Vector3i position = packet.getPosition();
            ResourceLocation name = packet.getName();
            Optional<ResourceLocation> target = packet.getTarget();
            ResourceLocation pool = packet.getPool();
            String finalState = packet.getFinalState();
            Optional<JointType> jointType = packet.getJointType();
            int selectionPriority = packet.getSelectionPriority();
            int placementPriority = packet.getPlacementPriority();

            @NotNull StringBuilder debugMessage = new StringBuilder(packetName);
            debugMessage.append(" position=").append(position);
            debugMessage.append(" name=").append(name);
            target.ifPresent(t -> debugMessage.append(" target=").append(t));
            debugMessage.append(" pool=").append(pool);
            debugMessage.append(" finalState=").append(finalState);
            jointType.ifPresent(j -> debugMessage.append(" jointType=").append(j));
            debugMessage.append(" selectionPriority=").append(selectionPriority);
            debugMessage.append(" placementPriority=").append(placementPriority);
            MessageUtil.debug(String.valueOf(debugMessage));

            handlePacketChecks(playerData, event);
            return;
        }

        // Handles USE_ITEM packets.
        if (packetType == PacketType.Play.Client.USE_ITEM) {
            @NotNull WrapperPlayClientUseItem packet = new WrapperPlayClientUseItem(event);
            InteractionHand hand = packet.getHand();
            int sequence = packet.getSequence();
            float yaw = packet.getYaw();
            float pitch = packet.getPitch();

            MessageUtil.debug(packetName
                    + " hand=" + hand
                    + " sequence=" + sequence
                    + " yaw=" + yaw
                    + " pitch=" + pitch
            );

            handlePacketChecks(playerData, event);
            return;
        }

        // Handles CHAT_COMMAND packets.
        if (packetType == PacketType.Play.Client.CHAT_COMMAND) {
            @NotNull WrapperPlayClientChatCommand packet = new WrapperPlayClientChatCommand(event);
            String command = packet.getCommand();
            MessageSignData messageSignData = packet.getMessageSignData();
            List<SignedCommandArgument> signedArguments = packet.getSignedArguments();
            LastSeenMessages.@Nullable Update lastSeenMessages = packet.getLastSeenMessages();
            LastSeenMessages.@Nullable LegacyUpdate legacyLastSeenMessages = packet.getLegacyLastSeenMessages();

            @NotNull StringBuilder debugMessage = new StringBuilder(packetName);
            debugMessage.append(" command=").append(command);

            if (messageSignData != null) {
                SaltSignature saltSignature = messageSignData.getSaltSignature();
                long salt = saltSignature.getSalt();
                byte[] signature = saltSignature.getSignature();
                Instant timestamp = messageSignData.getTimestamp();
                boolean signedPreview = messageSignData.isSignedPreview();

                debugMessage.append(" salt=").append(salt);
                debugMessage.append(" signature=").append(Arrays.toString(signature));
                debugMessage.append(" timestamp=").append(timestamp);
                debugMessage.append(" signedPreview=").append(signedPreview);
            }

            if (signedArguments != null) {
                for (@NotNull SignedCommandArgument signedArgument : signedArguments) {
                    String argument = signedArgument.getArgument();
                    MessageSignature signature = signedArgument.getSignature();
                    byte[] bytes = signature.getBytes();

                    debugMessage.append(" argument=").append(argument);
                    debugMessage.append(" signature=").append(Arrays.toString(bytes));
                }
            }

            if (lastSeenMessages != null) {
                int offset = lastSeenMessages.getOffset();
                BitSet acknowledged = lastSeenMessages.getAcknowledged();

                debugMessage.append(" offset=").append(offset);
                debugMessage.append(" acknowledged=").append(acknowledged);
            }

            if (legacyLastSeenMessages != null) {
                LastSeenMessages messages = legacyLastSeenMessages.getLastSeenMessages();
                List<LastSeenMessages.Entry> entries = messages.getEntries();
                @Nullable LastSeenMessages.Entry lastReceived = legacyLastSeenMessages.getLastReceived();

                debugMessage.append(" messages=").append(entries);

                if (lastReceived != null) {
                    UUID uuid = lastReceived.getUUID();
                    byte[] lastVerifier = lastReceived.getLastVerifier();

                    debugMessage.append(" lastReceived=").append(uuid);
                    debugMessage.append(" lastVerifier=").append(Arrays.toString(lastVerifier));
                }
            }

            MessageUtil.debug(String.valueOf(debugMessage));
            handlePacketChecks(playerData, event);
            return;
        }

        // Handles CHAT_ACK packets.
        if (packetType == PacketType.Play.Client.CHAT_ACK) {
            @NotNull WrapperPlayClientChatAck packet = new WrapperPlayClientChatAck(event);
            int offset = packet.getOffset();
            LastSeenMessages.LegacyUpdate legacyUpdate = packet.getLastSeenMessages();
            LastSeenMessages lastSeenMessages = legacyUpdate.getLastSeenMessages();
            List<LastSeenMessages.Entry> entries = lastSeenMessages.getEntries();
            @Nullable LastSeenMessages.Entry lastReceived = legacyUpdate.getLastReceived();

            if (lastReceived != null) {
                UUID uuid = lastReceived.getUUID();
                byte[] lastVerifier = lastReceived.getLastVerifier();

                MessageUtil.debug(packetName
                        + " lastSeenMessages=" + entries
                        + " lastReceived=" + uuid
                        + " lastVerifier=" + Arrays.toString(lastVerifier)
                        + " offset=" + offset
                );
            } else {
                MessageUtil.debug(packetName
                        + " lastSeenMessages=" + entries
                        + " offset=" + offset
                );
            }

            handlePacketChecks(playerData, event);
            return;
        }

        // Handles CHAT_SESSION_UPDATE packets.
        if (packetType == PacketType.Play.Client.CHAT_SESSION_UPDATE) {
            @NotNull WrapperPlayClientChatSessionUpdate packet = new WrapperPlayClientChatSessionUpdate(event);
            RemoteChatSession chatSession = packet.getChatSession();
            UUID sessionId = chatSession.getSessionId();
            PublicProfileKey publicProfileKey = chatSession.getPublicProfileKey();
            Instant expiresAt = publicProfileKey.getExpiresAt();
            PublicKey key = publicProfileKey.getKey();
            byte[] keySignature = publicProfileKey.getKeySignature();

            MessageUtil.debug(packetName
                    + " sessionId=" + sessionId
                    + " expiresAt=" + expiresAt
                    + " key=" + key
                    + " keySignature=" + Arrays.toString(keySignature)
            );

            handlePacketChecks(playerData, event);
            return;
        }

        // Handles CHUNK_BATCH_ACK packets.
        if (packetType == PacketType.Play.Client.CHUNK_BATCH_ACK) {
            @NotNull WrapperPlayClientChunkBatchAck packet = new WrapperPlayClientChunkBatchAck(event);
            float desiredChunksPerTick = packet.getDesiredChunksPerTick();

            MessageUtil.debug(packetName
                    + " desiredChunksPerTick=" + desiredChunksPerTick
            );

            handlePacketChecks(playerData, event);
            return;
        }

        // Handles DEBUG_PING packets.
        if (packetType == PacketType.Play.Client.DEBUG_PING) {
            @NotNull WrapperPlayClientDebugPing packet = new WrapperPlayClientDebugPing(event);
            long timestamp = packet.getTimestamp();

            MessageUtil.debug(packetName
                    + " timestamp=" + timestamp
            );

            handlePacketChecks(playerData, event);
            return;
        }

        // Handles CHAT_COMMAND_UNSIGNED packets.
        if (packetType == PacketType.Play.Client.CHAT_COMMAND_UNSIGNED) {
            @NotNull WrapperPlayClientChatCommandUnsigned packet = new WrapperPlayClientChatCommandUnsigned(event);
            String command = packet.getCommand();

            MessageUtil.debug(packetName
                    + " command=" + command
            );

            handlePacketChecks(playerData, event);
            return;
        }

        // Handles DEBUG_SAMPLE_SUBSCRIPTION packets.
        if (packetType == PacketType.Play.Client.DEBUG_SAMPLE_SUBSCRIPTION) {
            @NotNull WrapperPlayClientDebugSampleSubscription packet = new WrapperPlayClientDebugSampleSubscription(event);
            WrapperPlayServerDebugSample.SampleType sampleType = packet.getSampleType();

            MessageUtil.debug(packetName
                    + " sampleType=" + sampleType
            );

            handlePacketChecks(playerData, event);
            return;
        }

        // Handles SELECT_BUNDLE_ITEM packets.
        if (packetType == PacketType.Play.Client.SELECT_BUNDLE_ITEM) {
            @NotNull WrapperPlayClientSelectBundleItem packet = new WrapperPlayClientSelectBundleItem(event);
            int slotId = packet.getSlotId();
            int selectedItemIndex = packet.getSelectedItemIndex();

            MessageUtil.debug(packetName
                    + " slotId=" + slotId
                    + " selectedItemIndex=" + selectedItemIndex
            );

            handlePacketChecks(playerData, event);
            return;
        }

        // Handles PLAYER_INPUT packets.
        if (packetType == PacketType.Play.Client.PLAYER_INPUT) {
            @NotNull WrapperPlayClientPlayerInput packet = new WrapperPlayClientPlayerInput(event);
            boolean forward = packet.isForward();
            boolean backward = packet.isBackward();
            boolean left = packet.isLeft();
            boolean right = packet.isRight();
            boolean jump = packet.isJump();
            boolean shift = packet.isShift();
            boolean sprint = packet.isSprint();

            MessageUtil.debug(packetName
                    + " forward=" + forward
                    + " backward=" + backward
                    + " left=" + left
                    + " right=" + right
                    + " jump=" + jump
                    + " shift=" + shift
                    + " sprint=" + sprint
            );

            handlePacketChecks(playerData, event);
            return;
        }

        // Handles PICK_ITEM_FROM_BLOCK packets.
        if (packetType == PacketType.Play.Client.PICK_ITEM_FROM_BLOCK) {
            @NotNull WrapperPlayClientPickItemFromBlock packet = new WrapperPlayClientPickItemFromBlock(event);
            Vector3i blockPos = packet.getBlockPos();
            boolean includeData = packet.isIncludeData();

            MessageUtil.debug(packetName
                    + " blockPos=" + blockPos
                    + " includeData=" + includeData
            );

            handlePacketChecks(playerData, event);
            return;
        }

        // Handles PICK_ITEM_FROM_ENTITY packets.
        if (packetType == PacketType.Play.Client.PICK_ITEM_FROM_ENTITY) {
            @NotNull WrapperPlayClientPickItemFromEntity packet = new WrapperPlayClientPickItemFromEntity(event);
            int entityId = packet.getEntityId();
            boolean includeData = packet.isIncludeData();

            MessageUtil.debug(packetName
                    + " entityId=" + entityId
                    + " includeData=" + includeData
            );

            handlePacketChecks(playerData, event);
            return;
        }

        // Handles the rest of the packets.
        if (packetType != PacketType.Play.Client.KEEP_ALIVE
                && packetType != PacketType.Play.Client.RESOURCE_PACK_STATUS) {
            MessageUtil.debug("Unhandled packet: " + packetName);
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
                                           @NotNull ProtocolPacketEvent event) {
        List<Check> checks = playerData.getChecks();

        // Create a copy of the checks list to avoid ConcurrentModificationException
        if (checks != null) {
            @NotNull Iterable<Check> checksCopy = new ArrayList<>(checks);

            for (@NotNull Check check : checksCopy) {
                if (check.getCheckInfo().enabled() && event instanceof PacketPlayReceiveEvent) {
                    check.handle((PacketPlayReceiveEvent) event);
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
            @NotNull Iterable<Check> checksCopy = new ArrayList<>(checks);

            for (@NotNull Check check : checksCopy) {
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
            @NotNull Iterable<Check> checksCopy = new ArrayList<>(checks);

            for (@NotNull Check check : checksCopy) {
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
    private static void steerVehicleCheck(@NotNull Player player, @NotNull CancellableEvent event,
                                          @NotNull WrapperPlayClientSteerVehicle steerVehicle, float value) {
        if (Math.abs(value) == 0.98f) {
            if (steerVehicle.isUnmount()) {
                KickUtil.kickPlayer(player, event, Settings.steerVehicleInvalidDismountValue,
                        "Sent STEER_VEHICLE with invalid dismount value"
                );
            }
        } else if (Math.abs(value) == 0.29400003f) {
            if (!steerVehicle.isUnmount()) {
                KickUtil.kickPlayer(player, event, Settings.steerVehicleInvalidNonDismountValue,
                        "Sent STEER_VEHICLE with invalid non-dismount value"
                );
            }
        } else if (value != 0.0f) {
            KickUtil.kickPlayer(player, event, Settings.steerVehicleInvalidValue,
                    "Sent STEER_VEHICLE with invalid value"
            );
        }
    }
}
