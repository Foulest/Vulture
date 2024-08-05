package io.github.retrooper.packetevents.packetwrappers.play.out.updatetime;

import io.github.retrooper.packetevents.packettype.PacketTypeClasses;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;
import io.github.retrooper.packetevents.packetwrappers.api.SendableWrapper;
import lombok.ToString;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

@ToString
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
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
        }
    }

    private long getWorldAgeTicks() {
        if (nmsPacket != null) {
            return readLong(0);
        }
        return worldAgeTicks;
    }

    public void setWorldAgeTicks(long ticks) {
        if (nmsPacket != null) {
            writeLong(0, ticks);
        } else {
            worldAgeTicks = ticks;
        }
    }

    private long getTimeOfDayTicks() {
        if (nmsPacket != null) {
            return readLong(1);
        }
        return timeOfDayTicks;
    }

    public void setTimeOfDayTicks(long ticks) {
        if (nmsPacket != null) {
            writeLong(1, ticks);
        } else {
            timeOfDayTicks = ticks;
        }
    }

    @Override
    public Object asNMSPacket() throws InvocationTargetException, InstantiationException, IllegalAccessException {
        return packetConstructor.newInstance(getWorldAgeTicks(), getTimeOfDayTicks(), true);
    }
}
