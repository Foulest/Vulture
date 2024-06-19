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
package io.github.retrooper.packetevents.event.manager;

import io.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.event.PacketEvent;
import io.github.retrooper.packetevents.event.PacketListenerAbstract;

/**
 * This is the event manager interface.
 * Every method is already implemented, we just use this
 * to have less code duplications.
 * You can either call event manager methods in the
 * {@link io.github.retrooper.packetevents.PacketEvents} instance or with an instance of the
 * {@link PEEventManager} stored in {@link PacketEvents}.
 *
 * @author retrooper
 * @see PacketEvents#getEventManager()
 * @since 1.0
 */
@SuppressWarnings("UnusedReturnValue")
public interface EventManager {

    /**
     * Call a packet event.
     * This will let all listeners(new and deprecated listeners) process the event.
     * It isn't recommended to use a mix of both listeners(new and deprecated),
     * but it will work and they are compatible.
     *
     * @param event {@link PacketEvent}
     * @return Same event manager instance.
     */
    default EventManager callEvent(PacketEvent event) {
        // The dynamic event manager calls the legacy event manager.
        PEEventManager.EVENT_MANAGER_MODERN.callEvent(event);
        return this;
    }

    /**
     * Register a PacketListenerAbstract listener.
     *
     * @param listener {@link PacketListenerAbstract}
     * @return Same event manager instance.
     */
    default EventManager registerListener(PacketListenerAbstract listener) {
        if (listener != null) {
            PEEventManager.EVENT_MANAGER_MODERN.registerListener(listener);
        }
        return this;
    }

    /**
     * Register multiple PacketListenerAbstract listeners.
     *
     * @param listeners {@link PacketListenerAbstract}
     * @return Same event manager instance.
     */
    default EventManager registerListeners(PacketListenerAbstract... listeners) {
        PEEventManager.EVENT_MANAGER_MODERN.registerListeners(listeners);
        return this;
    }

    /**
     * Unregister a PacketListenerAbstract listener.
     *
     * @param listener {@link PacketListenerAbstract}
     * @return Same event manager instance.
     */
    default EventManager unregisterListener(PacketListenerAbstract listener) {
        if (listener != null) {
            PEEventManager.EVENT_MANAGER_MODERN.unregisterListener(listener);
        }
        return this;
    }

    /**
     * Unregister multiple PacketListenerAbstract listeners.
     *
     * @param listeners {@link PacketListenerAbstract}
     * @return Same event manager instance.
     */
    default EventManager unregisterListeners(PacketListenerAbstract... listeners) {
        PEEventManager.EVENT_MANAGER_MODERN.unregisterListeners(listeners);
        return this;
    }

    /**
     * Unregister all registered event listeners.
     * All the deprecated and the dynamic listeners will be unregistered.
     *
     * @return Same event manager instance.
     */
    default EventManager unregisterAllListeners() {
        PEEventManager.EVENT_MANAGER_MODERN.unregisterAllListeners();
        return this;
    }
}
