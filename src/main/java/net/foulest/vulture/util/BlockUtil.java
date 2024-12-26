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
import net.foulest.vulture.util.data.ConcurrentStream;
import net.foulest.vulture.util.raytrace.BoundingBox;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Data
public class BlockUtil {

    private static boolean isPlayerInUnloadedChunk(@NotNull Player player) {
        Location location = player.getLocation();
        int blockX = location.getBlockX();
        int blockZ = location.getBlockZ();
        return !location.getWorld().isChunkLoaded(blockX >> 4, blockZ >> 4);
    }

    private static BoundingBox getPlayerCustomBoundingBox(Player player, double expandXZ,
                                                          double expandMin, double expandMax) {
        return new BoundingBox(player)
                .expand(expandXZ, 0.0, expandXZ)
                .expandMin(0.0, expandMin, 0.0)
                .expandMax(0.0, expandMax, 0.0);
    }

    @Contract("_, _ -> new")
    private static @NotNull ConcurrentStream<Block> getCollidingBlocks(Player player, @NotNull BoundingBox boundingBox) {
        List<Block> collidingBlocks = boundingBox.getCollidingBlocks(player);
        return new ConcurrentStream<>(collidingBlocks, false);
    }

    private static boolean collidesWithSolid(Player player, BoundingBox boundingBox) {
        ConcurrentStream<Block> collidingBlocks = getCollidingBlocks(player, boundingBox);

        return collidingBlocks.any(block -> {
            Material type = block.getType();

            return type.isSolid()
                    || type == Material.WATER_LILY
                    || type == Material.FLOWER_POT
                    || type == Material.CARPET
                    || type == Material.SNOW
                    || type == Material.SKULL;
        });
    }

    public static boolean isAgainstBlock(Player player) {
        if (isPlayerInUnloadedChunk(player)) {
            return false;
        }

        BoundingBox boundingBox = getPlayerCustomBoundingBox(player, 0.01, 0.0, 0.0);
        return collidesWithSolid(player, boundingBox);
    }

    public static boolean isNearPortal(Player player) {
        if (isPlayerInUnloadedChunk(player)) {
            return false;
        }

        BoundingBox boundingBox = getPlayerCustomBoundingBox(player, 0.01, 0.0, 0.0);
        ConcurrentStream<Block> collidingBlocks = getCollidingBlocks(player, boundingBox);

        return collidingBlocks.any(block -> {
            Material type = block.getType();
            return type == Material.PORTAL;
        });
    }
}
