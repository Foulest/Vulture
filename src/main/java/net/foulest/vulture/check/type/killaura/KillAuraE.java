package net.foulest.vulture.check.type.killaura;

import io.github.retrooper.packetevents.event.eventtypes.CancellableNMSPacketEvent;
import io.github.retrooper.packetevents.packettype.PacketType;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import lombok.NonNull;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.util.MathUtil;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

@CheckInfo(name = "KillAura (E)", type = CheckType.KILLAURA)
public class KillAuraE extends Check {

    private int buffer;

    public KillAuraE(@NonNull PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(@NonNull CancellableNMSPacketEvent event, byte packetId,
                       @NonNull NMSPacket nmsPacket, @NonNull Object packet, long timestamp) {
        if (PacketType.Play.Client.Util.isInstanceOfFlying(packetId)) {
            Player lastTarget = playerData.getLastTarget();
            Entity vehicle = player.getVehicle();

            int lastAttackTick = playerData.getLastAttackTick();
            int lastServerPositionTick = playerData.getLastServerPositionTick();

            long transPing = playerData.getTransPing();

            if (lastTarget != null && vehicle == null
                    && lastAttackTick <= 1 && lastServerPositionTick > 60) {
                Location playerEyeLocation = player.getEyeLocation();
                Location targetEyeLocation = lastTarget.getEyeLocation();

                double range = playerEyeLocation.clone().toVector().setY(0.0).distance(targetEyeLocation.clone().toVector().setY(0.0));
                Vector vec = lastTarget.getLocation().clone().toVector().setY(0.0).subtract(playerEyeLocation.clone().toVector().setY(0.0));
                float angle = playerEyeLocation.getDirection().angle(vec);

                double direction = MathUtil.getDirection(playerEyeLocation, targetEyeLocation);
                double dist = MathUtil.getDistanceBetweenAngles360(playerEyeLocation.getYaw(), direction);
                double maxAngle = 35.0 + MathUtil.pingFormula(transPing) + 5;

                if ((dist > maxAngle || angle > 2) && range > 1.5) {
                    if (++buffer > 5) {
                        buffer = 0;
                        flag("dist=" + dist + " maxAngle=" + maxAngle);
                    }
                } else if (dist < maxAngle && angle < 2) {
                    buffer = Math.max(buffer - 1, 0);
                }
            }
        }
    }
}
