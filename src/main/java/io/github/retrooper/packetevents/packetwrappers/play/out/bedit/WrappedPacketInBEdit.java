package io.github.retrooper.packetevents.packetwrappers.play.out.bedit;

import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;
import io.github.retrooper.packetevents.utils.enums.EnumUtil;
import io.github.retrooper.packetevents.utils.nms.NMSUtils;
import io.github.retrooper.packetevents.utils.player.Hand;
import io.github.retrooper.packetevents.utils.server.ServerVersion;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

/**
 * @author Tecnio
 */
public class WrappedPacketInBEdit extends WrappedPacket {

    private static boolean v_1_13;
    private static boolean v_1_17;

    public WrappedPacketInBEdit(NMSPacket packet) {
        super(packet);
    }

    @Override
    protected void load() {
        v_1_13 = version.isNewerThanOrEquals(ServerVersion.v_1_13);
        v_1_17 = version.isNewerThanOrEquals(ServerVersion.v_1_17);
    }

    public ItemStack getItemStack() {
        return readItemStack(0);
    }

    public void setItemStack(ItemStack itemStack) {
        writeItemStack(0, itemStack);
    }

    public boolean isSigning() {
        return readBoolean(0);
    }

    public void setSigning(boolean signing) {
        writeBoolean(0, signing);
    }

    @SupportedVersions(ranges = {ServerVersion.v_1_13, ServerVersion.ERROR})
    public Optional<Hand> getHand() {
        if (v_1_17) {
            return Optional.of(Hand.values()[readInt(0)]);
        } else if (v_1_13) {
            Enum<?> enumConst = readEnumConstant(0, NMSUtils.enumHandClass);
            return Optional.of(Hand.valueOf(enumConst.name()));
        }
        return Optional.empty();
    }

    @SupportedVersions(ranges = {ServerVersion.v_1_13, ServerVersion.ERROR})
    public void setHand(Hand hand) {
        if (v_1_17) {
            writeInt(0, hand.ordinal());
        } else if (v_1_13) {
            Enum<?> enumConst = EnumUtil.valueOf(NMSUtils.enumHandClass, hand.name());
            writeEnumConstant(0, enumConst);
        }
    }
}
