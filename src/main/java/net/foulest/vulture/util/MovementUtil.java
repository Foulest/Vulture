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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MovementUtil {

    /**
     * Checks if the player's Y level is divisible by 0.015625.
     * This is used to check if the player is on the ground.
     *
     * @param y The player's Y level.
     * @return Whether the player's Y level is divisible by 0.015625
     */
    public static boolean isYLevel(double y) {
        return y % 0.015625 == 0.0;
    }

    /**
     * Gets the player's base speed.
     *
     * @param player The player to get the base speed of.
     * @return The player's base speed.
     */
    public static float getBaseSpeed(Player player) {
        return 0.26f + (getPotionEffectLevel(player, PotionEffectType.SPEED) * 0.03001f)
                + ((player.getWalkSpeed() - 0.2f) * 1.6f);
    }

    /**
     * Gets the potion effect level of the specified effect type.
     *
     * @param player     The player to get the potion effect level of.
     * @param effectType The effect type to get the potion effect level of.
     * @return The potion effect level of the specified effect type.
     */
    public static int getPotionEffectLevel(@NotNull Player player, PotionEffectType effectType) {
        for (PotionEffect effect : player.getActivePotionEffects()) {
            if (effect.getType().getName().equals(effectType.getName())) {
                return effect.getAmplifier() + 1;
            }
        }
        return 0;
    }

    /**
     * Gets the player's depth strider level.
     *
     * @param player The player to get the depth strider level of.
     * @return The player's depth strider level.
     */
    public static int getDepthStriderLevel(@NotNull Player player) {
        ItemStack boots = player.getInventory().getBoots();

        if (boots != null) {
            Enchantment depthStrider = Enchantment.getByName("DEPTH_STRIDER");

            if (boots.getEnchantments().containsKey(depthStrider)) {
                return boots.getEnchantments().get(depthStrider);
            }
        }
        return 0;
    }
}
