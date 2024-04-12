package io.github.retrooper.packetevents.packetwrappers.play.out.updatehealth;

import io.github.retrooper.packetevents.packettype.PacketTypeClasses;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;
import io.github.retrooper.packetevents.packetwrappers.api.SendableWrapper;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;

public final class WrappedPacketOutUpdateHealth extends WrappedPacket implements SendableWrapper {

    private static Constructor<?> packetConstructor;
    private float health;
    private float foodSaturation;
    private int food;

    public WrappedPacketOutUpdateHealth(NMSPacket packet) {
        super(packet);
    }

    public WrappedPacketOutUpdateHealth(float health, int food, float foodSaturation) {
        this.health = health;
        this.food = food;
        this.foodSaturation = foodSaturation;
    }

    @Override
    protected void load() {
        try {
            packetConstructor = PacketTypeClasses.Play.Server.UPDATE_HEALTH.getConstructor(float.class, int.class, float.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public float getHealth() {
        if (packet != null) {
            return readFloat(0);
        } else {
            return health;
        }
    }

    public void setHealth(float health) {
        if (packet != null) {
            writeFloat(0, health);
        } else {
            this.health = health;
        }
    }

    public float getFoodSaturation() {
        if (packet != null) {
            return readFloat(1);
        } else {
            return foodSaturation;
        }
    }

    public void setFoodSaturation(float foodSaturation) {
        if (packet != null) {
            writeFloat(0, foodSaturation);
        } else {
            this.foodSaturation = foodSaturation;
        }
    }

    public int getFood() {
        if (packet != null) {
            return readInt(0);
        } else {
            return food;
        }
    }

    public void setFood(int food) {
        if (packet != null) {
            writeInt(0, food);
        } else {
            this.food = food;
        }
    }

    @Override
    public @NotNull Object asNMSPacket() throws Exception {
        return packetConstructor.newInstance(getHealth(), getFood(), getFoodSaturation());
    }
}
