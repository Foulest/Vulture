package io.github.retrooper.packetevents.packetwrappers.play.in.armanimation;

import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;
import io.github.retrooper.packetevents.utils.player.Hand;

public class WrappedPacketInArmAnimation extends WrappedPacket {

    public WrappedPacketInArmAnimation(NMSPacket packet) {
        super(packet);
    }

    public Hand getHand() {
        return Hand.MAIN_HAND;
    }
}
