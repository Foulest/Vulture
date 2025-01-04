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
package net.foulest.packetevents.injector;

import org.bukkit.entity.Player;

public interface ChannelInjector {

    default boolean isBound() {
        return true;
    }

    void inject();

    void eject();

    void injectPlayer(Player player);

    void ejectPlayer(Player player);

    boolean hasInjected(Player player);

    void writePacket(Object channel, Object rawNMSPacket);

    void flushPackets(Object channel);

    void sendPacket(Object channel, Object rawNMSPacket);
}
