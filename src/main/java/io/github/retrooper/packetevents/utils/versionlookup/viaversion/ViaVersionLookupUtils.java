package io.github.retrooper.packetevents.utils.versionlookup.viaversion;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ViaVersionLookupUtils {

    private ViaVersionLookupUtils() {
    }

    private static ViaVersionAccessor viaVersionAccessor;

    public static boolean isAvailable() {
        return Bukkit.getPluginManager().getPlugin("ViaVersion") != null;
    }

    public static int getProtocolVersion(Player player) {
        if (viaVersionAccessor == null) {
            try {
                Class.forName("com.viaversion.viaversion.api.Via");
                viaVersionAccessor = new ViaVersionAccessorImpl();
            } catch (Exception e) {
                viaVersionAccessor = new ViaVersionAccessorImplLegacy();
            }
        }
        return viaVersionAccessor.getProtocolVersion(player);
    }
}
