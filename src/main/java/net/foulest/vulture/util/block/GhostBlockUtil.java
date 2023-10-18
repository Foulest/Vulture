package net.foulest.vulture.util.block;

import io.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.packetwrappers.play.out.blockchange.WrappedPacketOutBlockChange;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class GhostBlockUtil {

    /**
     * Updates the blocks surrounding a player.
     * This removes "ghost blocks" that can be seen by the player.
     * These blocks cause false flags due to the de-sync between the client and server.
     * Some of the updated blocks may be visually different, but it's better than false flags.
     *
     * @param player The player to update.
     */
    public static void update(@NonNull Player player) {
        Location location = player.getLocation();
        List<Location> blocks = new ArrayList<>();

        // Adds the blocks surrounding the player to a list.
        blocks.add(location);
        blocks.add(location.clone().add(0, 1, 0));
        blocks.add(location.clone().add(1, 1, 0));
        blocks.add(location.clone().add(0, 1, 1));
        blocks.add(location.clone().add(1, 1, 1));
        blocks.add(location.clone().add(0, 2, 0));
        blocks.add(location.clone().add(1, 2, 0));
        blocks.add(location.clone().add(0, 2, 1));
        blocks.add(location.clone().add(1, 2, 1));

        // Sends a block change packet to the player for each block.
        for (Location loc : blocks) {
            PacketEvents.get().getPlayerUtils().sendPacket(player,
                    new WrappedPacketOutBlockChange(loc, loc.getBlock().getType()));
        }
    }
}
