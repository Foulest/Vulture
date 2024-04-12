package io.github.retrooper.packetevents.packetwrappers.play.in.armanimation;

import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;
import io.github.retrooper.packetevents.utils.enums.EnumUtil;
import io.github.retrooper.packetevents.utils.nms.NMSUtils;
import io.github.retrooper.packetevents.utils.player.Hand;
import io.github.retrooper.packetevents.utils.server.ServerVersion;

public class WrappedPacketInArmAnimation extends WrappedPacket {

    private static boolean v_1_9;

    public WrappedPacketInArmAnimation(NMSPacket packet) {
        super(packet);
    }

    @Override
    protected void load() {
        v_1_9 = version.isNewerThanOrEquals(ServerVersion.v_1_9);
    }

    public Hand getHand() {
        if (v_1_9) {
            Enum<?> enumConst = readEnumConstant(0, NMSUtils.enumHandClass);
            return Hand.values()[enumConst.ordinal()];
        } else {
            return Hand.MAIN_HAND;
        }
    }

    public void setHand(Hand hand) {
        // Optimize to do nothing on legacy versions.
        // The protocol of the legacy versions only support one hand; the main hand.
        if (v_1_9) {
            Enum<?> enumConst = EnumUtil.valueByIndex(NMSUtils.enumHandClass, hand.ordinal());
            writeEnumConstant(0, enumConst);
        }
    }
}
