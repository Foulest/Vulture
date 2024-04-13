package io.github.retrooper.packetevents.packetwrappers.login.in.start;

import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;
import io.github.retrooper.packetevents.utils.gameprofile.GameProfileUtil;
import io.github.retrooper.packetevents.utils.gameprofile.WrappedGameProfile;
import io.github.retrooper.packetevents.utils.nms.NMSUtils;
import org.jetbrains.annotations.NotNull;

public class WrappedPacketLoginInStart extends WrappedPacket {

    public WrappedPacketLoginInStart(NMSPacket packet) {
        super(packet);
    }

    // TODO: Allow accessing game profile property
    public WrappedGameProfile getGameProfile() {
        return GameProfileUtil.getWrappedGameProfile(readObject(0, NMSUtils.gameProfileClass));
    }

    // TODO: Add support for game profile property
    public void setGameProfile(@NotNull WrappedGameProfile wrappedGameProfile) {
        Object gameProfile = GameProfileUtil.getGameProfile(wrappedGameProfile.getId(), wrappedGameProfile.getName());
        write(NMSUtils.gameProfileClass, 0, gameProfile);
    }
}
