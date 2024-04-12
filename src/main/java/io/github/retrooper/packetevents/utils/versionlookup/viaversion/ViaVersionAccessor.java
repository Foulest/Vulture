package io.github.retrooper.packetevents.utils.versionlookup.viaversion;

import org.bukkit.entity.Player;

public interface ViaVersionAccessor {

    int getProtocolVersion(Player player);
}
