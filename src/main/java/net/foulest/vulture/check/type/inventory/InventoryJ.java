package net.foulest.vulture.check.type.inventory;

import io.github.retrooper.packetevents.event.eventtypes.CancellableNMSPacketEvent;
import io.github.retrooper.packetevents.packettype.PacketType;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.play.in.blockplace.WrappedPacketInBlockPlace;
import io.github.retrooper.packetevents.utils.player.Direction;
import io.github.retrooper.packetevents.utils.vector.Vector3i;
import lombok.NonNull;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;

@CheckInfo(name = "Inventory (J)", type = CheckType.INVENTORY)
public class InventoryJ extends Check {

    private double lastX;
    private double lastY;
    private double lastZ;
    private Vector3i lastBlockPosition;
    private Float lastYaw;

    public InventoryJ(@NonNull PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(@NonNull CancellableNMSPacketEvent event, byte packetId,
                       @NonNull NMSPacket nmsPacket, @NonNull Object packet, long timestamp) {
        if (packetId == PacketType.Play.Client.BLOCK_PLACE) {
            WrappedPacketInBlockPlace blockPlace = new WrappedPacketInBlockPlace(nmsPacket);

            if (blockPlace.getDirection() != Direction.OTHER) {
                Vector3i blockPosition = blockPlace.getBlockPosition();
                int blockX = blockPosition.getX();
                int blockY = blockPosition.getY();
                int blockZ = blockPosition.getZ();

                if (lastBlockPosition != null && blockX == lastX && blockY == lastY && blockZ == lastZ
                        && Math.abs(blockX - lastBlockPosition.getX()) + Math.abs(blockZ - lastBlockPosition.getZ()) == 1) {
                    float yaw = playerData.getPlayer().getLocation().getYaw();

                    if (lastYaw != null) {
                        double yawDiff = Math.abs(yaw - lastYaw);

                        if (yawDiff > 20.0) {
                            flag("yawDiff=" + yawDiff);
                        }
                    }

                    lastYaw = yaw;
                }

                lastX = blockX;
                lastY = blockY;
                lastZ = blockZ;
                lastBlockPosition = blockPosition;
            }
        }
    }
}
