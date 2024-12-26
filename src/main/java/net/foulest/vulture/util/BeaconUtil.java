/*
 * Vulture - an advanced anti-cheat plugin designed for Minecraft 1.8.9 servers.
 * Copyright (C) 2024 Foulest (https://github.com/Foulest)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package net.foulest.vulture.util;

import lombok.Data;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

@Data
public class BeaconUtil {

    private static final List<Material> beaconTierBlocks = Arrays.asList(
            Material.IRON_BLOCK,
            Material.GOLD_BLOCK,
            Material.DIAMOND_BLOCK,
            Material.EMERALD_BLOCK
    );

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
                    Block relative = beaconBlock.getRelative(x, -tierCounter, z);
                    Material type = relative.getType();

                    if (!beaconTierBlocks.contains(type)) {
                        return calculatedTier;
                    }
                }
            }

            calculatedTier++;
        }
        return calculatedTier;
    }
}
