package io.github.retrooper.packetevents.event.manager;

/**
 * Class storing a static instance of the dynamic and the legacy event manager.
 *
 * @author retrooper
 * @since 1.8
 */
public class PEEventManager implements EventManager {

    public static final EventManagerModern EVENT_MANAGER_MODERN = new EventManagerModern();
}
