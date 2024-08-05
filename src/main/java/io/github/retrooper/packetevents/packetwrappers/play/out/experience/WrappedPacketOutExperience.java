package io.github.retrooper.packetevents.packetwrappers.play.out.experience;

import io.github.retrooper.packetevents.packettype.PacketTypeClasses;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;
import io.github.retrooper.packetevents.packetwrappers.api.SendableWrapper;
import lombok.AllArgsConstructor;
import lombok.ToString;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

@ToString
@AllArgsConstructor
public class WrappedPacketOutExperience extends WrappedPacket implements SendableWrapper {

    private static Constructor<?> packetConstructor;
    private float experienceBar;
    private int experienceLevel;
    private int totalExperience;

    public WrappedPacketOutExperience(NMSPacket packet) {
        super(packet);
    }

    @Override
    protected void load() {
        try {
            packetConstructor = PacketTypeClasses.Play.Server.EXPERIENCE.getConstructor(float.class, int.class, int.class);
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
        }
    }

    private float getExperienceBar() {
        if (nmsPacket != null) {
            return readFloat(0);
        } else {
            return experienceBar;
        }
    }

    public void setExperienceBar(float experienceBar) {
        if (nmsPacket != null) {
            writeFloat(0, experienceBar);
        } else {
            this.experienceBar = experienceBar;
        }
    }

    private int getExperienceLevel() {
        if (nmsPacket != null) {
            return readInt(0);
        } else {
            return experienceLevel;
        }
    }

    public void setExperienceLevel(int experienceLevel) {
        if (nmsPacket != null) {
            writeInt(0, experienceLevel);
        } else {
            this.experienceLevel = experienceLevel;
        }
    }

    private int getTotalExperience() {
        if (nmsPacket != null) {
            return readInt(1);
        } else {
            return totalExperience;
        }
    }

    public void setTotalExperience(int totalExperience) {
        if (nmsPacket != null) {
            writeInt(1, totalExperience);
        } else {
            this.totalExperience = totalExperience;
        }
    }

    @Override
    public Object asNMSPacket() throws InvocationTargetException, InstantiationException, IllegalAccessException {
        return packetConstructor.newInstance(getExperienceBar(), getExperienceLevel(), getTotalExperience());
    }
}
