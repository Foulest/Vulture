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
package io.github.retrooper.packetevents.event.eventtypes;

import io.github.retrooper.packetevents.event.PacketEvent;
import io.github.retrooper.packetevents.event.PacketListenerAbstract;
import io.github.retrooper.packetevents.event.impl.PacketPlayReceiveEvent;

/**
 * The {@link PacketEvent} implements this interface.
 * Every inbuilt event should implement the {@link #call(PacketListenerAbstract)} method.
 * If you are making a custom event, don't implement this.
 * The {@link PacketListenerAbstract#onPacketEventExternal(PacketEvent)} method is called for every event that is not in-built.
 * including custom events.
 *
 * @author retrooper
 * @see PacketPlayReceiveEvent
 * @since 1.8
 */
public interface CallableEvent {

    void call(PacketListenerAbstract listener);
}
