package net.foulest.vulture.check.type.killaura;

import io.github.retrooper.packetevents.event.eventtypes.CancellableNMSPacketEvent;
import io.github.retrooper.packetevents.packettype.PacketType;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.play.in.useentity.WrappedPacketInUseEntity;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;

@CheckInfo(name = "KillAura (B)", type = CheckType.KILLAURA)
public class KillAuraB extends Check {

    private boolean sentInteract;
    private boolean sentAttack;

    public KillAuraB(PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(CancellableNMSPacketEvent event, byte packetId,
                       NMSPacket nmsPacket, Object packet, long timestamp) {
        if (PacketType.Play.Client.Util.isInstanceOfFlying(packetId)) {
            sentInteract = false;
            sentAttack = false;

        } else if (packetId == PacketType.Play.Client.USE_ENTITY) {
            WrappedPacketInUseEntity useEntity = new WrappedPacketInUseEntity(nmsPacket);
            WrappedPacketInUseEntity.EntityUseAction action = useEntity.getAction();

            if (action == WrappedPacketInUseEntity.EntityUseAction.ATTACK) {
                sentAttack = true;
            } else if (action == WrappedPacketInUseEntity.EntityUseAction.INTERACT
                    || action == WrappedPacketInUseEntity.EntityUseAction.INTERACT_AT) {
                sentInteract = true;
            }

        } else if (packetId == PacketType.Play.Client.BLOCK_PLACE && sentAttack && !sentInteract) {
            flag(false);
        }
    }
}
