package io.github.retrooper.packetevents.packetwrappers.login.out.success;

import io.github.retrooper.packetevents.packettype.PacketTypeClasses;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;
import io.github.retrooper.packetevents.packetwrappers.api.SendableWrapper;
import io.github.retrooper.packetevents.utils.gameprofile.GameProfileUtil;
import io.github.retrooper.packetevents.utils.gameprofile.WrappedGameProfile;
import io.github.retrooper.packetevents.utils.nms.NMSUtils;
import lombok.AllArgsConstructor;

import java.lang.reflect.Constructor;

@AllArgsConstructor
public class WrappedPacketLoginOutSuccess extends WrappedPacket implements SendableWrapper {

    private static Constructor<?> packetConstructor;
    private WrappedGameProfile wrappedGameProfile;

    public WrappedPacketLoginOutSuccess(NMSPacket packet) {
        super(packet);
    }

    @Override
    protected void load() {
        try {
            packetConstructor = PacketTypeClasses.Login.Server.SUCCESS.getConstructors()[1];
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // TODO: Support property reading in wrapped game profile
    public WrappedGameProfile getGameProfile() {
        if (packet != null) {
            return GameProfileUtil.getWrappedGameProfile(readObject(0, NMSUtils.gameProfileClass));
        } else {
            return this.wrappedGameProfile;
        }
    }

    // TODO: Support writing property in wrapped game profile
    public void setGameProfile(WrappedGameProfile wrappedGameProfile) {
        if (packet != null) {
            Object gameProfile = GameProfileUtil.getGameProfile(wrappedGameProfile.getId(), wrappedGameProfile.getName());
            write(NMSUtils.gameProfileClass, 0, gameProfile);
        } else {
            this.wrappedGameProfile = wrappedGameProfile;
        }
    }

    @Override
    public Object asNMSPacket() throws Exception {
        WrappedGameProfile gp = getGameProfile();
        // TODO: Support writing property in wrapped game profile
        return packetConstructor.newInstance(GameProfileUtil.getGameProfile(gp.getId(), gp.getName()));
    }
}
