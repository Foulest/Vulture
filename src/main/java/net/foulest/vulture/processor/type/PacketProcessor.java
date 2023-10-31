package net.foulest.vulture.processor.type;

import io.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.event.eventtypes.CancellableNMSPacketEvent;
import io.github.retrooper.packetevents.event.impl.PacketPlayReceiveEvent;
import io.github.retrooper.packetevents.event.impl.PacketPlaySendEvent;
import io.github.retrooper.packetevents.packettype.PacketType;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.play.in.abilities.WrappedPacketInAbilities;
import io.github.retrooper.packetevents.packetwrappers.play.in.blockdig.WrappedPacketInBlockDig;
import io.github.retrooper.packetevents.packetwrappers.play.in.blockplace.WrappedPacketInBlockPlace;
import io.github.retrooper.packetevents.packetwrappers.play.in.chat.WrappedPacketInChat;
import io.github.retrooper.packetevents.packetwrappers.play.in.clientcommand.WrappedPacketInClientCommand;
import io.github.retrooper.packetevents.packetwrappers.play.in.custompayload.WrappedPacketInCustomPayload;
import io.github.retrooper.packetevents.packetwrappers.play.in.enchantitem.WrappedPacketInEnchantItem;
import io.github.retrooper.packetevents.packetwrappers.play.in.entityaction.WrappedPacketInEntityAction;
import io.github.retrooper.packetevents.packetwrappers.play.in.flying.WrappedPacketInFlying;
import io.github.retrooper.packetevents.packetwrappers.play.in.helditemslot.WrappedPacketInHeldItemSlot;
import io.github.retrooper.packetevents.packetwrappers.play.in.setcreativeslot.WrappedPacketInSetCreativeSlot;
import io.github.retrooper.packetevents.packetwrappers.play.in.settings.WrappedPacketInSettings;
import io.github.retrooper.packetevents.packetwrappers.play.in.spectate.WrappedPacketInSpectate;
import io.github.retrooper.packetevents.packetwrappers.play.in.steervehicle.WrappedPacketInSteerVehicle;
import io.github.retrooper.packetevents.packetwrappers.play.in.tabcomplete.WrappedPacketInTabComplete;
import io.github.retrooper.packetevents.packetwrappers.play.in.transaction.WrappedPacketInTransaction;
import io.github.retrooper.packetevents.packetwrappers.play.in.updatesign.WrappedPacketInUpdateSign;
import io.github.retrooper.packetevents.packetwrappers.play.in.useentity.WrappedPacketInUseEntity;
import io.github.retrooper.packetevents.packetwrappers.play.in.windowclick.WrappedPacketInWindowClick;
import io.github.retrooper.packetevents.packetwrappers.play.out.abilities.WrappedPacketOutAbilities;
import io.github.retrooper.packetevents.packetwrappers.play.out.animation.WrappedPacketOutAnimation;
import io.github.retrooper.packetevents.packetwrappers.play.out.camera.WrappedPacketOutCamera;
import io.github.retrooper.packetevents.packetwrappers.play.out.entityvelocity.WrappedPacketOutEntityVelocity;
import io.github.retrooper.packetevents.packetwrappers.play.out.position.WrappedPacketOutPosition;
import io.github.retrooper.packetevents.packetwrappers.play.out.resourcepacksend.WrappedPacketOutResourcePackSend;
import io.github.retrooper.packetevents.packetwrappers.play.out.transaction.WrappedPacketOutTransaction;
import io.github.retrooper.packetevents.utils.player.ClientVersion;
import io.github.retrooper.packetevents.utils.player.Direction;
import io.github.retrooper.packetevents.utils.vector.Vector3d;
import io.github.retrooper.packetevents.utils.vector.Vector3f;
import io.github.retrooper.packetevents.utils.vector.Vector3i;
import lombok.Cleanup;
import lombok.NonNull;
import net.foulest.vulture.action.ActionType;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.data.PlayerDataManager;
import net.foulest.vulture.event.MovementEvent;
import net.foulest.vulture.event.RotationEvent;
import net.foulest.vulture.hamsterapi.HamsterAPI;
import net.foulest.vulture.hamsterapi.events.PacketDecodeEvent;
import net.foulest.vulture.processor.Processor;
import net.foulest.vulture.util.KickUtil;
import net.foulest.vulture.util.MessageUtil;
import net.foulest.vulture.util.TaskUtil;
import net.foulest.vulture.util.block.BlockUtil;
import net.foulest.vulture.util.data.Pair;
import net.foulest.vulture.util.raytrace.BoundingBox;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;

/**
 * Handles all incoming & outgoing packets.
 *
 * @author Foulest
 * @project Vulture
 */
public class PacketProcessor extends Processor {

