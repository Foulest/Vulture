package net.foulest.vulture.processor.type;

import dev._2lstudios.hamsterapi.HamsterAPI;
import dev._2lstudios.hamsterapi.events.PacketDecodeEvent;
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
import net.foulest.vulture.action.ActionType;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.data.PlayerDataManager;
import net.foulest.vulture.event.MovementEvent;
import net.foulest.vulture.event.RotationEvent;
import net.foulest.vulture.processor.Processor;
import net.foulest.vulture.util.*;
import net.foulest.vulture.util.data.Pair;
import net.foulest.vulture.util.raytrace.BoundingBox;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Beacon;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static net.foulest.vulture.util.MessageUtil.debug;

/**
 * Handles all incoming & outgoing packets.
 *
 * @author Foulest
 * @project Vulture
 */
public class PacketProcessor extends Processor {

    /**
     * Handles incoming packets as they are decoded.
     *
     * @param event PacketDecodeEvent
     */
    @EventHandler
    public void onPacketDecode(@NotNull PacketDecodeEvent event) {
        PlayerData playerData = event.getPlayerData();
        Player player = playerData.getPlayer();

        // Ignores if the packet limit is disabled.
        if (Settings.maxPacketsPerTick <= 0) {
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

        // Iterate packets sent per tick.
        int packetsSentInTick = playerData.getPacketsSentPerTick();
        playerData.setPacketsSentPerTick(packetsSentInTick + 1);
        packetsSentInTick++;

        // If the player has sent too many packets in one tick, kick them.
        if (packetsSentInTick >= Settings.maxPacketsPerTick) {
            event.setCancelled(true);
            HamsterAPI.closeChannel(playerData);
            KickUtil.kickPlayer(player, "Sent too many packets in one tick"
                    + " (count=" + packetsSentInTick + ")", true);
        }
    }

    /**
     * Handles incoming packets after they are decoded.
     *
     * @param event PacketPlayReceiveEvent
     */
    @Override
    @SuppressWarnings("deprecation")
    public void onPacketPlayReceive(@NotNull PacketPlayReceiveEvent event) {
        // Cancels incoming packets for invalid players.
        if (Bukkit.getPlayer(event.getPlayer().getUniqueId()) == null) {
            event.setCancelled(true);
            return;
        }

        Player player = event.getPlayer();
        PlayerData playerData = PlayerDataManager.getPlayerData(player);

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

        switch (event.getPacketId()) {
            case PacketType.Play.Client.ABILITIES:
                WrappedPacketInAbilities abilities = new WrappedPacketInAbilities(event.getNMSPacket());

                if (playerData.getVersion().isOlderThanOrEquals(ClientVersion.v_1_8)) {
                    if (abilities.isFlying() == playerData.isFlying()) {
                        KickUtil.kickPlayer(player, event, Settings.abilitiesDuplicateFlying,
                                "Sent Abilities packet with duplicate flying values"
                                        + " (flying=" + abilities.isFlying()
                                        + " playerFlying=" + playerData.isFlying() + ")"
                        );
                        return;
                    }

                    if (abilities.isFlying() && !playerData.isFlightAllowed()) {
                        KickUtil.kickPlayer(player, event, Settings.abilitiesInvalidFlying,
                                "Sent Abilities packet with invalid flying values"
                                        + " (flying=" + abilities.isFlying()
                                        + " playerFlightAllowed=" + playerData.isFlightAllowed() + ")"
                        );
                        return;
                    }

                    if (abilities.isFlightAllowed().isPresent()
                            && abilities.isFlightAllowed().get() != playerData.isFlightAllowed()) {
                        KickUtil.kickPlayer(player, event, Settings.abilitiesInvalidFlightAllowed,
                                "Sent Abilities packet with invalid flight allowed values"
                                        + " (flightAllowed=" + abilities.isFlightAllowed().get()
                                        + " playerFlightAllowed=" + playerData.isFlightAllowed() + ")"
                        );
                        return;
                    }

                    if (abilities.canInstantlyBuild().isPresent()
                            && abilities.canInstantlyBuild().get() != playerData.isInstantBuild()) {
                        KickUtil.kickPlayer(player, event, Settings.abilitiesInvalidInstantBuild,
                                "Sent Abilities packet with invalid instant build values"
                                        + " (instantBuild=" + abilities.canInstantlyBuild().get()
                                        + " playerInstantBuild=" + playerData.isInstantBuild() + ")"
                        );
                        return;
                    }

                    if (abilities.isVulnerable().isPresent()
                            && abilities.isVulnerable().get() != playerData.isVulnerable()) {
                        KickUtil.kickPlayer(player, event, Settings.abilitiesInvalidInvulnerable,
                                "Sent Abilities packet with invalid invulnerable values"
                                        + " (invulnerable=" + abilities.isVulnerable().get()
                                        + " playerInvulnerable=" + playerData.isVulnerable() + ")"
                        );
                        return;
                    }

                    if (abilities.getFlySpeed().isPresent()
                            && !playerData.isFlightAllowed()
                            && abilities.getFlySpeed().get() != playerData.getFlySpeed()) {
                        KickUtil.kickPlayer(player, event, Settings.abilitiesInvalidFlySpeed,
                                "Sent Abilities packet with invalid fly speed values"
                                        + " (flySpeed=" + abilities.getFlySpeed().get()
                                        + " playerFlySpeed=" + playerData.getFlySpeed() + ")"
                        );
                        return;
                    }

                    if (abilities.getWalkSpeed().isPresent()
                            && abilities.getWalkSpeed().get() != playerData.getWalkSpeed()) {
                        KickUtil.kickPlayer(player, event, Settings.abilitiesInvalidWalkSpeed,
                                "Sent Abilities packet with invalid walk speed values"
                                        + " (walkSpeed=" + abilities.getWalkSpeed().get()
                                        + " playerWalkSpeed=" + playerData.getWalkSpeed() + ")"
                        );
                        return;
                    }
                }

                if (abilities.isFlying()) {
                    playerData.setFlying(true);
                    playerData.setTimestamp(ActionType.START_FLYING);
                } else {
                    playerData.setFlying(false);
                    playerData.setTimestamp(ActionType.STOP_FLYING);
                }

                abilities.isFlightAllowed().ifPresent(playerData::setFlightAllowed);
                abilities.canInstantlyBuild().ifPresent(playerData::setInstantBuild);
                abilities.isVulnerable().ifPresent(playerData::setVulnerable);
                abilities.getFlySpeed().ifPresent(playerData::setFlySpeed);
                abilities.getWalkSpeed().ifPresent(playerData::setWalkSpeed);
                break;

            case PacketType.Play.Client.ARM_ANIMATION:
                if (playerData.isInventoryOpen()) {
                    KickUtil.kickPlayer(player, event, Settings.armAnimationInvalidConditions,
                            "Sent ArmAnimation packet with invalid conditions"
                                    + " (inventoryOpen=" + true + ")"
                    );
                    return;
                }

                playerData.setTimestamp(ActionType.ARM_ANIMATION);
                break;

            case PacketType.Play.Client.BLOCK_DIG:
                WrappedPacketInBlockDig blockDig = new WrappedPacketInBlockDig(event.getNMSPacket());
                Vector3i digBlockPosition = blockDig.getBlockPosition();
                Direction digDirection = blockDig.getDirection();
                int digXPos = digBlockPosition.getX();
                int digYPos = digBlockPosition.getY();
                int digZPos = digBlockPosition.getZ();

                if (blockDig.getDigType() == null) {
                    break;
                }

                if (digXPos != 0 && digYPos != 0 && digZPos != 0
                        && digXPos != -1 && digYPos != -1 && digZPos != -1) {
                    Location playerLocation = player.getLocation();
                    Location blockLocation = new Location(player.getWorld(), digXPos, digYPos, digZPos);
                    double distance = playerLocation.distance(blockLocation);

                    if (distance > 7.03) {
                        KickUtil.kickPlayer(player, event, Settings.blockDigInvalidDistance,
                                "Sent BlockDig packet with invalid distance"
                                        + " (distance=" + distance + ")"
                        );
                        return;
                    }
                }

                switch (blockDig.getDigType()) {
                    case START_DESTROY_BLOCK:
                        // Ignores players in creative mode.
                        if (player.getGameMode() == GameMode.CREATIVE) {
                            break;
                        }

                        // Checks the block being dug.
                        TaskUtil.runTask(() -> {
                            Block block = player.getWorld().getBlockAt(digXPos, digYPos, digZPos);

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
                                    break;

                                default:
                                    playerData.setDigging(true);
                                    break;
                            }
                        });
                        break;

                    case STOP_DESTROY_BLOCK:
                    case ABORT_DESTROY_BLOCK:
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

                        if (!playerData.isBlocking() && !playerData.isShootingBow()
                                && !playerData.isEating() && !playerData.isDrinking()) {
                            KickUtil.kickPlayer(player, event, Settings.releaseUseItemInvalidConditions,
                                    "Sent ReleaseUseItem packet with invalid conditions"
                                            + " (blocking=" + false
                                            + " shootingBow=" + false
                                            + " eating=" + false
                                            + " drinking=" + false + ")"
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
                WrappedPacketInBlockPlace blockPlace = new WrappedPacketInBlockPlace(event.getNMSPacket());
                Vector3i placeBlockPosition = blockPlace.getBlockPosition();
                Direction placeDirection = blockPlace.getDirection();
                double placeXPos = placeBlockPosition.getX();
                double placeYPos = placeBlockPosition.getY();
                double placeZPos = placeBlockPosition.getZ();

                if (playerData.isInventoryOpen()) {
                    KickUtil.kickPlayer(player, event, Settings.blockPlaceInvalidConditions,
                            "Sent BlockPlace packet with invalid conditions"
                                    + " (inventoryOpen=" + true + ")"
                    );
                    return;
                }

                if (placeXPos != -1 && placeYPos != -1 && placeZPos != -1) {
                    Location blockLocation = new Location(player.getWorld(), placeXPos, placeYPos, placeZPos);
                    double distance = player.getLocation().distance(blockLocation);

                    if (distance > 7.03) {
                        KickUtil.kickPlayer(player, event, Settings.blockPlaceInvalidDistance,
                                "Sent BlockPlace packet with invalid distance"
                                        + " (distance=" + distance + ")"
                        );
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
                        && placeXPos != -1.0 && placeYPos != -1.0 && placeZPos != -1.0) {
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

                    if (itemStack.getType() != Material.AIR && !player.getInventory().contains(itemStack)) {
                        if (player.getInventory().contains(itemStack.getType()) && !itemStack.hasItemMeta()) {
                            break;
                        }

                        KickUtil.kickPlayer(player, event, Settings.blockPlaceInvalidItem,
                                "Sent BlockPlace packet with invalid item"
                                        + " (item=" + itemStack
                                        + " contents=" + Arrays.toString(player.getInventory().getContents()) + ")"
                        );
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
                        || playerData.isPlacingBlock()) {
                    KickUtil.kickPlayer(player, event, Settings.chatInvalidConditions,
                            "Sent Chat packet with invalid conditions"
                                    + " (blocking=" + playerData.isBlocking()
                                    + " shootingBow=" + playerData.isShootingBow()
                                    + " eating=" + playerData.isEating()
                                    + " drinking=" + playerData.isDrinking()
                                    + " inventoryOpen=" + playerData.isInventoryOpen()
                                    + " digging=" + playerData.isDigging()
                                    + " placingBlock=" + playerData.isPlacingBlock() + ")");
                    return;
                }

                if (chat.getMessage().isEmpty()) {
                    KickUtil.kickPlayer(player, event, Settings.chatInvalidMessage,
                            "Sent Chat packet with invalid message"
                                    + " (message=" + chat.getMessage() + ")"
                    );
                    return;
                }

                playerData.setTimestamp(ActionType.CHATTING);
                break;

            case PacketType.Play.Client.CLIENT_COMMAND:
                WrappedPacketInClientCommand clientCommand = new WrappedPacketInClientCommand(event.getNMSPacket());

                switch (clientCommand.getClientCommand()) {
                    case OPEN_INVENTORY_ACHIEVEMENT:
                        // Players can't open their inventory in portals.
                        if (playerData.isNearPortal()) {
                            break;
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
                            KickUtil.kickPlayer(player, event, Settings.respawnInvalidConditions,
                                    "Sent Respawn packet with invalid conditions"
                                            + " (shootingBow=" + playerData.isShootingBow()
                                            + " eating=" + playerData.isEating()
                                            + " drinking=" + playerData.isDrinking()
                                            + " inventoryOpen=" + playerData.isInventoryOpen()
                                            + " placingBlock=" + playerData.isPlacingBlock()
                                            + " blocking=" + playerData.isBlocking()
                                            + " digging=" + playerData.isDigging()
                                            + " dead=" + player.isDead() + ")"
                            );
                            return;
                        }

                        playerData.setTimestamp(ActionType.RESPAWN);
                        break;

                    case REQUEST_STATS: // Ignored
                        break;

                    default:
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
                    KickUtil.kickPlayer(player, event, Settings.closeWindowInvalidConditions,
                            "Sent CloseWindow packet with invalid conditions"
                                    + " (shootingBow=" + playerData.isShootingBow()
                                    + " eating=" + playerData.isEating()
                                    + " drinking=" + playerData.isDrinking()
                                    + " placingBlock=" + playerData.isPlacingBlock()
                                    + " blocking=" + playerData.isBlocking()
                                    + " digging=" + playerData.isDigging() + ")"
                    );
                    return;
                }

                if (!playerData.isInventoryOpen() && !playerData.getVersion().isNewerThan(ClientVersion.v_1_8)) {
                    KickUtil.kickPlayer(player, event, Settings.closeWindowClosedInventory,
                            "Sent CloseWindow packet with closed inventory"
                                    + " (inventoryOpen=" + false + ")"
                    );
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
                    KickUtil.kickPlayer(player, event, Settings.customPayloadInvalidSize,
                            "Sent CustomPayload packet with invalid size"
                                    + " (size=" + payloadSize + ")"
                    );
                    return;
                }

                // Checks for invalid item name payloads.
                if (channelName.equals("MC|ItemName")) {
                    if (payloadSize > (playerData.getVersion().isOlderThanOrEquals(ClientVersion.v_1_8) ? 31 : 32)) {
                        KickUtil.kickPlayer(player, event, Settings.itemNameInvalidSize,
                                "Sent ItemName payload with invalid size"
                                        + " (size=" + payloadSize + ")"
                        );
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
                        KickUtil.kickPlayer(player, event, Settings.itemNameInvalidData,
                                "Sent ItemName payload with invalid data"
                                        + " (data=" + data + ")"
                        );

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

                // Checks for invalid beacon payloads.
                if (channelName.equals("MC|Beacon")) {
                    if (player.getOpenInventory().getType() != InventoryType.BEACON) {
                        KickUtil.kickPlayer(player, event, Settings.beaconInvalidConditions,
                                "Sent Beacon payload with invalid conditions"
                                        + " (inventoryType=" + player.getOpenInventory().getType() + ")"
                        );
                        return;
                    }

                    Beacon beacon = (Beacon) player.getOpenInventory().getTopInventory().getHolder();
                    int tier = BeaconUtil.getTier(beacon.getLocation());

                    if (data.charAt(0) == '\u0000'
                            && data.charAt(1) == '\u0000'
                            && data.charAt(2) == '\u0000'
                            && data.charAt(4) == '\u0000'
                            && data.charAt(5) == '\u0000'
                            && data.charAt(6) == '\u0000'
                            && (data.charAt(7) == '\u0000' || data.charAt(7) == '\n' || data.charAt(7) == '\u0001'
                            || data.charAt(7) == '\u0003' || data.charAt(7) == '\u000B' || data.charAt(7) == '\u0008'
                            || data.charAt(7) == '\u0005')) {

                        // Checks for invalid beacon effect payloads.
                        if (data.charAt(3) != '\u0001' // Speed
                                && data.charAt(3) != '\u0003' // Haste
                                && data.charAt(3) != '\u000B' // Resistance
                                && data.charAt(3) != '\u0008' // Jump Boost
                                && data.charAt(3) != '\u0005' // Strength
                        ) {
                            KickUtil.kickPlayer(player, event, Settings.beaconInvalidEffect,
                                    "Sent Beacon payload with invalid effect"
                                            + " (effect=" + data.charAt(3) + ")"
                            );
                            return;
                        }

                        // Checks for players sending beacon effects that don't match the beacon tier.
                        switch (tier) {
                            case 1:
                                if (data.charAt(3) == '\u000B'
                                        || data.charAt(3) == '\u0008'
                                        || data.charAt(3) == '\u0005') {
                                    KickUtil.kickPlayer(player, event, Settings.beaconInvalidTier,
                                            "Sent Beacon payload with invalid tier"
                                                    + " (tier=" + tier
                                                    + " (data=" + data + ")"
                                    );
                                    return;
                                }
                                break;

                            case 2:
                                if (data.charAt(3) == '\u0005') {
                                    KickUtil.kickPlayer(player, event, Settings.beaconInvalidTier,
                                            "Sent Beacon payload with invalid tier"
                                                    + " (tier=" + tier
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
                    ItemStack itemInHand = playerData.getPlayer().getInventory().getItemInHand();

                    // Checks for invalid book open payloads.
                    // (The client doesn't send this payload; the server does)
                    if (channelName.equals("MC|BOpen")) {
                        KickUtil.kickPlayer(player, event, Settings.bookOpenInvalidConditions,
                                "Sent BookOpen payload with invalid conditions"
                        );
                        return;
                    }

                    // Checks for invalid book edit payloads.
                    if (channelName.equals("MC|BEdit")) {
                        if (itemInHand == null || !itemInHand.getType().toString().toLowerCase().contains("book")) {
                            KickUtil.kickPlayer(player, event, Settings.bookEditInvalidConditions,
                                    "Sent BookEdit payload with invalid conditions"
                                            + " (itemInHand=" + itemInHand + ")"
                            );
                            return;
                        }

                        if (!data.startsWith("\u0001ï¿½\u0001\u0000\u0000\n\u0000\u0000\t"
                                + "\u0000\u0005pages\b\u0000\u0000\u0000\u0001\u0000\f") && !data.endsWith("\u0000")) {
                            KickUtil.kickPlayer(player, event, Settings.bookEditInvalidData,
                                    "Sent BookEdit payload with invalid data"
                                            + " (" + data + ")"
                            );
                            return;
                        }
                    }

                    // Checks for invalid book sign payloads.
                    if (channelName.equals("MC|BSign")) {
                        if (itemInHand == null || !itemInHand.getType().toString().toLowerCase().contains("book")) {
                            KickUtil.kickPlayer(player, event, Settings.bookSignInvalidConditions,
                                    "Sent BookSign payload with invalid conditions"
                                            + " (itemInHand=" + itemInHand + ")"
                            );
                            return;
                        }

                        if (!data.startsWith("\u0001ï¿½\u0001\u0000\u0000\n\u0000\u0000\t"
                                + "\u0000\u0005pages\b\u0000\u0000\u0000\u0001\u0000\f")
                                && !data.endsWith("\u0000") && !data.contains("\b\u0000\u0006author\u0000\u0007")
                                && !data.contains("\b\u0000\u0005title\u0000\u0004")) {
                            KickUtil.kickPlayer(player, event, Settings.bookSignInvalidData,
                                    "Sent BookSign payload with invalid data"
                                            + " (" + data + ")"
                            );
                            return;
                        }
                    }
                }

                if ((channelName.equals("MC|AdvCdm") || channelName.equals("MC|AutoCmd")) && !player.isOp()) {
                    KickUtil.kickPlayer(player, event, Settings.commandBlockInvalidConditions,
                            "Sent CommandBlock payload with invalid conditions"
                                    + " (op=" + player.isOp() + ")"
                    );
                    return;
                }

                playerData.setTimestamp(ActionType.SENT_PAYLOAD);
                break;

            case PacketType.Play.Client.DIFFICULTY_CHANGE: // Players shouldn't be able to send this packet
                event.setCancelled(true);
                break;

            case PacketType.Play.Client.ENTITY_ACTION:
                WrappedPacketInEntityAction entityAction = new WrappedPacketInEntityAction(event.getNMSPacket());
                WrappedPacketInEntityAction.PlayerAction playerAction = entityAction.getAction();
                int jumpBoost = entityAction.getJumpBoost();

                if (playerAction != WrappedPacketInEntityAction.PlayerAction.RIDING_JUMP && jumpBoost != 0) {
                    KickUtil.kickPlayer(player, event, Settings.entityActionInvalidJumpBoost,
                            "Send EntityAction packet with invalid jump boost");
                    return;
                }

                switch (playerAction) {
                    case START_SPRINTING:
                        if ((playerData.isBlocking() && playerData.getVersion().isOlderThanOrEquals(ClientVersion.v_1_8))
                                || playerData.isShootingBow()
                                || playerData.isDrinking()
                                || player.hasPotionEffect(PotionEffectType.BLINDNESS)) {
                            KickUtil.kickPlayer(player, event, Settings.startSprintingInvalidConditions,
                                    "Sent StartSprinting packet with invalid conditions"
                                            + " (blocking=" + playerData.isBlocking()
                                            + " shootingBow=" + playerData.isShootingBow()
                                            + " drinking=" + playerData.isDrinking()
                                            + " blindness=" + player.hasPotionEffect(PotionEffectType.BLINDNESS) + ")"
                            );
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
                            KickUtil.kickPlayer(player, event, Settings.startSneakingInvalidConditions,
                                    "Sent StartSneaking packet with invalid conditions"
                                            + " (inventoryOpen=" + playerData.isInventoryOpen()
                                            + " sneaking=" + playerData.isSneaking() + ")"
                            );
                            return;
                        }

                        playerData.setSneaking(true);
                        playerData.setTimestamp(ActionType.SNEAKING);
                        break;

                    case STOP_SNEAKING:
                        if (!playerData.isSneaking()) {
                            KickUtil.kickPlayer(player, event, Settings.stopSneakingInvalidConditions,
                                    "Sent StopSneaking packet with invalid conditions"
                                            + " (sneaking=" + false + ")"
                            );
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

                        // Players can't open their inventory in portals.
                        if (playerData.isNearPortal()) {
                            break;
                        }

                        playerData.setInventoryOpen(true);
                        playerData.setTimestamp(ActionType.INVENTORY_OPEN);
                        break;

                    case STOP_SLEEPING:
                        if (playerData.isInventoryOpen()
                                || !playerData.isInBed()) {
                            KickUtil.kickPlayer(player, event, Settings.stopSleepingInvalidConditions,
                                    "Sent StopSleeping packet with invalid conditions"
                                            + " (inventoryOpen=" + playerData.isInventoryOpen()
                                            + " inBed=" + playerData.isInBed() + ")"
                            );
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
                                || player.getVehicle().getType() != EntityType.HORSE) {
                            KickUtil.kickPlayer(player, event, Settings.ridingJumpInvalidConditions,
                                    "Sent RidingJump packet with invalid conditions"
                                            + " (inventoryOpen=" + playerData.isInventoryOpen()
                                            + " insideVehicle=" + player.isInsideVehicle()
                                            + " vehicle=" + (player.isInsideVehicle() ? player.getVehicle().getType() : null) + ")"
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

            case PacketType.Play.Client.ENCHANT_ITEM:
                WrappedPacketInEnchantItem enchantItem = new WrappedPacketInEnchantItem(event.getNMSPacket());
                int enchantItemWindowId = enchantItem.getWindowId();

                if (enchantItemWindowId < 0 || enchantItemWindowId > 2) {
                    KickUtil.kickPlayer(player, event, Settings.enchantItemInvalidWindowId,
                            "Sent EnchantItem packet with invalid window ID"
                                    + " (windowId=" + enchantItemWindowId + ")"
                    );
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

                // Handles resetting velocity data.
                if (playerData.getTotalTicks() - playerData.getVelocityTicks() > 1) {
                    playerData.setLastVelocityX(playerData.getVelocityX());
                    playerData.setLastVelocityY(playerData.getVelocityY());
                    playerData.setLastVelocityZ(playerData.getVelocityZ());
                    playerData.setLastVelocityXZ(playerData.getVelocityXZ());
                    playerData.setVelocityX(0);
                    playerData.setVelocityY(0);
                    playerData.setVelocityZ(0);
                    playerData.setVelocityXZ(0);
                }

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
                        KickUtil.kickPlayer(player, event, Settings.flyingInvalidPitch,
                                "Sent Flying packet with invalid pitch"
                                        + " (pitch=" + flyingPitch + ")"
                        );
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
                    boolean chunkUnloaded = BlockUtil.isLocationInUnloadedChunk(location);
                    playerData.setInUnloadedChunk(chunkUnloaded);

                    if (chunkUnloaded) {
                        playerData.setTimestamp(ActionType.IN_UNLOADED_CHUNK);
                    }

                    if (player.isInsideVehicle() && playerData.getTimeSince(ActionType.ENTER_VEHICLE) > 100L) {
                        event.setCancelled(true);
                        player.getVehicle().eject();
                        debug("Flying packet ignored for " + player.getName() + " (inside vehicle) "
                                + playerData.getTimeSince(ActionType.ENTER_VEHICLE));
                    }

                    // Set ground data
                    playerData.setLastLastOnGroundPacket(playerData.isLastOnGroundPacket());
                    playerData.setLastOnGroundPacket(playerData.isOnGroundPacket());
                    playerData.setOnGroundPacket(flying.isOnGround());
                    playerData.setLastOnGround(playerData.isNearGround());
                    playerData.setOnGround(BlockUtil.isOnGroundOffset(player, 0.001));
                    playerData.setNearGround(BlockUtil.isOnGroundOffset(player, 0.25)); // Normally 0.21, or 0.105

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
                    playerData.setNearStairs(BlockUtil.isNearStairs(player));
                    playerData.setNearPiston(BlockUtil.isNearPiston(player));
                    playerData.setNearCactus(BlockUtil.isNearCactus(player));
                    playerData.setInWeb(BlockUtil.isInWeb(player));
                    playerData.setInLiquid(BlockUtil.isInLiquid(player));
                    playerData.setNearLiquid(BlockUtil.isNearLiquid(player));
                    playerData.setOnChest(BlockUtil.isOnChest(player));
                    playerData.setOnClimbable(BlockUtil.isOnClimbable(player));
                    playerData.setNearClimbable(BlockUtil.isNearClimbable(player));
                    playerData.setNearPortal(BlockUtil.isNearPortal(player));
                    playerData.setOnSnowLayer(BlockUtil.isOnSnowLayer(player));
                    playerData.setOnIce(BlockUtil.isOnIce(player));
                    playerData.setOnSoulSand(BlockUtil.isOnSoulSand(player));
                    playerData.setNearTrapdoor(BlockUtil.isNearTrapdoor(player));
                    playerData.setNearFenceGate(BlockUtil.isNearFenceGate(player));
                    playerData.setOnLilyPad(BlockUtil.isOnLilyPad(player));
                    playerData.setNearLilyPad(BlockUtil.isNearLilyPad(player));
                    playerData.setNearAnvil(BlockUtil.isNearAnvil(player));
                    playerData.setNearSlimeBlock(BlockUtil.isNearSlimeBlock(player));

                    if (playerData.isNearSlimeBlock() && !playerData.isOnGround()) {
                        playerData.setUnderEffectOfSlime(true);
                    } else if (!playerData.isNearSlimeBlock() && playerData.isOnGround()) {
                        playerData.setTouchedGroundSinceLogin(true);
                        playerData.setUnderEffectOfSlime(false);
                    } else if (playerData.isOnGround()) {
                        playerData.setTouchedGroundSinceLogin(true);
                    }

                    if (playerData.isNearClimbable()
                            || playerData.isNearLiquid()
                            || player.isFlying()) {
                        playerData.setUnderEffectOfSlime(false);
                    }

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

                    if (playerData.isOnIce()) {
                        playerData.setTimestamp(ActionType.ON_ICE);
                    }

                    if (playerData.isInLiquid()) {
                        playerData.setTimestamp(ActionType.IN_LIQUID);
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
                    KickUtil.kickPlayer(player, event, Settings.heldItemSlotInvalidConditions,
                            "Sent HeldItemSlot packet with invalid conditions"
                                    + " (timeSinceInventoryOpen=" + playerData.getTimeSince(ActionType.INVENTORY_OPEN) + ")"
                    );
                    return;
                }

                if (currentSlot == playerData.getCurrentSlot()
                        && playerData.getVersion().isOlderThanOrEquals(ClientVersion.v_1_8)
                        && playerData.getTimeSince(ActionType.LOGIN) > 1000) {
                    KickUtil.kickPlayer(player, event, Settings.heldItemSlotInvalidSlotChange,
                            "Sent HeldItemSlot packet with invalid slot change"
                                    + " (slot=" + currentSlot + ")"
                                    + " (currentSlot=" + playerData.getCurrentSlot() + ")"
                    );
                    return;
                }

                if (currentSlot < 0 || currentSlot > 8) {
                    KickUtil.kickPlayer(player, event, Settings.heldItemSlotInvalidSlot,
                            "Sent HeldItemSlot packet with invalid slot"
                                    + " (slot=" + currentSlot + ")"
                    );
                    return;
                }

                playerData.setCurrentSlot(currentSlot);
                playerData.setTimestamp(ActionType.CHANGE_SLOT);
                break;

            case PacketType.Play.Client.SET_CREATIVE_SLOT:
                WrappedPacketInSetCreativeSlot setCreativeSlot = new WrappedPacketInSetCreativeSlot(event.getNMSPacket());
                int creativeSlot = setCreativeSlot.getSlot();

                if (player.getGameMode() != GameMode.CREATIVE) {
                    KickUtil.kickPlayer(player, event, Settings.setCreativeSlotInvalidConditions,
                            "Sent SetCreativeSlot packet with invalid conditions"
                                    + " (gamemode=" + player.getGameMode() + ")"
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
                WrappedPacketInSettings settings = new WrappedPacketInSettings(event.getNMSPacket());
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
                WrappedPacketInSpectate spectate = new WrappedPacketInSpectate(event.getNMSPacket());

                if (player.getGameMode() != GameMode.SPECTATOR
                        || Bukkit.getServer().getPlayer(spectate.getUUID()) == null) {
                    KickUtil.kickPlayer(player, event, Settings.spectateInvalidConditions,
                            "Sent Spectate packet with invalid conditions"
                                    + " (gamemode=" + player.getGameMode() + ")"
                                    + " (player=" + Bukkit.getServer().getPlayer(spectate.getUUID()) + ")"
                    );
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
                        break;
                    }

                    KickUtil.kickPlayer(player, event, Settings.steerVehicleInvalidConditions,
                            "Sent SteerVehicle packet with invalid conditions"
                                    + " (sidewaysValue=" + sidewaysValue
                                    + " forwardValue=" + forwardValue
                                    + " jump=" + steerVehicle.isJump()
                                    + " insideVehicle=" + player.isInsideVehicle() + ")"
                    );
                    return;

                } else if (!player.isInsideVehicle()) {
                    event.setCancelled(true);
                    debug("SteerVehicle packet ignored for " + player.getName() + " (not inside vehicle)");
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
                    KickUtil.kickPlayer(player, event, Settings.tabCompleteInvalidMessage,
                            "Sent TabComplete packet with invalid message"
                                    + " (message=" + tabComplete.getText() + ")"
                    );
                    return;
                }
                break;

            case PacketType.Play.Client.TRANSACTION:
                WrappedPacketInTransaction transaction = new WrappedPacketInTransaction(event.getNMSPacket());
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
                if (transactionWindowId != 0 && !playerData.isInventoryOpen()) {
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
                WrappedPacketInUpdateSign updateSign = new WrappedPacketInUpdateSign(event.getNMSPacket());

                for (String line : updateSign.getTextLines()) {
                    if (line.length() > 45) {
                        KickUtil.kickPlayer(player, event, Settings.updateSignInvalidData,
                                "Sent UpdateSign packet with invalid data"
                                        + " (line=" + line + ")"
                                        + " (length=" + line.length() + ")"
                        );
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
                    break;
                }

                double distance = entity.getLocation().distance(player.getLocation());

                if (distance > 7.03) {
                    KickUtil.kickPlayer(player, event, Settings.useEntityInvalidDistance,
                            "Sent UseEntity packet with invalid distance"
                                    + " (distance=" + distance + ")"
                    );
                    return;
                }

                if (playerData.isInventoryOpen()
                        || playerData.isPlacingBlock()
                        || playerData.isShootingBow()
                        || playerData.isEating()
                        || playerData.isDrinking()
                        || entity.equals(player)
                        || entityId < 0) {
                    KickUtil.kickPlayer(player, event, Settings.useEntityInvalidConditions,
                            "Sent UseEntity packet with invalid conditions"
                                    + " (inventoryOpen=" + playerData.isInventoryOpen()
                                    + " placingBlock=" + playerData.isPlacingBlock()
                                    + " shootingBow=" + playerData.isShootingBow()
                                    + " eating=" + playerData.isEating()
                                    + " drinking=" + playerData.isDrinking()
                                    + " entityEqualsPlayer=" + (entity.equals(player))
                                    + " entityId=" + entityId + ")"
                    );
                    return;
                }

                switch (useEntityAction) {
                    case ATTACK:
                        if (playerData.isBlocking()) {
                            KickUtil.kickPlayer(player, event, Settings.attackEntityInvalidConditions,
                                    "Sent AttackEntity packet with invalid conditions"
                                            + " (blocking=" + true + ")"
                            );
                            return;
                        }

                        playerData.setLastAttackTick(0);
                        playerData.setTimestamp(ActionType.ATTACKING);
                        break;

                    case INTERACT:
                    case INTERACT_AT:
                        playerData.setTimestamp(ActionType.ENTITY_INTERACT);
                        break;

                    default:
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
                    if (!playerData.isInventoryOpen() && !playerData.getVersion().isNewerThan(ClientVersion.v_1_8)) {
                        KickUtil.kickPlayer(player, event, Settings.windowClickInvalidConditions,
                                "Sent WindowClick packet with invalid conditions"
                                        + " (inventoryOpen=" + false + ")"
                        );
                        return;
                    }

                    int diff = windowSlot - (player.getOpenInventory().countSlots() - 1);

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
                                && windowButton != 4 && windowButton != 8
                                && windowButton != 40) {
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

            case PacketType.Play.Client.KEEP_ALIVE:
            case PacketType.Play.Client.RESOURCE_PACK_STATUS: // These packets are handled elsewhere
                break;

            default:
                debug("Unhandled packet: " + event.getPacketId());
                break;
        }

        // Handles packet checks.
        handlePacketChecks(playerData, event, false);

        // Sets the last on ground location for setback purposes.
        // This has to be done after the packet checks are handled.
        switch (event.getPacketId()) {
            case PacketType.Play.Client.POSITION:
            case PacketType.Play.Client.POSITION_LOOK:
            case PacketType.Play.Client.LOOK:
            case PacketType.Play.Client.FLYING:
                WrappedPacketInFlying flying = new WrappedPacketInFlying(event.getNMSPacket());
                Vector3d flyingPosition = flying.getPosition();
                double flyingXPos = flyingPosition.getX();
                double flyingYPos = flyingPosition.getY();
                double flyingZPos = flyingPosition.getZ();

                if (flying.isMoving()) {
                    Location currentLocation = playerData.getLocation();

                    if (currentLocation == null) {
                        currentLocation = player.getLocation();
                    }

                    Location location = new Location(player.getWorld(), flyingXPos, flyingYPos, flyingZPos,
                            currentLocation.getYaw(), currentLocation.getPitch());

                    // Sets the last on ground location.
                    if (playerData.isNearGround() && flyingYPos % 0.015625 == 0.0 && !playerData.isInUnloadedChunk()
                            && !playerData.isInsideBlock() && location.getBlock().isEmpty()
                            && playerData.getTimeSince(ActionType.SETBACK) > 40L
                            && playerData.getTimeSince(ActionType.LAST_ON_GROUND_LOCATION_SET) > 40L) {
                        playerData.setTimestamp(ActionType.LAST_ON_GROUND_LOCATION_SET);
                        playerData.setLastOnGroundLocation(location);
                    }
                }
                break;

            default:
                break;
        }
    }

    /**
     * Handles outgoing packets before they are encoded.
     *
     * @param event The packet event.
     */
    @Override
    public void onPacketPlaySend(@NotNull PacketPlaySendEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerDataManager.getPlayerData(player);

        // Sets the last server packet timestamps.
        if (playerData.getTimeSince(ActionType.SERVER_PACKET_RECEIVED) >= 1000L) {
            playerData.setTimestamp(ActionType.SERVER_PACKET_DELAYED);
        }
        playerData.setTimestamp(ActionType.SERVER_PACKET_RECEIVED);

        switch (event.getPacketId()) {
            case PacketType.Play.Server.ABILITIES:
                WrappedPacketOutAbilities abilities = new WrappedPacketOutAbilities(event.getNMSPacket());

                playerData.setFlying(abilities.isFlying());
                playerData.setFlightAllowed(abilities.isFlightAllowed());
                playerData.setFlySpeed(abilities.getFlySpeed());
                playerData.setWalkSpeed(abilities.getWalkSpeed());
                playerData.setInstantBuild(abilities.canBuildInstantly());
                playerData.setVulnerable(abilities.isVulnerable());

                if (abilities.isFlying()) {
                    playerData.setTimestamp(ActionType.START_FLYING);
                } else {
                    playerData.setTimestamp(ActionType.STOP_FLYING);
                }
                break;

            case PacketType.Play.Server.ANIMATION:
                WrappedPacketOutAnimation animation = new WrappedPacketOutAnimation(event.getNMSPacket());

                if (animation.getAnimationType() == WrappedPacketOutAnimation.EntityAnimationType.LEAVE_BED) {
                    playerData.setInBed(false);
                }
                break;

            case PacketType.Play.Server.ATTACH_ENTITY:
                if (player.isInsideVehicle()) {
                    playerData.setTimestamp(ActionType.ENTER_VEHICLE);
                } else {
                    playerData.setTimestamp(ActionType.LEAVE_VEHICLE);
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
                    playerData.setLastVelocityX(playerData.getVelocityX());
                    playerData.setLastVelocityY(playerData.getVelocityY());
                    playerData.setLastVelocityZ(playerData.getVelocityZ());
                    playerData.setLastVelocityXZ(playerData.getVelocityXZ());

                    playerData.setVelocityX(velocity.getX());
                    playerData.setVelocityY(velocity.getY());
                    playerData.setVelocityZ(velocity.getZ());
                    playerData.setVelocityXZ(Math.hypot(velocity.getX(), velocity.getZ()));

                    playerData.setVelocityTicks(playerData.getTotalTicks());
                }
                break;

            case PacketType.Play.Server.RESOURCE_PACK_SEND:
                WrappedPacketOutResourcePackSend resourcePackSend = new WrappedPacketOutResourcePackSend(event.getNMSPacket());
                String url = resourcePackSend.getUrl();
                String scheme = URI.create(url).getScheme();

                if (scheme == null) {
                    event.setCancelled(true);
                    debug("ResourcePackSend packet cancelled; contained null URI scheme");
                    break;
                }

                if (!scheme.equals("https") && !scheme.equals("http") && !scheme.equals("level")) {
                    event.setCancelled(true);
                    debug("ResourcePackSend packet cancelled; contained invalid URI scheme");
                    break;
                }

                try {
                    url = URLDecoder.decode(url.substring("level://".length()), StandardCharsets.UTF_8.toString());
                } catch (UnsupportedEncodingException ignored) {
                    event.setCancelled(true);
                    debug("ResourcePackSend packet cancelled; could not decode URL");
                    break;
                }

                if (scheme.equals("level") && (url.contains("..") || !url.endsWith("/resources.zip"))) {
                    event.setCancelled(true);
                    debug("ResourcePackSend packet cancelled; contained invalid level URL");
                    break;
                }
                break;

            case PacketType.Play.Server.TRANSACTION:
                WrappedPacketOutTransaction transaction = new WrappedPacketOutTransaction(event.getNMSPacket());
                playerData.getTransactionSentMap().put(transaction.getActionNumber(), System.currentTimeMillis());
                break;

            case PacketType.Play.Server.OPEN_WINDOW:
                // Players can't open their inventory in portals.
                if (playerData.isNearPortal()) {
                    break;
                }

                playerData.setInventoryOpen(true);
                playerData.setTimestamp(ActionType.INVENTORY_OPEN);
                break;

            case PacketType.Play.Server.POSITION:
                WrappedPacketOutPosition position = new WrappedPacketOutPosition(event.getNMSPacket());
                Location teleportLoc = new Location(player.getWorld(), position.getPosition().getX(),
                        position.getPosition().getY(), position.getPosition().getZ(),
                        position.getYaw(), position.getPitch());

                playerData.setLastServerPositionTick(0);
                playerData.setLastTeleportPacket(position);
                playerData.setTimestamp(ActionType.TELEPORT);

                if (BlockUtil.isLocationInUnloadedChunk(teleportLoc)) {
                    playerData.setInUnloadedChunk(true);
                    playerData.setTimestamp(ActionType.IN_UNLOADED_CHUNK);
                }
                break;

            default:
                break;
        }

        // Handles packet checks.
        handlePacketChecks(playerData, event, true);
    }

    /**
     * Handle the checks for the given packet event.
     *
     * @param playerData The player data.
     * @param event      The packet event.
     */
    private void handlePacketChecks(@NotNull PlayerData playerData,
                                    @NotNull CancellableNMSPacketEvent event,
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
    private void handleRotationChecks(@NotNull PlayerData playerData, RotationEvent event) {
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
    private void handleMovementChecks(@NotNull PlayerData playerData, MovementEvent event) {
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

    /**
     * Checks for valid SteerVehicle packets.
     *
     * @param player       The player.
     * @param event        The packet event.
     * @param steerVehicle The SteerVehicle packet.
     * @param value        The value to check.
     */
    private void steerVehicleCheck(Player player, PacketPlayReceiveEvent event,
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

    /**
     * Get the packet class from the given packet id.
     *
     * @param packetId The packet id.
     * @return The packet class.
     */
    public static Class<?> getPacketFromId(Byte packetId) {
        return PacketType.packetIDMap.entrySet().stream()
                .filter(entry -> Objects.equals(entry.getValue(), packetId))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }
}
