/*
 * This file is part of packetevents - https://github.com/retrooper/packetevents
 * Copyright (C) 2022 retrooper and contributors
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
package net.foulest.packetevents.utils.gameprofile;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.foulest.packetevents.packetwrappers.NMSPacket;
import net.foulest.packetevents.packetwrappers.WrappedPacket;
import net.foulest.packetevents.packetwrappers.api.WrapperPacketReader;
import net.foulest.packetevents.utils.nms.NMSUtils;
import net.foulest.packetevents.utils.player.Skin;
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
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GameProfileUtil {

    public static Object getGameProfile(UUID uuid, String username) {
        Player player = Bukkit.getPlayer(uuid);

        if (player != null) {
            Object entityHuman = NMSUtils.entityHumanClass.cast(NMSUtils.getEntityPlayer(player));
            WrapperPacketReader packet = new WrappedPacket(new NMSPacket(entityHuman), NMSUtils.entityHumanClass);
            return packet.readObject(0, GameProfile.class);
        } else {
            return new GameProfile(uuid, username);
        }
    }

    @Contract("_ -> new")
    public static @NotNull WrappedGameProfile getWrappedGameProfile(Object gameProfile) {
        GameProfile profile = (GameProfile) gameProfile;
        return new WrappedGameProfile(profile.getId(), profile.getName());
    }

    public static void setGameProfileSkin(Object gameProfile, @NotNull Skin skin) {
        GameProfile profile = (GameProfile) gameProfile;
        profile.getProperties().put("textures", new Property(skin.getValue(), skin.getSignature()));
    }

    public static @NotNull Skin getGameProfileSkin(Object gameProfile) {
        Property property = ((GameProfile) gameProfile).getProperties().get("textures").iterator().next();
        String value = property.getValue();
        String signature = property.getSignature();
        return new Skin(value, signature);
    }
}
