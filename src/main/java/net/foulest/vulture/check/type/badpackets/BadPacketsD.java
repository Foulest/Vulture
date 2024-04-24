package net.foulest.vulture.check.type.badpackets;

import io.github.retrooper.packetevents.event.eventtypes.CancellableNMSPacketEvent;
import io.github.retrooper.packetevents.packettype.PacketType;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.play.in.flying.WrappedPacketInFlying;
import net.foulest.vulture.action.ActionType;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.util.KickUtil;

@CheckInfo(name = "BadPackets (D)", type = CheckType.BADPACKETS, punishable = false,
        description = "Detects ignoring the mandatory Position packet.")
public class BadPacketsD extends Check {

    private long lastPosition;
    private long lastTransaction;

    public BadPacketsD(PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
        lastPosition = System.currentTimeMillis();
        lastTransaction = System.currentTimeMillis();
    }

    @Override
    public void handle(CancellableNMSPacketEvent event, byte packetId,
                       NMSPacket nmsPacket, Object packet, long timestamp) {
        if (PacketType.Play.Client.Util.isInstanceOfFlying(packetId)) {
            WrappedPacketInFlying flying = new WrappedPacketInFlying(nmsPacket);

            if (flying.isMoving()) {
                lastPosition = System.currentTimeMillis();
            }

        } else if (packetId == PacketType.Play.Client.TRANSACTION) {
            lastTransaction = System.currentTimeMillis();

        } else if (packetId == PacketType.Play.Client.KEEP_ALIVE) {
            checkDifference();
        }
    }

    public void checkDifference() {
        // Checks the player for exemptions.
        if (player.isDead() || player.isInsideVehicle()
                || playerData.getTicksSince(ActionType.LOGIN) < 200) {
            return;
        }

        long timeSincePosition = System.currentTimeMillis() - lastPosition;
        long timeSinceTransaction = System.currentTimeMillis() - lastTransaction;

        // If the player hasn't sent a Position packet in the last 5 seconds, kick them.
        if (timeSincePosition > 5000 && timeSinceTransaction < 1000) {
            KickUtil.kickPlayer(player, "Might be cancelling sending Position packets");
        }
    }
}
