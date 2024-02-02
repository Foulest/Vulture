package net.foulest.vulture.check.type.killaura;

import io.github.retrooper.packetevents.event.eventtypes.CancellableNMSPacketEvent;
import io.github.retrooper.packetevents.packettype.PacketType;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.play.in.flying.WrappedPacketInFlying;
import io.github.retrooper.packetevents.packetwrappers.play.in.useentity.WrappedPacketInUseEntity;
import lombok.NonNull;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.util.MovementUtil;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

@CheckInfo(name = "KillAura (J)", type = CheckType.KILLAURA)
public class KillAuraJ extends Check {

    private double buffer;
    private double lastDeltaXZ;
    private int hits;

    public KillAuraJ(PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(CancellableNMSPacketEvent event, byte packetId,
                       NMSPacket nmsPacket, Object packet, long timestamp) {
        if (PacketType.Play.Client.Util.isInstanceOfFlying(packetId)) {
            WrappedPacketInFlying flying = new WrappedPacketInFlying(nmsPacket);

            Location to = new Location(player.getWorld(), flying.getPosition().getX(), flying.getPosition().getY(),
                    flying.getPosition().getZ(), flying.getYaw(), flying.getPitch());
            Location from = playerData.getLastLocation();

            double deltaX = to.getX() - from.getX();
            double deltaZ = to.getZ() - from.getZ();
            double deltaXZ = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

            if (++hits <= 2 && playerData.isSprinting()) {
                double deltaXZDiff = Math.abs(deltaXZ - lastDeltaXZ);
                float baseSpeed = MovementUtil.getBaseSpeed(player);

                if (deltaXZ > baseSpeed) {
                    if (deltaXZDiff <= 0.01) {
                        if (++buffer > 9.5) {
                            flag(false);
                        }
                    } else {
                        buffer = Math.max(buffer - 0.75, 0);
                    }
                }
            }

            lastDeltaXZ = deltaXZ;

        } else if (packetId == PacketType.Play.Client.USE_ENTITY) {
            WrappedPacketInUseEntity useEntity = new WrappedPacketInUseEntity(nmsPacket);
            WrappedPacketInUseEntity.EntityUseAction action = useEntity.getAction();
            Entity entity = useEntity.getEntity();

            if (action == WrappedPacketInUseEntity.EntityUseAction.ATTACK && entity instanceof Player) {
                hits = 0;
            }
        }
    }
}
