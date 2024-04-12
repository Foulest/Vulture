package io.github.retrooper.packetevents.utils.versionlookup.viaversion;

import com.viaversion.viaversion.api.Via;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ViaVersionAccessorImpl implements ViaVersionAccessor {

    @Override
    public int getProtocolVersion(@NotNull Player player) {
        return Via.getAPI().getPlayerVersion(player.getUniqueId());
    }
}