    @EventHandler
    public void onPacketDecode(PacketDecodeEvent event) {
        PlayerData playerData = event.getPlayerData();
        Player player = playerData.getPlayer();
        int maxPacketsPerSecond = (player.isDead() ? 150 : 75);

        // Cancels incoming packets for offline players.
        if (!player.isOnline()) {
            event.setCancelled(true);
            return;
        }

        // Iterate packets sent per tick (resets on server transaction).
        int packetsSentInTick = playerData.getPacketsSentInTick();
        playerData.setPacketsSentInTick(packetsSentInTick + 1);
        packetsSentInTick++;

        // If the player has sent too many packets in one tick, kick them.
        if (packetsSentInTick >= maxPacketsPerSecond) {
            event.setCancelled(true);
            HamsterAPI.closeChannel(playerData);
            KickUtil.kickPlayer(player, "Sent too many packets in one tick"
                    + " (" + packetsSentInTick + ")", true);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onPacketPlayReceive(@NonNull PacketPlayReceiveEvent event) {
        if (Bukkit.getPlayer(event.getPlayer().getUniqueId()) == null) {
            event.setCancelled(true);
            return;
        }

        Player player = event.getPlayer();
        PlayerData playerData = PlayerDataManager.getPlayerData(player);

        if (!player.isOnline()) {
            event.setCancelled(true);
            return;
        }

        switch (event.getPacketId()) {
            case PacketType.Play.Client.ABILITIES:
                WrappedPacketInAbilities abilities = new WrappedPacketInAbilities(event.getNMSPacket());

                if (playerData.getVersion().isOlderThanOrEquals(ClientVersion.v_1_8)) {
                    if (abilities.isFlying() == playerData.isFlying()) {
                        KickUtil.kickPlayer(player, event, "Sent duplicate Abilities flying values");
                        return;
                    }

                    if (abilities.isFlying() && !playerData.isFlightAllowed()) {
                        KickUtil.kickPlayer(player, event, "Sent invalid Abilities flying values");
                        return;
                    }

                    if (abilities.isFlightAllowed().isPresent()) {
                        if (abilities.isFlightAllowed().get() != playerData.isFlightAllowed()) {
                            KickUtil.kickPlayer(player, event, "Sent invalid Abilities flight allowed values");
                            return;
                        }
                    }

                    if (abilities.canInstantlyBuild().isPresent()) {
                        if (abilities.canInstantlyBuild().get() != playerData.isInstantBuild()) {
                            KickUtil.kickPlayer(player, event, "Sent invalid Abilities instant build values");
                            return;
                        }
                    }

                    if (abilities.isVulnerable().isPresent()) {
                        if (abilities.isVulnerable().get() != playerData.isVulnerable()) {
                            KickUtil.kickPlayer(player, event, "Sent invalid Abilities invulnerable values");
                            return;
                        }
                    }

                    if (abilities.getFlySpeed().isPresent()) {
                        if (!playerData.isFlightAllowed() && abilities.getFlySpeed().get() != playerData.getFlySpeed()) {
                            KickUtil.kickPlayer(player, event, "Sent invalid Abilities fly speed values");
                            return;
                        }
                    }

                    if (abilities.getWalkSpeed().isPresent()) {
                        if (abilities.getWalkSpeed().get() != playerData.getWalkSpeed()) {
                            KickUtil.kickPlayer(player, event, "Sent invalid Abilities walk speed values");
                        }
                    }
                }

                playerData.setFlying(abilities.isFlying());
                abilities.isFlightAllowed().ifPresent(playerData::setFlightAllowed);
                abilities.canInstantlyBuild().ifPresent(playerData::setInstantBuild);
                abilities.isVulnerable().ifPresent(playerData::setVulnerable);
                abilities.getFlySpeed().ifPresent(playerData::setFlySpeed);
                abilities.getWalkSpeed().ifPresent(playerData::setWalkSpeed);
                break;

            case PacketType.Play.Client.ARM_ANIMATION:
                if (playerData.isInventoryOpen()) {
                    KickUtil.kickPlayer(player, event, "Sent invalid ArmAnimation packet");
                    return;
                }


                playerData.setTimestamp(ActionType.ARM_ANIMATION);
                break;

            case PacketType.Play.Client.BLOCK_DIG:
                WrappedPacketInBlockDig blockDig = new WrappedPacketInBlockDig(event.getNMSPacket());
                Vector3i digBlockPosition = blockDig.getBlockPosition();
                Direction digDirection = blockDig.getDirection();
                double digXPos = digBlockPosition.getX();
                double digYPos = digBlockPosition.getY();
                double digZPos = digBlockPosition.getZ();

                if (blockDig.getDigType() == null) {
                    break;
                }

                if (digXPos != 0 && digYPos != 0 && digZPos != 0
                        && digXPos != -1 && digYPos != -1 && digZPos != -1) {
                    Location playerLocation = player.getLocation();
                    Location blockLocation = new Location(player.getWorld(), digXPos, digYPos, digZPos);
                    double distance = playerLocation.distance(blockLocation);

                    if (distance > 6.2) {
                        KickUtil.kickPlayer(player, event, "Sent BlockDig packet with invalid distance: "
                                + distance + " (x=" + digXPos + " y=" + digYPos + " z=" + digZPos + ")");
                        return;
                    }
                }

                switch (blockDig.getDigType()) {
                    case STOP_DESTROY_BLOCK:
                    case ABORT_DESTROY_BLOCK:
                        playerData.setDigging(false);
                        break;

                    case RELEASE_USE_ITEM:
                        if (!(digDirection == Direction.DOWN
                                && digXPos == 0 && digYPos == 0 && digZPos == 0)) {
                            KickUtil.kickPlayer(player, event, "Sent ReleaseUseItem packet with invalid data");
                            return;
                        }

                        if (!playerData.isBlocking() && !playerData.isShootingBow()
                                && !playerData.isEating() && !playerData.isDrinking()) {
                            KickUtil.kickPlayer(player, event, "Sent invalid ReleaseUseItem packet");
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
                            KickUtil.kickPlayer(player, event, "Sent DropItem packet with invalid data");
                            return;
                        }
                        break;

                    case SWAP_ITEM_WITH_OFFHAND: // Not present in 1.8.9
                        break;

                    case START_DESTROY_BLOCK: // Ignored; digging set in Bukkit events
                        break;
                }
                break;

            case PacketType.Play.Client.BLOCK_PLACE:
                WrappedPacketInBlockPlace blockPlace = new WrappedPacketInBlockPlace(event.getNMSPacket());
                Vector3i placeBlockPosition = blockPlace.getBlockPosition();
                Direction placeDirection = blockPlace.getDirection();
                double placeXPos = placeBlockPosition.getX();
                double placeYPos = placeBlockPosition.getY();
                double placeZPos = placeBlockPosition.getZ();

                if (playerData.isInventoryOpen()) {
                    KickUtil.kickPlayer(player, event, "Sent invalid BlockPlace packet");
                    return;
                }

                if (placeXPos != -1 && placeYPos != -1 && placeZPos != -1) {
                    Location blockLocation = new Location(player.getWorld(), placeXPos, placeYPos, placeZPos);
                    double distance = player.getLocation().distance(blockLocation);

                    if (distance > 6.2) {
                        KickUtil.kickPlayer(player, event, "Sent BlockPlace packet with invalid distance: "
                                + distance);
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
                        KickUtil.kickPlayer(player, event, "Sent BlockPlace packet with invalid cursor position");
                        return;
                    }

                    if (placeDirection == Direction.OTHER
                            && (cursorPosX != 0.0
                            || cursorPos.getY() != 0.0
                            || cursorPos.getZ() != 0.0)) {
                        KickUtil.kickPlayer(player, event, "Sent BlockPlace packet OTHER with invalid cursor position");
                        return;
                    }
                }

                if (placeDirection == Direction.UP
                        && placeXPos == 0.0 && placeYPos == 0.0 && placeZPos == 0.0) {
                    KickUtil.kickPlayer(player, event, "Sent BlockPlace packet UP with invalid block position");
                    return;
                }

                if (placeDirection == Direction.OTHER
                        && placeXPos != -1.0 && placeYPos != -1.0 && placeZPos != -1.0) {
                    KickUtil.kickPlayer(player, event, "Sent BlockPlace packet OTHER with invalid block position");
                    return;
                }

                if (blockPlace.getItemStack().isPresent()) {
                    ItemStack itemStack = blockPlace.getItemStack().get();

                    if (itemStack.getType() != Material.AIR && !player.getInventory().contains(itemStack)) {
                        KickUtil.kickPlayer(player, event, "Sent BlockPlace packet with invalid item"
                                + " (" + itemStack + ")"
                                + " (" + Arrays.toString(player.getInventory().getContents()) + ")");
                        return;
                    }

                    if (blockPlace.getDirection() == Direction.OTHER) {
                        switch (itemStack.getType()) {
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
                                playerData.setDrinking(true);
                                break;

                            case BOW:
                                playerData.setShootingBow(true);
                                break;
                        }
                    } else if (itemStack.getType().isBlock() && itemStack.getType() != Material.AIR) {
                        playerData.setPlacingBlock(true);
                        playerData.setTimestamp(ActionType.PLACING_BLOCK);
                    }
                }
                break;

            case PacketType.Play.Client.CHAT:
                WrappedPacketInChat chat = new WrappedPacketInChat(event.getNMSPacket());

                if (playerData.isBlocking()
                        || playerData.isShootingBow()
                        || playerData.isEating()
                        || playerData.isDrinking()
                        || playerData.isInventoryOpen()
                        || playerData.isDigging()
                        || playerData.isPlacingBlock()
                        || chat.getMessage().isEmpty()) {
                    KickUtil.kickPlayer(player, event, "Sent invalid Chat packet"
                            + " (blocking=" + playerData.isBlocking()
                            + " shootingBow=" + playerData.isShootingBow()
                            + " eating=" + playerData.isEating()
                            + " drinking=" + playerData.isDrinking()
                            + " inventoryOpen=" + playerData.isInventoryOpen()
                            + " digging=" + playerData.isDigging()
                            + " placingBlock=" + playerData.isPlacingBlock()
                            + " message=" + chat.getMessage() + ")");
                    return;
                }

                playerData.setTimestamp(ActionType.CHATTING);
                break;

            case PacketType.Play.Client.CLIENT_COMMAND:
                WrappedPacketInClientCommand clientCommand = new WrappedPacketInClientCommand(event.getNMSPacket());

                switch (clientCommand.getClientCommand()) {
                    case OPEN_INVENTORY_ACHIEVEMENT:
                        if (playerData.isInventoryOpen()) {
                            KickUtil.kickPlayer(player, event, "Sent invalid OpenInventory packet");
                            return;
                        }

                        playerData.setInventoryOpen(true);
                        playerData.setTimestamp(ActionType.INVENTORY_OPEN);
                        break;

                    case PERFORM_RESPAWN:
                        if (playerData.isShootingBow()
                                || playerData.isEating()
                                || playerData.isDrinking()
                                || playerData.isInventoryOpen()
                                || playerData.isPlacingBlock()
                                || playerData.isBlocking()
                                || playerData.isDigging()
                                || !player.isDead()) {
                            KickUtil.kickPlayer(player, event, "Sent invalid Respawn packet");
                            return;
                        }

                        playerData.setTimestamp(ActionType.RESPAWN);
                        break;

                    case REQUEST_STATS: // Ignored
                        break;
                }
                break;

            case PacketType.Play.Client.CLOSE_WINDOW:
                if (playerData.isShootingBow()
                        || playerData.isEating()
                        || playerData.isDrinking()
                        || playerData.isPlacingBlock()
                        || playerData.isBlocking()
                        || playerData.isDigging()) {
                    KickUtil.kickPlayer(player, event, "Sent invalid CloseWindow packet");
                    return;
                }

                if (!playerData.isInventoryOpen() && playerData.getVersion().isOlderThanOrEquals(ClientVersion.v_1_8)) {
                    KickUtil.kickPlayer(player, event, "Sent CloseWindow packet with closed inventory");
                    return;
                }

                playerData.setInventoryOpen(false);
                playerData.setTimestamp(ActionType.CLOSE_WINDOW);
                break;

            case PacketType.Play.Client.CUSTOM_PAYLOAD:
                WrappedPacketInCustomPayload customPayload = new WrappedPacketInCustomPayload(event.getNMSPacket());
                String channelName = customPayload.getChannelName();
                byte[] rawData = customPayload.getData();
                int payloadSize = rawData.length;
                String data = new String(rawData, StandardCharsets.UTF_8);

                // Checks for payloads with invalid sizes.
                if (payloadSize > 15000 || payloadSize == 0) {
                    KickUtil.kickPlayer(player, event, "Sent payload with invalid size");
                    return;
                }

                // Checks for invalid item name payloads.
                if (channelName.equals("MC|ItemName")) {
                    if (payloadSize > (playerData.getVersion().isOlderThanOrEquals(ClientVersion.v_1_8) ? 31 : 32)) {
                        KickUtil.kickPlayer(player, event, "Sent item name payload with invalid size: " + payloadSize);
                        return;
                    }

                    if (!data.startsWith("\b") && !data.startsWith("\t") && !data.startsWith("\n")
                            && !data.startsWith("\u0000") && !data.startsWith("\u0001")
                            && !data.startsWith("\u0002") && !data.startsWith("\u0003")
                            && !data.startsWith("\u0004") && !data.startsWith("\u0005")
                            && !data.startsWith("\u0006") && !data.startsWith("\u0007")
                            && !data.startsWith("\u000B") && !data.startsWith("\f")
                            && !data.startsWith("\r") && !data.startsWith("\u000E")
                            && !data.startsWith("\u000F") && !data.startsWith("\u0010")
                            && !data.startsWith("\u0011") && !data.startsWith("\u0012")
                            && !data.startsWith("\u0013") && !data.startsWith("\u0014")
                            && !data.startsWith("\u0015") && !data.startsWith("\u0016")
                            && !data.startsWith("\u0017") && !data.startsWith("\u0018")
                            && !data.startsWith("\u0019") && !data.startsWith("\u001A")
                            && !data.startsWith("\u001B") && !data.startsWith("\u001C")
                            && !data.startsWith("\u001D") && !data.startsWith("\u001E")) {
                        KickUtil.kickPlayer(player, event, "Sent invalid item name payload");

                        try {
                            @Cleanup BufferedWriter writer = new BufferedWriter(new FileWriter("test.txt"));
                            writer.write(data);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                        return;
                    }
                }

                // Checks for invalid trade select payloads.
                if (channelName.equals("MC|TrSel")
                        && !data.equals("\u0000\u0000\u0000\u0000")
                        && !data.equals("\u0000\u0000\u0000\u0001")
                        && !data.equals("\u0000\u0000\u0000\u0002")) {
                    KickUtil.kickPlayer(player, event, "Sent invalid trade select payload");
                    return;
                }

                // Checks for invalid beacon payloads.
                if (channelName.equals("MC|Beacon")) {
                    if (data.startsWith("\u0000\u0000\u0000")
                            && data.endsWith("\u0000\u0000\u0000\u0000")) {
                        if (data.charAt(2) != '\u0001'
                                && data.charAt(2) != '\u0003'
                                && data.charAt(2) != '\u0005'
                                && data.charAt(2) != '\u0008'
                                && data.charAt(2) != '\u0010'
                                && data.charAt(2) != '\u0011') {
                            KickUtil.kickPlayer(player, event, "Sent beacon payload with invalid effect");
                            return;
                        }
                    } else if (!data.equals("\u0000\u0000\u0000\u0001\u0000\u0000\u0000\u0001")) {
                        KickUtil.kickPlayer(player, event, "Sent invalid beacon payload");
                        return;
                    }
                }

                // Checks for invalid book-related payloads.
                if (channelName.equals("MC|BOpen")
                        || channelName.equals("MC|BEdit")
                        || channelName.equals("MC|BSign")) {

                    // Checks for invalid book open payloads.
                    // (The client doesn't send this payload; the server does)
                    if (channelName.equals("MC|BOpen")) {
                        KickUtil.kickPlayer(player, event, "Sent invalid book open payload");
                        return;
                    }

                    // Checks for invalid book edit payloads.
                    if (channelName.equals("MC|BEdit")) {
                        if (!data.startsWith("\u0001ï¿½\u0001\u0000\u0000\n\u0000\u0000\t\u0000\u0005pages\b"
                                + "\u0000\u0000\u0000\u0001\u0000\f")
                                && !data.endsWith("\u0000")) {
                            KickUtil.kickPlayer(player, event, "Sent invalid book edit payload");
                            return;
                        }
                    }

                    // Checks for invalid book sign payloads.
                    if (channelName.equals("MC|BSign")) {
                        if (!data.startsWith("\u0001ï¿½\u0001\u0000\u0000\n\u0000\u0000\t\u0000\u0005pages\b"
                                + "\u0000\u0000\u0000\u0001\u0000\f")
                                && !data.endsWith("\u0000")
                                && !data.contains("\b\u0000\u0006author\u0000\u0007")
                                && !data.contains("\b\u0000\u0005title\u0000\u0004")) {
                            KickUtil.kickPlayer(player, event, "Sent invalid book sign payload");
                            return;
                        }
                    }

                    ItemStack itemInHand = playerData.getPlayer().getInventory().getItemInHand();

                    if (itemInHand != null && !itemInHand.getType().toString().toLowerCase().contains("book")) {
                        KickUtil.kickPlayer(player, event, "Sent book payload without a book");
                        return;
                    }
                }

                if (channelName.equals("MC|AdvCdm") && !player.isOp()) {
                    KickUtil.kickPlayer(player, event, "Sent command block minecart payload without being an operator");
                    return;
                }

                if (channelName.equals("MC|AutoCmd") && !player.isOp()) {
                    KickUtil.kickPlayer(player, event, "Sent command block minecart payload without being an operator");
                    return;
                }

                playerData.setTimestamp(ActionType.SENT_PAYLOAD);
                break;

            case PacketType.Play.Client.DIFFICULTY_CHANGE: // Players shouldn't be able to send this packet
                event.setCancelled(true);
                return;

            case PacketType.Play.Client.ENTITY_ACTION:
                WrappedPacketInEntityAction entityAction = new WrappedPacketInEntityAction(event.getNMSPacket());
                WrappedPacketInEntityAction.PlayerAction playerAction = entityAction.getAction();
                int jumpBoost = entityAction.getJumpBoost();

                if (playerAction != WrappedPacketInEntityAction.PlayerAction.RIDING_JUMP && jumpBoost != 0) {
                    KickUtil.kickPlayer(player, event, "Send EntityAction packet with invalid jump boost");
                    return;
                }

                switch (playerAction) {
                    case START_SPRINTING:
                        if ((playerData.isBlocking() && playerData.getVersion().isOlderThanOrEquals(ClientVersion.v_1_8))
                                || playerData.isShootingBow()
                                || playerData.isEating()
                                || playerData.isDrinking()
                                || player.hasPotionEffect(PotionEffectType.BLINDNESS)) {
                            KickUtil.kickPlayer(player, event, "Sent invalid Start Sprinting packet");
                            return;
                        }

                        playerData.setSprinting(true);
                        playerData.setTimestamp(ActionType.SPRINTING);
                        break;

                    case STOP_SPRINTING:
                        playerData.setSprinting(false);
                        playerData.setTimestamp(ActionType.SPRINTING);
                        break;

                    case START_SNEAKING:
                        if (playerData.isInventoryOpen()
                                || playerData.isSneaking()) {
                            KickUtil.kickPlayer(player, event, "Sent invalid Start Sneaking packet");
                            return;
                        }

                        playerData.setSneaking(true);
                        playerData.setTimestamp(ActionType.SNEAKING);
                        break;

                    case STOP_SNEAKING:
                        if (!playerData.isSneaking()) {
                            KickUtil.kickPlayer(player, event, "Sent invalid Stop Sneaking packet");
                            return;
                        }

                        playerData.setSneaking(false);
                        playerData.setTimestamp(ActionType.SNEAKING);
                        break;

                    case OPEN_INVENTORY:
                        if (player.isInsideVehicle()) {
                            Entity vehicle = player.getVehicle();

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
                        if (playerData.isInventoryOpen()
                                || !playerData.isInBed()) {
                            KickUtil.kickPlayer(player, event, "Sent invalid Stop Sleeping packet");
                            return;
                        }

                        playerData.setInBed(false);
                        playerData.setTimestamp(ActionType.IN_BED);
                        break;

                    case RIDING_JUMP:
                    case START_RIDING_JUMP:
                    case STOP_RIDING_JUMP:
                        if (playerData.isInventoryOpen()
                                || !player.isInsideVehicle()
                                || player.getVehicle().getType() != EntityType.HORSE
                                || jumpBoost < 0 || jumpBoost > 100) {
                            KickUtil.kickPlayer(player, event, "Sent invalid Riding Jump packet");
                            return;
                        }
                        break;
                }
                break;

            case PacketType.Play.Client.ENCHANT_ITEM:
                WrappedPacketInEnchantItem enchantItem = new WrappedPacketInEnchantItem(event.getNMSPacket());
                int enchantItemWindowId = enchantItem.getWindowId();

                if (enchantItemWindowId < 0 || enchantItemWindowId > 2) {
                    KickUtil.kickPlayer(player, event, "Sent EnchantItem packet with invalid window ID");
                    return;
                }
                break;

            case PacketType.Play.Client.POSITION:
            case PacketType.Play.Client.POSITION_LOOK:
            case PacketType.Play.Client.LOOK:
            case PacketType.Play.Client.FLYING:
                WrappedPacketInFlying flying = new WrappedPacketInFlying(event.getNMSPacket());
                Vector3d flyingPosition = flying.getPosition();
                double flyingXPos = flyingPosition.getX();
                double flyingYPos = flyingPosition.getY();
                double flyingZPos = flyingPosition.getZ();
                float flyingYaw = flying.getYaw();
                float flyingPitch = flying.getPitch();
                long timeSinceFlying = playerData.getTimeSince(ActionType.FLYING_PACKET);

                playerData.setDroppedPackets(playerData.getTotalTicks() - playerData.getLastDroppedPackets() < 2);

                if (timeSinceFlying > 300L) {
                    playerData.setTimestamp(ActionType.LAG2);
                }

                if (timeSinceFlying > 150L) {
                    playerData.setTimestamp(ActionType.LAG);
                }

                if (timeSinceFlying >= 110L) {
                    playerData.setTimestamp(ActionType.DELAYED_PACKET);
                }

                if (timeSinceFlying < 25L) {
                    playerData.setTimestamp(ActionType.FAST);
                }

                playerData.setTotalTicks(playerData.getTotalTicks() + 1);
                playerData.setLastAttackTick(playerData.getLastAttackTick() + 1);
                playerData.setLastServerPositionTick(playerData.getLastServerPositionTick() + 1);

                // Handles teleport resets.
                if (flying.isMoving()) {
                    if (playerData.isTeleportReset()) {
                        playerData.setTeleportReset(false);
                        playerData.setLastTeleportPacket(null);
                        playerData.setLastServerPositionTick(600);
                        playerData.setLastTeleportReset(playerData.getTotalTicks());
                    }

                    if (playerData.getLastTeleportPacket() != null) {
                        Vector3d lastTPPos = playerData.getLastTeleportPacket().getPosition();

                        if (flyingPosition.distanceSquared(lastTPPos) <= 0.005 && playerData.getTotalTicks() > 100) {
                            playerData.setTeleportReset(true);
                        }
                    }
                }

                // Handles setting velocity data.
                if (playerData.getVelocityH() > 0 || playerData.getVelocityV() > 0) {
                    playerData.setVelocityH(playerData.getVelocityH() - 1);
                    playerData.setVelocityV(playerData.getVelocityV() - 1);
                } else if (playerData.getVelocityIds().isEmpty()) {
                    playerData.setVelocityHorizontal(0);
                    playerData.setVelocityY(0);
                }

                // Handles dropped packets.
                if (playerData.getTotalTicks() - playerData.getLastDroppedPackets() < 2) {
                    playerData.setLastPacketDrop(playerData.getTotalTicks());
                }

                // Handles setting target data.
                Player target = playerData.getLastTarget();
                if (target != null && target.isOnline()) {
                    double x1 = ((int) (target.getLocation().getX() * 32.0)) / 32.0;
                    double y1 = ((int) (target.getLocation().getY() * 32.0)) / 32.0;
                    double z1 = ((int) (target.getLocation().getZ() * 32.0)) / 32.0;

                    BoundingBox box = BoundingBox.getEntityBoundingBox(x1, y1, z1);

                    double expandValue = playerData.getVersion().isNewerThan(ClientVersion.v_1_8)
                            ? 0.03 : (playerData.getLastPosition() > 0) ? 0.13 : 0.1;
                    BoundingBox axisalignedbb = box.expand(expandValue);

                    expandValue = playerData.getVersion().isNewerThan(ClientVersion.v_1_8)
                            ? 0.23 : (playerData.getLastPosition2() > 0) ? 0.33 : 0.3;
                    BoundingBox axisalignedbb2 = box.expand(expandValue);

                    if (playerData.getLastTeleportPacket() == null && !player.isInsideVehicle() && !target.isInsideVehicle()) {
                        playerData.getPastLocsA().add(new Pair<>(axisalignedbb, playerData.getTotalTicks()));
                        playerData.getPastLocsB().add(new Pair<>(axisalignedbb, playerData.getTotalTicks()));
                        playerData.getPastLocsC().add(new Pair<>(axisalignedbb2, playerData.getTotalTicks()));
                    } else {
                        playerData.getPastLocsA().clear();
                        playerData.getPastLocsB().clear();
                        playerData.getPastLocsC().clear();
                    }
                }

                // Handles invalid Y data.
                if (Math.abs(flyingYPos) > 1.0E9) {
                    KickUtil.kickPlayer(player, event, "Sent Flying packet with invalid Y data");
                    return;
                }

                // Handles empty Flying packets.
                if (!flying.isMoving() && !flying.isRotating()) {
                    if (!(flyingXPos == 0 && flyingYPos == 0 && flyingZPos == 0)) {
                        KickUtil.kickPlayer(player, event, "Sent Flying packet with invalid position data");
                        return;
                    }

                    if (!(flyingYaw == 0.0 && flyingPitch == 0.0)) {
                        KickUtil.kickPlayer(player, event, "Sent Flying packet with invalid rotation data");
                        return;
                    }
                }

                // Handles setting last locations.
                Location currentLocation = playerData.getLocation();

                if (currentLocation == null) {
                    currentLocation = player.getLocation();
                }

                Location location = new Location(player.getWorld(), flyingXPos, flyingYPos, flyingZPos,
                        currentLocation.getYaw(), currentLocation.getPitch());

                playerData.setLastLastLastLocation(playerData.getLastLastLocation());
                playerData.setLastLastLocation(playerData.getLastLocation());
                playerData.setLastLocation(currentLocation);
                playerData.setLocation(location);

                // Handles player rotations.
                if (flying.isRotating()) {
                    if (Math.abs(flyingPitch) > 90.0) {
                        KickUtil.kickPlayer(player, event, "Sent Flying packet with invalid pitch");
                        return;
                    }

                    if (playerData.getLastRotationPacket() != null) {
                        WrappedPacketInFlying from = playerData.getLastRotationPacket();

                        // Checks if the player has rotated.
                        if (Math.abs(flyingPitch - from.getPitch()) != 0.0
                                || Math.abs(flyingYaw - from.getYaw()) != 0.0) {
                            // Ignores teleport packets.
                            handleRotationChecks(playerData, new RotationEvent(flying, playerData.getLastRotationPacket()));
                        }
                    }

                    playerData.setLastRotationPacket(flying);
                }

                // Handles player movement.
                if (flying.isMoving()) {
                    if (player.isInsideVehicle() && !player.getVehicle().isValid()) {
                        event.setCancelled(true);
                        player.getVehicle().eject();
                        MessageUtil.log(Level.INFO, "Flying packet ignored for " + player.getName() + " (invalid vehicle)");
                    }

                    // Set ground data
                    playerData.setLastLastOnGroundPacket(playerData.isLastOnGroundPacket());
                    playerData.setLastOnGroundPacket(playerData.isOnGroundPacket());
                    playerData.setOnGroundPacket(flying.isOnGround());
                    playerData.setLastOnGround(playerData.isNearGround());
                    playerData.setOnGround(BlockUtil.isOnGroundOffset(player, 0.001));
                    playerData.setNearGround(BlockUtil.isOnGroundOffset(player, 0.21)); // Normally 0.105

                    // Sets non-strict ground data.
                    if (playerData.isNearGround()) {
                        playerData.setAirTicks(0);
                        playerData.setGroundTicks(playerData.getGroundTicks() + 1);

                        if (playerData.getAboveBlockTicks() < 60) {
                            playerData.setAboveBlockTicks(playerData.getAboveBlockTicks() + 1);
                        }

                    } else {
                        playerData.setAirTicks(playerData.getAirTicks() + 1);
                        playerData.setGroundTicks(0);

                        if (playerData.getAboveBlockTicks() > 0) {
                            playerData.setAboveBlockTicks(playerData.getAboveBlockTicks() - 1);
                        }
                    }

                    // Sets strict ground data.
                    if (playerData.isOnGround()) {
                        playerData.setAirTicksStrict(0);
                        playerData.setGroundTicksStrict(playerData.getGroundTicksStrict() + 1);

                        if (playerData.getAboveBlockTicksStrict() < 60) {
                            playerData.setAboveBlockTicksStrict(playerData.getAboveBlockTicksStrict() + 1);
                        }

                    } else {
                        playerData.setAirTicksStrict(playerData.getAirTicksStrict() + 1);
                        playerData.setGroundTicksStrict(0);

                        if (playerData.getAboveBlockTicksStrict() > 0) {
                            playerData.setAboveBlockTicksStrict(playerData.getAboveBlockTicksStrict() - 1);
                        }
                    }

                    // Set block data
                    playerData.setOnSlab(BlockUtil.isOnSlab(player));
                    playerData.setOnStairs(BlockUtil.isOnStairs(player));
                    playerData.setNearPiston(BlockUtil.isNearPiston(player));
                    playerData.setNearCactus(BlockUtil.isNearCactus(player));
                    playerData.setInWeb(BlockUtil.isInWeb(player));
                    playerData.setInLiquid(BlockUtil.isInLiquid(player));
                    playerData.setNearLiquid(BlockUtil.isNearLiquid(player));
                    playerData.setOnChest(BlockUtil.isOnChest(player));
                    playerData.setOnClimbable(BlockUtil.isOnClimbable(player));
                    playerData.setNearClimbable(BlockUtil.isNearClimbable(player));
                    playerData.setOnSnowLayer(BlockUtil.isOnSnowLayer(player));

                    playerData.setOnIce(BlockUtil.isOnIce(player));
                    playerData.setTimestamp(ActionType.ON_ICE);

                    playerData.setOnSoulSand(BlockUtil.isOnSoulSand(player));
                    playerData.setNearTrapdoor(BlockUtil.isNearTrapdoor(player));
                    playerData.setNearFenceGate(BlockUtil.isNearFenceGate(player));
                    playerData.setNearLilyPad(BlockUtil.isNearLilyPad(player));
                    playerData.setNearAnvil(BlockUtil.isNearAnvil(player));
                    playerData.setNearSlimeBlock(BlockUtil.isNearSlimeBlock(player));

                    if (BlockUtil.isUnderBlock(player)) {
                        playerData.setUnderBlockTicks(playerData.getUnderBlockTicks() + 1);
                        playerData.setUnderBlock(true);
                        playerData.setTimestamp(ActionType.UNDER_BLOCK);
                    } else {
                        playerData.setUnderBlockTicks(0);
                        playerData.setUnderBlock(false);
                    }

                    playerData.setAgainstBlock(BlockUtil.isAgainstBlock(player));
                    playerData.setCollidingBlock(BlockUtil.getCollidingBlock(player));

                    // Sets the last on ground location.
                    if (playerData.isOnGround() && !playerData.isInsideBlock()
                            && flyingYPos % 0.015625 == 0.0
                            && !BlockUtil.isLocationInUnloadedChunk(location)
                            && location.getBlock().isEmpty()
                            && playerData.getTimeSince(ActionType.SETBACK) > 1000L
                            && playerData.getTimeSince(ActionType.LAST_ON_GROUND_LOCATION_SET) > 500L) {
                        playerData.setTimestamp(ActionType.LAST_ON_GROUND_LOCATION_SET);
                        playerData.setLastOnGroundLocation(location);
                    }

                    if (playerData.getLastPositionPacket() != null) {
                        WrappedPacketInFlying from = playerData.getLastPositionPacket();
                        Vector3d fromPosition = from.getPosition();

                        // Checks if the player has moved.
                        if (Math.abs(flyingXPos - fromPosition.getX()) != 0.0
                                || Math.abs(flyingYPos - fromPosition.getY()) != 0.0
                                || Math.abs(flyingZPos - fromPosition.getZ()) != 0.0) {

                            // Ignores teleport packets.
                            playerData.setMoving(true);
                            handleMovementChecks(playerData, new MovementEvent(playerData,
                                    flying, playerData.getLastPositionPacket(), event));
                        } else {
                            playerData.setMoving(false);
                        }
                    }

                    playerData.setLastPositionPacket(flying);
                }

                playerData.setPlacingBlock(false);
                playerData.setLastDroppedPackets(playerData.getTotalTicks() - playerData.getLastFlyingTicks() > 2
                        ? playerData.getTotalTicks() : playerData.getLastDroppedPackets());
                playerData.setLastFlyingTicks(playerData.getTotalTicks());
                playerData.setTimestamp(ActionType.FLYING_PACKET);
                break;

            case PacketType.Play.Client.HELD_ITEM_SLOT:
                WrappedPacketInHeldItemSlot heldItemSlot = new WrappedPacketInHeldItemSlot(event.getNMSPacket());
                int currentSlot = heldItemSlot.getCurrentSelectedSlot();

                if (playerData.isInventoryOpen() && playerData.getTimeSince(ActionType.INVENTORY_OPEN) > 100) {
                    KickUtil.kickPlayer(player, event, "Sent invalid HeldItemSlot packet ("
                            + playerData.getTimeSince(ActionType.INVENTORY_OPEN) + ")");
                    return;
                }

                if (currentSlot == playerData.getCurrentSlot()
                        && playerData.getVersion().isOlderThanOrEquals(ClientVersion.v_1_8)
                        && playerData.getTimeSince(ActionType.LOGIN) > 1000) {
                    KickUtil.kickPlayer(player, event, "Sent HeldItemSlot packet with no slot change");
                    return;
                }

                if (currentSlot < 0 || currentSlot > 8) {
                    KickUtil.kickPlayer(player, event, "Sent HeldItemSlot packet with invalid slot: " + currentSlot);
                    return;
                }

                playerData.setCurrentSlot(currentSlot);
                playerData.setTimestamp(ActionType.CHANGE_SLOT);
                break;

            case PacketType.Play.Client.SET_CREATIVE_SLOT:
                WrappedPacketInSetCreativeSlot setCreativeSlot = new WrappedPacketInSetCreativeSlot(event.getNMSPacket());
                int creativeSlot = setCreativeSlot.getSlot();

                if (player.getGameMode() != GameMode.CREATIVE) {
                    KickUtil.kickPlayer(player, event, "Sent invalid SetCreativeSlot packet");
                    return;
                }

                if (creativeSlot < -1 || creativeSlot > 44) {
                    KickUtil.kickPlayer(player, event, "Sent SetCreativeSlot packet with invalid slot: " + creativeSlot);
                    return;
                }
                break;

            case PacketType.Play.Client.SETTINGS:
                WrappedPacketInSettings settings = new WrappedPacketInSettings(event.getNMSPacket());
                int localeLength = settings.getLocale().length();
                int viewDistance = settings.getViewDistance();

                if (localeLength < 3 || localeLength > 8) {
                    KickUtil.kickPlayer(player, event, "Sent Settings packet with invalid locale");
                    return;
                }

                if (viewDistance < 2 || viewDistance > 48) {
                    KickUtil.kickPlayer(player, event, "Sent Settings packet with invalid view distance");
                    return;
                }
                break;

            case PacketType.Play.Client.SPECTATE:
                WrappedPacketInSpectate spectate = new WrappedPacketInSpectate(event.getNMSPacket());

                if (player.getGameMode() != GameMode.SPECTATOR
                        || Bukkit.getServer().getPlayer(spectate.getUUID()) == null) {
                    KickUtil.kickPlayer(player, event, "Sent invalid Spectate packet");
                    return;
                }
                break;

            case PacketType.Play.Client.STEER_VEHICLE:
                WrappedPacketInSteerVehicle steerVehicle = new WrappedPacketInSteerVehicle(event.getNMSPacket());
                float sidewaysValue = steerVehicle.getSideValue();
                float forwardValue = steerVehicle.getForwardValue();

                // Ignores horse dismount packets.
                if (sidewaysValue == 0.0f && forwardValue == 0.0f
                        && !steerVehicle.isJump() && !player.isInsideVehicle()) {
                    if (player.getNearbyEntities(3, 3, 3).stream().anyMatch(entity -> entity.getType() == EntityType.HORSE)) {
                        playerData.setTimestamp(ActionType.STEER_VEHICLE);
                        return;
                    }

                    KickUtil.kickPlayer(player, event, "Sent invalid SteerVehicle packet");
                    return;

                } else if (!player.isInsideVehicle()) {
                    KickUtil.kickPlayer(player, event, "Sent invalid SteerVehicle packet");
                    return;
                }

                // Checks for invalid sideways & forward values
                steerVehicleCheck(player, event, steerVehicle, sidewaysValue);
                steerVehicleCheck(player, event, steerVehicle, forwardValue);

                playerData.setTimestamp(ActionType.STEER_VEHICLE);
                break;

            case PacketType.Play.Client.TAB_COMPLETE:
                WrappedPacketInTabComplete tabComplete = new WrappedPacketInTabComplete(event.getNMSPacket());

                if (tabComplete.getText().isEmpty()) {
                    KickUtil.kickPlayer(player, event, "Sent invalid TabComplete packet");
                    return;
                }
                break;

            case PacketType.Play.Client.TRANSACTION:
                WrappedPacketInTransaction transaction = new WrappedPacketInTransaction(event.getNMSPacket());
                int transactionWindowId = transaction.getWindowId();
                short transactionActionNumber = transaction.getActionNumber();

                // If the client has sent a Transaction packet that was not accepted, kick them.
                if (!transaction.isAccepted()) {
                    KickUtil.kickPlayer(player, event, "Sent a Transaction packet that was not accepted");
                    return;
                }

                // If the client has sent a Transaction packet with an invalid window id, kick them.
                if (transactionWindowId != 0 && !playerData.isInventoryOpen()) {
                    KickUtil.kickPlayer(player, event, "Sent a Transaction packet with an invalid window id: " + transactionWindowId);
                    return;
                }

                playerData.setPacketsSentInTick(0);

                if (playerData.getVelocityIds().containsKey(transactionActionNumber)) {
                    Vector velocity = playerData.getVelocityIds().get(transactionActionNumber);
                    playerData.setVelocityH((int) (((velocity.getX() + velocity.getZ()) / 2.0D + 2.0D) * 15.0D));
                    playerData.setVelocityV((int) (Math.pow(velocity.getY() + 2.0D, 2.0D) * 5.0D));
                    playerData.setLastVelocityX(playerData.getVelocityX());
                    playerData.setLastVelocityY(playerData.getVelocityY());
                    playerData.setLastVelocityZ(playerData.getVelocityZ());
                    playerData.setVelocityX(velocity.getX());
                    playerData.setVelocityY(velocity.getY());
                    playerData.setVelocityZ(velocity.getZ());
                    playerData.setVelocityTicks(playerData.getTotalTicks());
                    playerData.setVelocityHorizontal(Math.hypot(velocity.getX(), velocity.getZ()));
                    playerData.getVelocityIds().remove(transactionActionNumber);

                } else if (playerData.getTransactionTime().containsKey(transactionActionNumber)) {
                    long transactionStamp = playerData.getTransactionTime().get(transactionActionNumber);
                    playerData.setTransPing(System.currentTimeMillis() - transactionStamp);
                    playerData.getTransactionSentMap().remove(transactionActionNumber);
                }
                break;

            case PacketType.Play.Client.UPDATE_SIGN:
                WrappedPacketInUpdateSign updateSign = new WrappedPacketInUpdateSign(event.getNMSPacket());

                for (String line : updateSign.getTextLines()) {
                    if (line.length() > 45) {
                        KickUtil.kickPlayer(player, event, "Sent invalid UpdateSign packet");
                        return;
                    }
                }
                break;

            case PacketType.Play.Client.USE_ENTITY:
                WrappedPacketInUseEntity useEntity = new WrappedPacketInUseEntity(event.getNMSPacket());
                WrappedPacketInUseEntity.EntityUseAction useEntityAction = useEntity.getAction();
                Entity entity = useEntity.getEntity();
                int entityId = useEntity.getEntityId();

                if (entity == null
                        || entity.isDead()
                        || entity.getWorld() != player.getWorld()) {
                    event.setCancelled(true);
                    return;
                }

                if (playerData.isInventoryOpen()
                        || playerData.isPlacingBlock()
                        || playerData.isShootingBow()
                        || playerData.isEating()
                        || playerData.isDrinking()
                        || entity == player
                        || entityId < 0) {
                    KickUtil.kickPlayer(player, event, "Sent invalid UseEntity packet"
                            + " (" + playerData.isInventoryOpen() + " " + playerData.isPlacingBlock()
                            + " " + playerData.isShootingBow() + " " + playerData.isEating()
                            + " " + playerData.isDrinking() + " " + " " + (entity == player) + " " + entityId + ")");
                    return;
                }

                switch (useEntityAction) {
                    case ATTACK:
                        if (playerData.isBlocking()) {
                            KickUtil.kickPlayer(player, event, "Sent invalid Attack packet");
                            return;
                        }

                        playerData.setLastAttackTick(0);
                        playerData.setTimestamp(ActionType.ATTACKING);
                        break;

                    case INTERACT:
                    case INTERACT_AT:
                        playerData.setTimestamp(ActionType.ENTITY_INTERACT);
                        break;
                }

                if (entity instanceof Player) {
                    Player useEntityTarget = (Player) entity;

                    if (playerData.getLastTarget() != null) {
                        playerData.setLastLastTarget(playerData.getLastTarget());

                        if (entity != playerData.getLastTarget()) {
                            playerData.getPastLocsB().clear();
                            playerData.getPastLocsA().clear();
                            playerData.getPastLocsC().clear();
                        }
                    }

                    playerData.setLastTarget(useEntityTarget);
                    playerData.getTarget().set(useEntityTarget);
                } else {
                    playerData.setLastTarget(null);
                    playerData.getTarget().set(null);
                }
                break;

            case PacketType.Play.Client.WINDOW_CLICK:
                WrappedPacketInWindowClick windowClick = new WrappedPacketInWindowClick(event.getNMSPacket());
                int windowId = windowClick.getWindowId();
                int windowSlot = windowClick.getWindowSlot();
                int windowMode = windowClick.getMode();
                int windowButton = windowClick.getWindowButton();

                if (windowId == 0) {
                    if (!playerData.isInventoryOpen() && playerData.getVersion().isOlderThanOrEquals(ClientVersion.v_1_8)) {
                        KickUtil.kickPlayer(player, event, "Sent WindowClick packet with closed inventory");
                        return;
                    }

                    int diff = windowSlot - (player.getOpenInventory().countSlots() - 1);

                    if (windowSlot > 44 || diff > 4
                            || (windowSlot != -999 && windowSlot < -1)) {
                        KickUtil.kickPlayer(player, event, "Sent WindowClick packet on an invalid slot: "
                                + windowSlot + " diff: " + diff);
                        return;
                    }
                }

                switch (windowMode) {
                    case 0:
                        if (windowButton != 0 && windowButton != 1) {
                            KickUtil.kickPlayer(player, event, "Sent WindowClick packet invalid Pickup button");
                        }
                        return;

                    case 1:
                        if (windowButton != 0 && windowButton != 1
                                && windowButton != 2 && windowButton != 5) {
                            KickUtil.kickPlayer(player, event, "Sent WindowClick packet with invalid QuickMove button");
                        }
                        return;

                    case 2:
                        if (windowButton != 0 && windowButton != 1
                                && windowButton != 2 && windowButton != 3
                                && windowButton != 4 && windowButton != 8
                                && windowButton != 40) {
                            KickUtil.kickPlayer(player, event, "Sent WindowClick packet with invalid Swap button");
                        }
                        return;

                    case 3:
                        if (windowButton != 0 && windowButton != 1
                                && windowButton != 2 && windowButton != 4
                                && windowButton != 5) {
                            KickUtil.kickPlayer(player, event, "Sent WindowClick packet with invalid Clone button");
                        }
                        return;

                    case 4:
                        if (windowButton != 0 && windowButton != 1
                                && windowButton != 2 && windowButton != 4
                                && windowButton != 5 && windowButton != 6) {
                            KickUtil.kickPlayer(player, event, "Sent WindowClick packet with invalid Throw button");
                        }
                        return;

                    case 5:
                        if (windowButton != 0 && windowButton != 1
                                && windowButton != 2 && windowButton != 4
                                && windowButton != 5 && windowButton != 6
                                && windowButton != 8 && windowButton != 9
                                && windowButton != 10) {
                            KickUtil.kickPlayer(player, event, "Sent WindowClick packet with invalid QuickCraft button");
                        }
                        return;

                    case 6:
                        if (windowButton != 0 && windowButton != 1
                                && windowButton != 2 && windowButton != 3
                                && windowButton != 4 && windowButton != 5
                                && windowButton != 6) {
                            KickUtil.kickPlayer(player, event, "Sent WindowClick packet with invalid PickupAll button");
                        }
                        break;
                }

                playerData.setTimestamp(ActionType.WINDOW_CLICK);
                break;

            case PacketType.Play.Client.KEEP_ALIVE:
            case PacketType.Play.Client.RESOURCE_PACK_STATUS: // These packets are handled elsewhere
                break;

            default:
                MessageUtil.log(Level.INFO, "Unhandled packet: " + event.getPacketId());
                break;
        }

        handlePacketChecks(playerData, event, false);
    }

    @Override
    // TODO: Look into spoofing entity health to prevent health bar mods.
    //       Also, look into spoofing player enchants for the same reason.
    public void onPacketPlaySend(@NonNull PacketPlaySendEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerDataManager.getPlayerData(player);

        switch (event.getPacketId()) {
            case PacketType.Play.Server.ABILITIES:
                WrappedPacketOutAbilities abilities = new WrappedPacketOutAbilities(event.getNMSPacket());

                playerData.setFlying(abilities.isFlying());
                playerData.setFlightAllowed(abilities.isFlightAllowed());
                playerData.setFlySpeed(abilities.getFlySpeed());
                playerData.setWalkSpeed(abilities.getWalkSpeed());
                playerData.setInstantBuild(abilities.canBuildInstantly());
                playerData.setVulnerable(abilities.isVulnerable());
                break;

            case PacketType.Play.Server.ANIMATION:
                WrappedPacketOutAnimation animation = new WrappedPacketOutAnimation(event.getNMSPacket());

                if (animation.getAnimationType() == WrappedPacketOutAnimation.EntityAnimationType.LEAVE_BED) {
                    playerData.setInBed(false);
                }
                break;

            case PacketType.Play.Server.RESPAWN:
                playerData.setSprinting(false);
                playerData.setSneaking(false);
                playerData.setTimestamp(ActionType.RESPAWN);
                break;

            case PacketType.Play.Server.CAMERA:
                WrappedPacketOutCamera camera = new WrappedPacketOutCamera(event.getNMSPacket());
                playerData.setInCamera(camera.getEntity() != null);
                break;

            case PacketType.Play.Server.CLOSE_WINDOW:
                playerData.setInventoryOpen(false);
                break;

            case PacketType.Play.Server.ENTITY_VELOCITY:
                WrappedPacketOutEntityVelocity entityVelocity = new WrappedPacketOutEntityVelocity(event.getNMSPacket());
                Vector3d velocity = entityVelocity.getVelocity();

                if (entityVelocity.getEntityId() == player.getEntityId()) {
                    short randomID = (short) new Random().nextInt(16);

                    playerData.getVelocityIds().put((short) -(randomID),
                            new Vector(velocity.getX(), velocity.getY(), velocity.getZ()));

                    TaskUtil.run(() -> PacketEvents.get().getPlayerUtils().sendPacket(event.getPlayer(),
                            new WrappedPacketOutTransaction(0, (short) -(randomID), false)));
                }
                break;

            case PacketType.Play.Server.RESOURCE_PACK_SEND:
                WrappedPacketOutResourcePackSend resourcePackSend = new WrappedPacketOutResourcePackSend(event.getNMSPacket());
                String url = resourcePackSend.getUrl();
                String scheme = URI.create(url).getScheme();

                if (scheme == null) {
                    event.setCancelled(true);
                    MessageUtil.log(Level.INFO, "ResourcePackSend packet cancelled; contained null URI scheme");
                    return;
                }

                if (!scheme.equals("https") && !scheme.equals("http") && !scheme.equals("level")) {
                    event.setCancelled(true);
                    MessageUtil.log(Level.INFO, "ResourcePackSend packet cancelled; contained invalid URI scheme");
                    return;
                }

                try {
                    url = URLDecoder.decode(url.substring("level://".length()), StandardCharsets.UTF_8.toString());
                } catch (UnsupportedEncodingException e) {
                    event.setCancelled(true);
                    MessageUtil.log(Level.INFO, "ResourcePackSend packet cancelled; could not decode URL");
                    return;
                }

                if (scheme.equals("level") && (url.contains("..") || !url.endsWith("/resources.zip"))) {
                    event.setCancelled(true);
                    MessageUtil.log(Level.INFO, "ResourcePackSend packet cancelled; contained invalid level URL");
                    return;
                }
                break;

            case PacketType.Play.Server.TRANSACTION:
                WrappedPacketOutTransaction transaction = new WrappedPacketOutTransaction(event.getNMSPacket());
                playerData.getTransactionSentMap().put(transaction.getActionNumber(), System.currentTimeMillis());
                playerData.setPacketsSentInTick(0);
                break;

            case PacketType.Play.Server.OPEN_WINDOW:
                playerData.setInventoryOpen(true);
                playerData.setTimestamp(ActionType.INVENTORY_OPEN);
                break;

            case PacketType.Play.Server.POSITION:
                WrappedPacketOutPosition position = new WrappedPacketOutPosition(event.getNMSPacket());
                playerData.setLastServerPositionTick(0);
                playerData.setLastTeleportPacket(position);
                playerData.setTimestamp(ActionType.TELEPORT);
                break;

            default:
                break;
        }

        handlePacketChecks(playerData, event, true);
    }

    /**
     * Handle the checks for the given packet event.
     *
     * @param playerData The player data.
     * @param event      The packet event.
     */
    private void handlePacketChecks(@NonNull PlayerData playerData,
                                    @NonNull CancellableNMSPacketEvent event,
                                    boolean sendingServerPackets) {
        long timestamp = System.currentTimeMillis();
        NMSPacket nmsPacket = event.getNMSPacket();

        // Create a copy of the checks list to avoid ConcurrentModificationException
        if (playerData.getChecks() != null) {
            List<Check> checksCopy = new ArrayList<>(playerData.getChecks());

            for (Check check : checksCopy) {
                if (sendingServerPackets) {
                    if (check.getCheckInfo().acceptsServerPackets()) {
                        check.handle(event, event.getPacketId(), nmsPacket, nmsPacket.getRawNMSPacket(), timestamp);
                    }
                } else {
                    check.handle(event, event.getPacketId(), nmsPacket, nmsPacket.getRawNMSPacket(), timestamp);
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
    private void handleRotationChecks(@NonNull PlayerData playerData, @NonNull RotationEvent event) {
        long timestamp = System.currentTimeMillis();

        // Create a copy of the checks list to avoid ConcurrentModificationException
        if (playerData.getChecks() != null) {
            List<Check> checksCopy = new ArrayList<>(playerData.getChecks());

            for (Check check : checksCopy) {
                check.handle(event, timestamp);
            }
        }
    }

    /**
     * Handle the checks for the given movement event.
     *
     * @param playerData The player data.
     * @param event      The movement event.
     */
    private void handleMovementChecks(@NonNull PlayerData playerData, @NonNull MovementEvent event) {
        long timestamp = System.currentTimeMillis();

        // Create a copy of the checks list to avoid ConcurrentModificationException
        if (playerData.getChecks() != null) {
            List<Check> checksCopy = new ArrayList<>(playerData.getChecks());

            for (Check check : checksCopy) {
                if (check.getCheckInfo().enabled()) {
                    check.handle(event, timestamp);
                }
            }
        }
    }

    private void steerVehicleCheck(@NonNull Player player, @NonNull PacketPlayReceiveEvent event,
                                   @NonNull WrappedPacketInSteerVehicle steerVehicle, float value) {
        if (Math.abs(value) == 0.98f) {
            if (steerVehicle.isDismount()) {
                event.setCancelled(true);
                KickUtil.kickPlayer(player, "Sent SteerVehicle packet with invalid dismount value");
            }

        } else if (Math.abs(value) == 0.29400003f) {
            if (!steerVehicle.isDismount()) {
                event.setCancelled(true);
                KickUtil.kickPlayer(player, "Sent SteerVehicle packet with invalid non-dismount value");
            }

        } else if (value != 0.0f) {
            event.setCancelled(true);
            KickUtil.kickPlayer(player, "Sent SteerVehicle packet with invalid value: " + value);
        }
    }

    /**
     * Get the packet class from the given packet id.
     *
     * @param packetId The packet id.
     * @return The packet class.
     */
    public static Class<?> getPacketFromId(@NonNull Byte packetId) {
        return PacketType.packetIDMap.entrySet().stream()
                .filter(entry -> Objects.equals(entry.getValue(), packetId))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }
}
