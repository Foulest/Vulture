package io.github.retrooper.packetevents.utils.gameprofile;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;
import io.github.retrooper.packetevents.utils.nms.NMSUtils;
import io.github.retrooper.packetevents.utils.player.Skin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * 1.8 (and above) Mojang Game Profile util using the 1.8 (and above) Mojang API import location.
 *
 * @author retrooper
 * @since 1.6.8.2
 */
public class GameProfileUtil {

    public static Object getGameProfile(UUID uuid, String username) {
        Player player = Bukkit.getPlayer(uuid);

        if (player != null) {
            Object entityHuman = NMSUtils.entityHumanClass.cast(NMSUtils.getEntityPlayer(player));
            WrappedPacket wrappedEntityPlayer = new WrappedPacket(new NMSPacket(entityHuman), NMSUtils.entityHumanClass);
            return wrappedEntityPlayer.readObject(0, GameProfile.class);
        } else {
            return new GameProfile(uuid, username);
        }
    }

    @Contract("_ -> new")
    public static @NotNull WrappedGameProfile getWrappedGameProfile(Object gameProfile) {
        GameProfile gp = (GameProfile) gameProfile;
        return new WrappedGameProfile(gp.getId(), gp.getName());
    }

    public static void setGameProfileSkin(Object gameProfile, @NotNull Skin skin) {
        GameProfile gp = (GameProfile) gameProfile;
        gp.getProperties().put("textures", new Property(skin.getValue(), skin.getSignature()));
    }

    public static @NotNull Skin getGameProfileSkin(Object gameProfile) {
        Property property = ((GameProfile) gameProfile).getProperties().get("textures").iterator().next();
        String value = property.getValue();
        String signature = property.getSignature();
        return new Skin(value, signature);
    }
}
