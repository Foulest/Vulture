package io.github.retrooper.packetevents.packetwrappers.play.out.helditemslot;

import io.github.retrooper.packetevents.packettype.PacketTypeClasses;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;
import io.github.retrooper.packetevents.packetwrappers.api.SendableWrapper;

import java.lang.reflect.Constructor;

public class WrappedPacketOutHeldItemSlot extends WrappedPacket implements SendableWrapper {

    private static Constructor<?> packetConstructor;
    private int slot;

    public WrappedPacketOutHeldItemSlot(NMSPacket packet) {
        super(packet);
    }

    public WrappedPacketOutHeldItemSlot(int slot) {
        this.slot = slot;
    }

    @Override
    protected void load() {
        try {
            packetConstructor = PacketTypeClasses.Play.Server.HELD_ITEM_SLOT.getConstructor(int.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public int getCurrentSelectedSlot() {
        if (packet == null) {
            return slot;
        }
        return readInt(0);
    }

    @Override
    public Object asNMSPacket() throws Exception {
        return packetConstructor.newInstance(slot);
    }
}
