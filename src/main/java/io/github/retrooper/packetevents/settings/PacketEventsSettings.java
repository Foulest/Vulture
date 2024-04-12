package io.github.retrooper.packetevents.settings;

import io.github.retrooper.packetevents.utils.server.ServerVersion;
import lombok.Getter;

/**
 * Packet Events' settings.
 *
 * @author retrooper
 * @since 1.5.8
 */
@Getter
public class PacketEventsSettings {

    private boolean locked;
    private ServerVersion fallbackServerVersion = ServerVersion.v_1_8_8;

    /**
     * This method locks the settings.
     * If the settings are locked, you won't be able to modify any settings using the setters.
     */
    public void lock() {
        this.locked = true;
    }

    /**
     * This is the server version PacketEvents should assume the server is when detecting
     * the server version fails using the Bukkit API.
     * This seems to be most common on 1.7.10 paper forks.
     *
     * @param version ServerVersion
     * @return Settings instance.
     */
    public PacketEventsSettings fallbackServerVersion(ServerVersion version) {
        if (!locked) {
            this.fallbackServerVersion = version;
        }
        return this;
    }
}
