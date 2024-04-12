package io.github.retrooper.packetevents.utils.versionlookup;

import io.github.retrooper.packetevents.utils.versionlookup.viaversion.ViaVersionLookupUtils;
import org.bukkit.entity.Player;

public class VersionLookupUtils {

    public static boolean isDependencyAvailable() {
        return ViaVersionLookupUtils.isAvailable();
    }

    public static int getProtocolVersion(Player player) {
        if (ViaVersionLookupUtils.isAvailable()) {
            return ViaVersionLookupUtils.getProtocolVersion(player);
        }
        return -1;
    }
}
