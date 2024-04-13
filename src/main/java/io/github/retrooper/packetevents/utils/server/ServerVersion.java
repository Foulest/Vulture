package io.github.retrooper.packetevents.utils.server;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Server Version.
 * This is a nice wrapper over minecraft's protocol versions.
 * You won't have to memorize the protocol version, just memorize the server version you see in the launcher.
 *
 * @author retrooper
 * @see <a href="https://wiki.vg/Protocol_version_numbers">https://wiki.vg/Protocol_version_numbers</a>
 * @since 1.6.9
 */
public class ServerVersion {

    private static final String NMS_VERSION_SUFFIX = Bukkit.getServer().getClass().getPackage().getName()
            .replace(".", ",").split(",")[3];

    public static String getNMSSuffix() {
        return NMS_VERSION_SUFFIX;
    }

    @Contract(pure = true)
    public static @NotNull String getNMSDirectory() {
        return "net.minecraft.server." + getNMSSuffix();
    }

    @Contract(pure = true)
    public static @NotNull String getOBCDirectory() {
        return "org.bukkit.craftbukkit." + (getNMSSuffix());
    }
}
