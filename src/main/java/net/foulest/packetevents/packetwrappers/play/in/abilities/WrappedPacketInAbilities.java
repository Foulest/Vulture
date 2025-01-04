package net.foulest.packetevents.packetwrappers.play.in.abilities;

import net.foulest.packetevents.packettype.PacketTypeClasses;
import net.foulest.packetevents.packetwrappers.NMSPacket;
import net.foulest.packetevents.packetwrappers.WrappedPacket;
import net.foulest.packetevents.utils.reflection.Reflection;

import java.util.Optional;

public final class WrappedPacketInAbilities extends WrappedPacket {

    private static boolean v_1_16_Mode;

    public WrappedPacketInAbilities(NMSPacket packet) {
        super(packet);
    }

    @Override
    protected void load() {
        v_1_16_Mode = Reflection.getField(PacketTypeClasses.Play.Client.ABILITIES, boolean.class, 1) == null;
    }

    public boolean isFlying() {
        return readBoolean(v_1_16_Mode ? 0 : 1);
    }

    public void setFlying(boolean flying) {
        writeBoolean(v_1_16_Mode ? 0 : 1, flying);
    }

    public Optional<Boolean> isVulnerable() {
        if (v_1_16_Mode) {
            return Optional.empty();
        }
        return Optional.of(readBoolean(0));
    }

    public void setVulnerable(boolean vulnerable) {
        if (!v_1_16_Mode) {
            writeBoolean(0, vulnerable);
        }
    }

    public Optional<Boolean> isFlightAllowed() {
        if (v_1_16_Mode) {
            return Optional.empty();
        }
        return Optional.of(readBoolean(2));
    }

    public void setFlightAllowed(boolean flightAllowed) {
        if (!v_1_16_Mode) {
            writeBoolean(2, flightAllowed);
        }
    }

    public Optional<Boolean> canInstantlyBuild() {
        if (v_1_16_Mode) {
            return Optional.empty();
        }
        return Optional.of(readBoolean(3));
    }

    public void setCanInstantlyBuild(boolean canInstantlyBuild) {
        if (!v_1_16_Mode) {
            writeBoolean(3, canInstantlyBuild);
        }
    }

    public Optional<Float> getFlySpeed() {
        if (v_1_16_Mode) {
            return Optional.empty();
        }
        return Optional.of(readFloat(0));
    }

    public void setFlySpeed(float flySpeed) {
        if (!v_1_16_Mode) {
            writeFloat(0, flySpeed);
        }
    }

    public Optional<Float> getWalkSpeed() {
        if (v_1_16_Mode) {
            return Optional.empty();
        }
        return Optional.of(readFloat(1));
    }

    public void setWalkSpeed(float walkSpeed) {
        if (!v_1_16_Mode) {
            writeFloat(1, walkSpeed);
        }
    }
}
