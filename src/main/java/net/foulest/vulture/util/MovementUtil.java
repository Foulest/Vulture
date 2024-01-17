package net.foulest.vulture.util;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

@Getter
@Setter
public class MovementUtil {

    /**
     * Checks if the player's Y level is a whole number.
     * This is used to check if the player is on the ground.
     *
     * @param y The player's Y level.
     * @return Whether or not the player's Y level is a whole number.
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
    public static float getBaseSpeed(@NonNull Player player) {
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
    public static int getPotionEffectLevel(@NonNull Player player, @NonNull PotionEffectType effectType) {
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
    public static int getDepthStriderLevel(@NonNull Player player) {
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
