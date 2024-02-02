package net.foulest.vulture.util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class BeaconUtil {

    private static final List<Material> beaconTierBlocks = Arrays.asList(
            Material.IRON_BLOCK, Material.GOLD_BLOCK, Material.DIAMOND_BLOCK, Material.EMERALD_BLOCK);

    /**
     * Gets the tier of a beacon. Credit to Msrules123 for the code.
     * <a href="https://www.spigotmc.org/threads/how-to-get-tier-of-beacon.316432/page-2#post-2984796">...</a>
     *
     * @param beaconLocation The location of the beacon.
     * @return The tier of the beacon.
     */
    public static int getTier(@NotNull Location beaconLocation) {
        Block beaconBlock = beaconLocation.getBlock();
        int calculatedTier = 0;

        for (int tierCounter = 1; tierCounter < 5; tierCounter++) {
            if (beaconLocation.getBlockY() - tierCounter < 0) {
                return calculatedTier;
            }

            // Check blocks in a diamond shape pattern around the beacon
            for (int x = -tierCounter; x <= tierCounter; x++) {
                int zWidth = tierCounter - Math.abs(x); // Adjust z range based on distance from center

                for (int z = -zWidth; z <= zWidth; z++) {
                    if (!beaconTierBlocks.contains(beaconBlock.getRelative(x, -tierCounter, z).getType())) {
                        return calculatedTier;
                    }
                }
            }

            calculatedTier++;
        }
        return calculatedTier;
    }
}
