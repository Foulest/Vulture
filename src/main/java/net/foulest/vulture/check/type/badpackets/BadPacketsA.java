package net.foulest.vulture.check.type.badpackets;

import io.github.retrooper.packetevents.event.eventtypes.CancellableNMSPacketEvent;
import io.github.retrooper.packetevents.packettype.PacketType;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.play.in.setcreativeslot.WrappedPacketInSetCreativeSlot;
import io.github.retrooper.packetevents.utils.player.ClientVersion;
import net.foulest.vulture.action.ActionType;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.processor.type.PacketProcessor;
import net.foulest.vulture.util.KickUtil;
import net.foulest.vulture.util.MessageUtil;
import org.bukkit.GameMode;
import org.bukkit.Material;

import java.util.Map;
import java.util.logging.Level;

@CheckInfo(name = "BadPackets (A)", type = CheckType.BADPACKETS, punishable = false,
        description = "Detects sending too many packets in the same tick.")
public class BadPacketsA extends Check {

    public BadPacketsA(PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(CancellableNMSPacketEvent event, byte packetId,
                       NMSPacket nmsPacket, Object packet, long timestamp) {
        synchronized (playerData.getPacketCounts()) {
            Map<Byte, Integer> packetCounts = playerData.getPacketCounts();
            packetCounts.compute(packetId, (key, count) -> (count == null) ? 1 : count + 1);
            Integer count = packetCounts.get(packetId);

            if (count == null) {
                // This normally shouldn't happen, but it's possible
                // if the map is cleared after compute but before get
                return;
            }

            String packetName = PacketProcessor.getPacketFromId(packetId).getSimpleName();
            boolean olderThan1_8 = playerData.getVersion().isOlderThanOrEquals(ClientVersion.v_1_8);
            boolean inCreative = player.getGameMode() == GameMode.CREATIVE;
            long timeSinceDelayed = playerData.getTimeSince(ActionType.SERVER_PACKET_DELAYED);

            switch (packetId) {
                case PacketType.Play.Client.BLOCK_PLACE:
                case PacketType.Play.Client.HELD_ITEM_SLOT:
                    if (count >= (olderThan1_8 && inCreative ? 3 : 4)) {
                        if (timeSinceDelayed < 500L) {
                            MessageUtil.log(Level.INFO, "Cancelled packet " + packetName + " with count " + count);
                            event.setCancelled(true);
                            break;
                        }

                        KickUtil.kickPlayer(player, event, "packet=" + packetName + " count=" + count);
                    }
                    break;

                case PacketType.Play.Client.CUSTOM_PAYLOAD:
                case PacketType.Play.Client.ENTITY_ACTION:
                    if (count >= 3) {
                        if (timeSinceDelayed < 500L) {
                            MessageUtil.log(Level.INFO, "Cancelled packet " + packetName + " with count " + count);
                            event.setCancelled(true);
                            break;
                        }

                        KickUtil.kickPlayer(player, event, "packet=" + packetName + " count=" + count);
                    }
                    break;

                case PacketType.Play.Client.WINDOW_CLICK:
                    if (count >= (olderThan1_8 ? 52 : 6)) {
                        if (timeSinceDelayed < 500L) {
                            MessageUtil.log(Level.INFO, "Cancelled packet " + packetName + " with count " + count);
                            event.setCancelled(true);
                            break;
                        }

                        KickUtil.kickPlayer(player, event, "packet=" + packetName + " count=" + count);
                    }
                    break;

                case PacketType.Play.Client.BLOCK_DIG:
                case PacketType.Play.Client.USE_ENTITY:
                    if (count >= 4) {
                        if (timeSinceDelayed < 500L) {
                            MessageUtil.log(Level.INFO, "Cancelled packet " + packetName + " with count " + count);
                            event.setCancelled(true);
                            break;
                        }

                        KickUtil.kickPlayer(player, event, "packet=" + packetName + " count=" + count);
                    }
                    break;

                case PacketType.Play.Client.ARM_ANIMATION:
                    if (count >= 7) {
                        if (timeSinceDelayed < 500L) {
                            MessageUtil.log(Level.INFO, "Cancelled packet " + packetName + " with count " + count);
                            event.setCancelled(true);
                            break;
                        }

                        KickUtil.kickPlayer(player, event, "packet=" + packetName + " count=" + count);
                    }
                    break;

                case PacketType.Play.Client.SET_CREATIVE_SLOT:
                    WrappedPacketInSetCreativeSlot creativeSlot = new WrappedPacketInSetCreativeSlot(nmsPacket);

                    if (creativeSlot.getClickedItem().getType() == Material.AIR) {
                        if (count >= 230) {
                            if (timeSinceDelayed < 500L) {
                                MessageUtil.log(Level.INFO, "Cancelled packet " + packetName + " with count " + count);
                                event.setCancelled(true);
                                break;
                            }

                            KickUtil.kickPlayer(player, event, "packet=" + packetName + " count=" + count);
                        }
                    } else {
                        if (count >= (olderThan1_8 ? 9 : 15)) {
                            if (timeSinceDelayed < 500L) {
                                MessageUtil.log(Level.INFO, "Cancelled packet " + packetName + " with count " + count);
                                event.setCancelled(true);
                                break;
                            }

                            KickUtil.kickPlayer(player, event, "packet=" + packetName + " count=" + count
                                    + " slot=" + creativeSlot.getSlot()
                                    + " item=" + creativeSlot.getClickedItem().getType().name());
                        }
                    }
                    break;

                case PacketType.Play.Client.SETTINGS:
                    if (count >= (olderThan1_8 ? 2 : 41)) {
                        if (timeSinceDelayed < 500L) {
                            MessageUtil.log(Level.INFO, "Cancelled packet " + packetName + " with count " + count);
                            event.setCancelled(true);
                            break;
                        }

                        KickUtil.kickPlayer(player, event, "packet=" + packetName + " count=" + count);
                    }
                    break;

                case PacketType.Play.Client.STEER_VEHICLE:
                    if (count >= (playerData.getTimeSince(ActionType.LOGIN) < 2000L ? 13 : (olderThan1_8 ? 2 : 5))) {
                        if (timeSinceDelayed < 500L) {
                            MessageUtil.log(Level.INFO, "Cancelled packet " + packetName + " with count " + count);
                            event.setCancelled(true);
                            break;
                        }

                        KickUtil.kickPlayer(player, event, "packet=" + packetName + " count=" + count);
                    }
                    break;

                case PacketType.Play.Client.FLYING:
                case PacketType.Play.Client.LOOK:
                case PacketType.Play.Client.POSITION:
                case PacketType.Play.Client.POSITION_LOOK:
                case PacketType.Play.Client.KEEP_ALIVE:
                case PacketType.Play.Client.TRANSACTION:
                    break;

                default:
                    if (count >= 2) {
                        if (timeSinceDelayed < 500L) {
                            MessageUtil.log(Level.INFO, "Cancelled packet " + packetName + " with count " + count);
                            event.setCancelled(true);
                            break;
                        }

                        KickUtil.kickPlayer(player, event, "packet=" + packetName + " count=" + count);
                    }
                    break;
            }
        }
    }
}
