package io.github.retrooper.packetevents.event.manager;

import io.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.event.PacketEvent;
import io.github.retrooper.packetevents.event.PacketListenerAbstract;
import io.github.retrooper.packetevents.event.PacketListenerPriority;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class EventManagerModern {

    private final Map<Byte, HashSet<PacketListenerAbstract>> listenersMap = new ConcurrentHashMap<>();

    /**
     * Call the PacketEvent.
     * This method processes the event on all the registered dynamic packet event listeners.
     * be the final decider whether the event has been cancelled or not.
     * This call event also calls the legacy event manager call event.
     *
     * @param event {@link PacketEvent}
     */
    public void callEvent(PacketEvent event) {
        for (byte priority = PacketListenerPriority.LOWEST.getId();
             priority <= PacketListenerPriority.MONITOR.getId(); priority++) {
            HashSet<PacketListenerAbstract> listeners = listenersMap.get(priority);

            if (listeners != null) {
                for (PacketListenerAbstract listener : listeners) {
                    try {
                        event.call(listener);
                    } catch (Exception ex) {
                        PacketEvents.get().getPlugin().getLogger()
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
    public synchronized void registerListener(@NotNull PacketListenerAbstract listener) {
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
    public synchronized void registerListeners(PacketListenerAbstract @NotNull ... listeners) {
        for (PacketListenerAbstract listener : listeners) {
            registerListener(listener);
        }
    }

    /**
     * Unregister the dynamic packet event listener.
     *
     * @param listener {@link PacketListenerAbstract}
     */
    public synchronized void unregisterListener(@NotNull PacketListenerAbstract listener) {
        byte priority = listener.getPriority().getId();
        HashSet<PacketListenerAbstract> listenerSet = listenersMap.get(priority);

        if (listenerSet != null) {
            listenerSet.remove(listener);
        }
    }

    /**
     * Unregister multiple dynamic packet event listeners with one method.
     *
     * @param listeners {@link PacketListenerAbstract}
     */
    public synchronized void unregisterListeners(PacketListenerAbstract @NotNull ... listeners) {
        for (PacketListenerAbstract listener : listeners) {
            unregisterListener(listener);
        }
    }

    /**
     * Unregister all dynamic packet event listeners.
     */
    public synchronized void unregisterAllListeners() {
        listenersMap.clear();
    }
}