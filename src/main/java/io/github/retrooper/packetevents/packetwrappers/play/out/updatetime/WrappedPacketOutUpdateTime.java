package io.github.retrooper.packetevents.packetwrappers.play.out.updatetime;

import io.github.retrooper.packetevents.packettype.PacketTypeClasses;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;
import io.github.retrooper.packetevents.packetwrappers.api.SendableWrapper;

import java.lang.reflect.Constructor;

public class WrappedPacketOutUpdateTime extends WrappedPacket implements SendableWrapper {

    private static Constructor<?> packetConstructor;
    private long worldAgeTicks;
    private long timeOfDayTicks;

    public WrappedPacketOutUpdateTime(NMSPacket packet) {
        super(packet);
    }

    public WrappedPacketOutUpdateTime(long worldAgeTicks, long timeOfDayTicks) {
        this.worldAgeTicks = worldAgeTicks;
        this.timeOfDayTicks = timeOfDayTicks;
    }

    @Override
    protected void load() {
        try {
            packetConstructor = PacketTypeClasses.Play.Server.UPDATE_TIME.getConstructor(long.class, long.class, boolean.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public long getWorldAgeTicks() {
        if (packet != null) {
            return readLong(0);
        }
        return worldAgeTicks;
    }

    public void setWorldAgeTicks(long ticks) {
        if (packet != null) {
            writeLong(0, ticks);
        } else {
            this.worldAgeTicks = ticks;
        }
    }

    public long getTimeOfDayTicks() {
        if (packet != null) {
            return readLong(1);
        }
        return timeOfDayTicks;
    }

    public void setTimeOfDayTicks(long ticks) {
        if (packet != null) {
            writeLong(1, ticks);
        } else {
            this.timeOfDayTicks = ticks;
        }
    }

    @Override
    public Object asNMSPacket() throws Exception {
        return packetConstructor.newInstance(getWorldAgeTicks(), getTimeOfDayTicks(), true);
    }
}
