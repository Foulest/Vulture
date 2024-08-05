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
import io.github.retrooper.packetevents.event.PacketListenerPriority;
import io.github.retrooper.packetevents.event.eventtypes.CallableEvent;
import lombok.NoArgsConstructor;
import lombok.Synchronized;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

@ToString
@NoArgsConstructor
class EventManagerModern {

    private final Map<Byte, HashSet<PacketListenerAbstract>> listenersMap = new ConcurrentHashMap<>();

    /**
     * Call the PacketEvent.
     * This method processes the event on all the registered dynamic packet event listeners.
     * be the final decider whether the event has been cancelled or not.
     * This call event also calls the legacy event manager call event.
     *
     * @param event {@link PacketEvent}
     */
    void callEvent(CallableEvent event) {
        for (byte priority = PacketListenerPriority.LOWEST.getId();
             priority <= PacketListenerPriority.MONITOR.getId(); priority++) {
            Iterable<PacketListenerAbstract> listeners = listenersMap.get(priority);

            if (listeners != null) {
                for (PacketListenerAbstract listener : listeners) {
                    try {
                        event.call(listener);
                    } catch (RuntimeException ex) {
                        PacketEvents.getPlugin().getLogger()
                                .log(Level.SEVERE, "PacketEvents found an exception while calling a packet listener.", ex);
                    }
                }
            }
        }
    }

    /**
     * Register the dynamic packet event listener.
     *
     * @param listener {@link PacketListenerAbstract}
     */
    @Synchronized
    void registerListener(@NotNull PacketListenerAbstract listener) {
        byte priority = listener.getPriority().getId();
        HashSet<PacketListenerAbstract> listenerSet = listenersMap.get(priority);

        if (listenerSet == null) {
            listenerSet = new HashSet<>();
        }

        listenerSet.add(listener);
        listenersMap.put(priority, listenerSet);
    }

    /**
     * Register multiple dynamic packet event listeners with one method.
     *
     * @param listeners {@link PacketListenerAbstract}
     */
    @Synchronized
    void registerListeners(PacketListenerAbstract @NotNull ... listeners) {
        for (PacketListenerAbstract listener : listeners) {
            registerListener(listener);
        }
    }

    /**
     * Unregister the dynamic packet event listener.
     *
     * @param listener {@link PacketListenerAbstract}
     */
    @Synchronized
    void unregisterListener(@NotNull PacketListenerAbstract listener) {
        byte priority = listener.getPriority().getId();
        Set<PacketListenerAbstract> listenerSet = listenersMap.get(priority);

        if (listenerSet != null) {
            listenerSet.remove(listener);
        }
    }

    /**
     * Unregister multiple dynamic packet event listeners with one method.
     *
     * @param listeners {@link PacketListenerAbstract}
     */
    @Synchronized
    void unregisterListeners(PacketListenerAbstract @NotNull ... listeners) {
        for (PacketListenerAbstract listener : listeners) {
            unregisterListener(listener);
        }
    }

    /**
     * Unregister all dynamic packet event listeners.
     */
    @Synchronized
    void unregisterAllListeners() {
        listenersMap.clear();
    }
}
