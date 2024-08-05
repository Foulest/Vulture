package io.github.retrooper.packetevents.packetwrappers.play.out.abilities;

import io.github.retrooper.packetevents.packettype.PacketTypeClasses;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;
import io.github.retrooper.packetevents.packetwrappers.api.SendableWrapper;
import io.github.retrooper.packetevents.packetwrappers.api.WrapperPacketWriter;
import io.github.retrooper.packetevents.utils.nms.NMSUtils;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

@ToString
public final class WrappedPacketOutAbilities extends WrappedPacket implements SendableWrapper {

    private static Constructor<?> packetConstructor;
    private static Constructor<?> playerAbilitiesConstructor;
    private boolean vulnerable;
    private boolean flying;
    private boolean allowFlight;
    private boolean instantBuild;
    private float flySpeed;
    private float walkSpeed;

    public WrappedPacketOutAbilities(NMSPacket packet) {
        super(packet);
    }

    public WrappedPacketOutAbilities(boolean isVulnerable, boolean isFlying, boolean allowFlight,
                                     boolean canBuildInstantly, float flySpeed, float walkSpeed) {
        vulnerable = isVulnerable;
        flying = isFlying;
        this.allowFlight = allowFlight;
        instantBuild = canBuildInstantly;
        this.flySpeed = flySpeed;
        this.walkSpeed = walkSpeed;
    }

    @Override
    protected void load() {
        Class<?> playerAbilitiesClass;

        try {
            playerAbilitiesClass = NMSUtils.getNMSClass("PlayerAbilities");
        } catch (ClassNotFoundException ex) {
            playerAbilitiesClass = NMSUtils.getNMClassWithoutException("world.entity.player.PlayerAbilities");
        }

        if (playerAbilitiesClass != null) {
            try {
                playerAbilitiesConstructor = playerAbilitiesClass.getConstructor();
            } catch (NoSuchMethodException ex) {
                ex.printStackTrace();
            }
        }

        try {
            packetConstructor = PacketTypeClasses.Play.Server.ABILITIES.getConstructor(playerAbilitiesClass);
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
        }
    }

    public boolean isVulnerable() {
        if (nmsPacket != null) {
            return readBoolean(0);
        } else {
            return vulnerable;
        }
    }

    public void setVulnerable(boolean isVulnerable) {
        if (nmsPacket != null) {
            writeBoolean(0, isVulnerable);
        } else {
            vulnerable = isVulnerable;
        }
    }

    public boolean isFlying() {
        if (nmsPacket != null) {
            return readBoolean(1);
        } else {
            return flying;
        }
    }

    public void setFlying(boolean isFlying) {
        if (nmsPacket != null) {
            writeBoolean(1, isFlying);
        } else {
            flying = isFlying;
        }
    }

    public boolean isFlightAllowed() {
        if (nmsPacket != null) {
            return readBoolean(2);
        } else {
            return allowFlight;
        }
    }

    public void setFlightAllowed(boolean isFlightAllowed) {
        if (nmsPacket != null) {
            writeBoolean(2, isFlightAllowed);
        } else {
            allowFlight = isFlightAllowed;
        }
    }

    public boolean canBuildInstantly() {
        if (nmsPacket != null) {
            return readBoolean(3);
        } else {
            return instantBuild;
        }
    }

    public void setCanBuildInstantly(boolean canBuildInstantly) {
        if (nmsPacket != null) {
            writeBoolean(3, canBuildInstantly);
        } else {
            instantBuild = canBuildInstantly;
        }
    }

    private float getFlySpeed() {
        if (nmsPacket != null) {
            return readFloat(0);
        } else {
            return flySpeed;
        }
    }

    public void setFlySpeed(float flySpeed) {
        if (nmsPacket != null) {
            writeFloat(0, flySpeed);
        } else {
            this.flySpeed = flySpeed;
        }
    }

    private float getWalkSpeed() {
        if (nmsPacket != null) {
            return readFloat(1);
        } else {
            return walkSpeed;
        }
    }

    public void setWalkSpeed(float walkSpeed) {
        if (nmsPacket != null) {
            writeFloat(1, walkSpeed);
        } else {
            this.walkSpeed = walkSpeed;
        }
    }

    private static Object getPlayerAbilities(boolean vulnerable, boolean flying, boolean flightAllowed,
                                             boolean canBuildInstantly, float flySpeed, float walkSpeed) {
        Object instance = null;

        try {
            instance = playerAbilitiesConstructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        WrapperPacketWriter wrapper = new WrappedPacket(new NMSPacket(instance));
        wrapper.writeBoolean(0, vulnerable);
        wrapper.writeBoolean(1, flying);
        wrapper.writeBoolean(2, flightAllowed);
        wrapper.writeBoolean(3, canBuildInstantly);
        wrapper.writeFloat(0, flySpeed);
        wrapper.writeFloat(1, walkSpeed);
        return instance;
    }

    @Override
    public @NotNull Object asNMSPacket() throws InvocationTargetException, InstantiationException, IllegalAccessException {
        return packetConstructor.newInstance(getPlayerAbilities(isVulnerable(), isFlying(),
                isFlightAllowed(), canBuildInstantly(), getFlySpeed(), getWalkSpeed()));
    }
}
