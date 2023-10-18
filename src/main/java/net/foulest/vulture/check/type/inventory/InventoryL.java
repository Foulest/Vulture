package net.foulest.vulture.check.type.inventory;

import io.github.retrooper.packetevents.event.eventtypes.CancellableNMSPacketEvent;
import io.github.retrooper.packetevents.event.impl.PacketPlayReceiveEvent;
import io.github.retrooper.packetevents.packettype.PacketType;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.play.in.blockdig.WrappedPacketInBlockDig;
import io.github.retrooper.packetevents.packetwrappers.play.in.clientcommand.WrappedPacketInClientCommand;
import io.github.retrooper.packetevents.packetwrappers.play.in.entityaction.WrappedPacketInEntityAction;
import lombok.NonNull;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.processor.type.PacketProcessor;

@CheckInfo(name = "Inventory (L)", type = CheckType.INVENTORY)
public class InventoryL extends Check {

    public InventoryL(@NonNull PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(@NonNull CancellableNMSPacketEvent event, byte packetId,
                       @NonNull NMSPacket nmsPacket, @NonNull Object packet, long timestamp) {
        if (!(event instanceof PacketPlayReceiveEvent)) {
            return;
        }

        if (playerData.isInventoryOpen()) {
            switch (packetId) {
                case PacketType.Play.Client.CLOSE_WINDOW:
                case PacketType.Play.Client.FLYING:
                case PacketType.Play.Client.HELD_ITEM_SLOT:
                case PacketType.Play.Client.KEEP_ALIVE:
                case PacketType.Play.Client.LOOK:
                case PacketType.Play.Client.POSITION:
                case PacketType.Play.Client.POSITION_LOOK:
                case PacketType.Play.Client.SETTINGS:
                case PacketType.Play.Client.SET_CREATIVE_SLOT:
                case PacketType.Play.Client.TRANSACTION:
                case PacketType.Play.Client.VEHICLE_MOVE:
                case PacketType.Play.Client.WINDOW_CLICK:
                    break;

                case PacketType.Play.Client.BLOCK_DIG:
                    WrappedPacketInBlockDig blockDig = new WrappedPacketInBlockDig(nmsPacket);
                    WrappedPacketInBlockDig.PlayerDigType digType = blockDig.getDigType();

                    if (digType == WrappedPacketInBlockDig.PlayerDigType.START_DESTROY_BLOCK
                            || digType == WrappedPacketInBlockDig.PlayerDigType.STOP_DESTROY_BLOCK
                            || digType == WrappedPacketInBlockDig.PlayerDigType.DROP_ALL_ITEMS
                            || digType == WrappedPacketInBlockDig.PlayerDigType.DROP_ITEM) {
                        flag("Sent invalid packet while in inventory: "
                                + PacketProcessor.getPacketFromId(packetId).getSimpleName()
                                + " (" + digType + ")");
                    }
                    break;

                case PacketType.Play.Client.CLIENT_COMMAND:
                    WrappedPacketInClientCommand clientCommand = new WrappedPacketInClientCommand(nmsPacket);
                    WrappedPacketInClientCommand.ClientCommand command = clientCommand.getClientCommand();

                    if (command != WrappedPacketInClientCommand.ClientCommand.OPEN_INVENTORY_ACHIEVEMENT) {
                        flag("Sent invalid packet while in inventory: "
                                + PacketProcessor.getPacketFromId(packetId).getSimpleName()
                                + " (" + command + ")");
                    }
                    break;

                case PacketType.Play.Client.ENTITY_ACTION:
                    WrappedPacketInEntityAction entityAction = new WrappedPacketInEntityAction(nmsPacket);
                    WrappedPacketInEntityAction.PlayerAction action = entityAction.getAction();

                    if (action == WrappedPacketInEntityAction.PlayerAction.START_SPRINTING
                            || action == WrappedPacketInEntityAction.PlayerAction.START_SNEAKING
                            || action == WrappedPacketInEntityAction.PlayerAction.RIDING_JUMP) {
                        flag("Sent invalid packet while in inventory: "
                                + PacketProcessor.getPacketFromId(packetId).getSimpleName()
                                + " (" + action + ")");
                    }
                    break;

                default:
                    flag("Sent invalid packet while in inventory: "
                            + PacketProcessor.getPacketFromId(packetId).getSimpleName());
                    break;
            }
        }
    }
}
