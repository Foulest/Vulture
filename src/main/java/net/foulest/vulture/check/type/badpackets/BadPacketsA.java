package net.foulest.vulture.check.type.badpackets;

import io.github.retrooper.packetevents.event.eventtypes.CancellableNMSPacketEvent;
import io.github.retrooper.packetevents.packettype.PacketType;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.utils.player.ClientVersion;
import lombok.NonNull;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.processor.type.PacketProcessor;
import net.foulest.vulture.util.KickUtil;
import org.bukkit.GameMode;

import java.util.HashMap;
import java.util.Map;

@CheckInfo(name = "BadPackets (A)", type = CheckType.BADPACKETS, punishable = false,
        description = "Detects sending too many packets in the same tick.")
public class BadPacketsA extends Check {

    private final Map<Byte, Integer> packetCounts = new HashMap<>();

    public BadPacketsA(@NonNull PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(@NonNull CancellableNMSPacketEvent event, byte packetId,
                       @NonNull NMSPacket nmsPacket, @NonNull Object packet, long timestamp) {
        // Checks the player for exemptions.
        if (PacketType.Play.Client.Util.isInstanceOfFlying(packetId)) {
            packetCounts.clear();
            return;
        }

        if (isExceededPacketLimit(packetId)) {
            // Checks the player for exemptions.
            if (playerData.isInCamera() && packetId == -86) {
                return;
            }

            KickUtil.kickPlayer(player, event, "Sent too many packets"
                    + " (" + PacketProcessor.getPacketFromId(packetId).getSimpleName() + ")"
                    + " (1_8: " + playerData.getVersion().isOlderThanOrEquals(ClientVersion.v_1_8) + ")");
        }
    }

    private boolean isExceededPacketLimit(byte packetId) {
        packetCounts.putIfAbsent(packetId, 0);
        int count = packetCounts.get(packetId);
        packetCounts.put(packetId, count + 1);

        boolean olderThan1_8 = playerData.getVersion().isOlderThanOrEquals(ClientVersion.v_1_8);
        boolean creative = player.getGameMode() == GameMode.CREATIVE;

        switch (packetId) {
            case PacketType.Play.Client.ADVANCEMENTS:
            case PacketType.Play.Client.ABILITIES:
            case PacketType.Play.Client.AUTO_RECIPE:
            case PacketType.Play.Client.BEACON:
            case PacketType.Play.Client.BOAT_MOVE:
            case PacketType.Play.Client.B_EDIT:
            case PacketType.Play.Client.CHAT:
            case PacketType.Play.Client.CLIENT_COMMAND:
            case PacketType.Play.Client.DIFFICULTY_LOCK:
            case PacketType.Play.Client.DIFFICULTY_CHANGE:
            case PacketType.Play.Client.ENCHANT_ITEM:
            case PacketType.Play.Client.ENTITY_NBT_QUERY:
            case PacketType.Play.Client.ITEM_NAME:
            case PacketType.Play.Client.PICK_ITEM:
            case PacketType.Play.Client.PONG:
            case PacketType.Play.Client.RECIPE_DISPLAYED:
            case PacketType.Play.Client.RESOURCE_PACK_STATUS:
            case PacketType.Play.Client.SET_COMMAND_BLOCK:
            case PacketType.Play.Client.SET_COMMAND_MINECART:
            case PacketType.Play.Client.SPECTATE:
            case PacketType.Play.Client.TR_SEL:
            case PacketType.Play.Client.TELEPORT_ACCEPT:
            case PacketType.Play.Client.TILE_NBT_QUERY:
            case PacketType.Play.Client.UPDATE_SIGN:
            case PacketType.Play.Client.USE_ITEM:
            case PacketType.Play.Client.WINDOW_CLICK:
                return count > 1;

            case PacketType.Play.Client.BLOCK_PLACE:
                return count > 2;

            case PacketType.Play.Client.CUSTOM_PAYLOAD:
                return count > 26;

            case PacketType.Play.Client.ARM_ANIMATION:
                return count > 20;

            case PacketType.Play.Client.BLOCK_DIG:
                return count > 3;

            case PacketType.Play.Client.USE_ENTITY:
                return count > (olderThan1_8 ? 3 : 6);

            case PacketType.Play.Client.CLOSE_WINDOW:
                return count > (olderThan1_8 ? 1 : 2);

            case PacketType.Play.Client.HELD_ITEM_SLOT:
                return count > (olderThan1_8 ? 1 : 3);

            case PacketType.Play.Client.ENTITY_ACTION:
                return count > (creative || !olderThan1_8 ? 2 : 1);

            case PacketType.Play.Client.SET_CREATIVE_SLOT:
                return count > 60;

            case PacketType.Play.Client.TAB_COMPLETE:
                return count > (olderThan1_8 ? 1 : 30);
        }

        return false;
    }
}
