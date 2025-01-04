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
package net.foulest.packetevents.packetwrappers.login.in.start;

import net.foulest.packetevents.packetwrappers.NMSPacket;
import net.foulest.packetevents.packetwrappers.WrappedPacket;
import net.foulest.packetevents.utils.gameprofile.GameProfileUtil;
import net.foulest.packetevents.utils.gameprofile.WrappedGameProfile;
import net.foulest.packetevents.utils.nms.NMSUtils;
import org.jetbrains.annotations.NotNull;

public class WrappedPacketLoginInStart extends WrappedPacket {

    public WrappedPacketLoginInStart(NMSPacket packet) {
        super(packet);
    }

    // TODO: Allow accessing game profile property
    public WrappedGameProfile getGameProfile() {
        return GameProfileUtil.getWrappedGameProfile(readObject(0, NMSUtils.gameProfileClass));
    }

    // TODO: Add support for game profile property
    public void setGameProfile(@NotNull WrappedGameProfile wrappedGameProfile) {
        Object gameProfile = GameProfileUtil.getGameProfile(wrappedGameProfile.getId(), wrappedGameProfile.getName());
        write(NMSUtils.gameProfileClass, 0, gameProfile);
    }
}
