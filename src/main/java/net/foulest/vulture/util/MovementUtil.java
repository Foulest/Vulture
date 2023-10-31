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

    public static float getBaseSpeed(@NonNull Player player) {
        return 0.26f + (getPotionEffectLevel(player, PotionEffectType.SPEED) * 0.03001f)
                + ((player.getWalkSpeed() - 0.2f) * 1.6f);
    }

    public static int getPotionEffectLevel(@NonNull Player player, @NonNull PotionEffectType effectType) {
        for (PotionEffect effect : player.getActivePotionEffects()) {
            if (effect.getType().getName().equals(effectType.getName())) {
                return effect.getAmplifier() + 1;
            }
        }
        return 0;
    }

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
