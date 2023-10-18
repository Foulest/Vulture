package net.foulest.vulture.processor;

import io.github.retrooper.packetevents.event.PacketListenerAbstract;
import lombok.Getter;
import lombok.Setter;
import net.foulest.vulture.Vulture;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

/**
 * Abstract class for processors.
 *
 * @author Foulest
 * @project Vulture
 */
@Getter
@Setter
public class Processor extends PacketListenerAbstract implements Listener {

    public Processor() {
        // Register the processor as a packet listener and bukkit event listener.
        Vulture.instance.getPacketEvents().getEventManager().registerListener(this);
        Bukkit.getPluginManager().registerEvents(this, Vulture.instance);
    }
}
