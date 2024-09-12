/*
 * Vulture - an advanced anti-cheat plugin designed for Minecraft 1.8.9 servers.
 * Copyright (C) 2024 Foulest (https://github.com/Foulest)
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
 */
@Getter
@Setter
public class Processor extends PacketListenerAbstract implements Listener {

    protected Processor() {
        // Register the processor as a packet listener and bukkit event listener.
        Vulture.instance.getPacketEvents().getEventManager().registerListener(this);
        Bukkit.getPluginManager().registerEvents(this, Vulture.instance);
    }
}
