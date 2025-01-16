/*
 * This file is part of Pledge - https://github.com/ThomasOM/Pledge
 * Copyright (C) 2021 Thomazz
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package net.foulest.pledge;

import io.netty.channel.Channel;
import net.foulest.pledge.pinger.ClientPinger;
import net.foulest.pledge.pinger.frame.FrameClientPinger;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Main API object
 */
public interface Pledge {

    /**
     * Sends a player a ping packet with a certain ID.
     * Can listen to events after sending the ping.
     * <p>
     *
     * @param player - Player to send ping
     * @param id     - ID of the ping
     */
    void sendPing(@NotNull Player player, int id);

    /**
     * Gets the networking channel for a {@link Player} if available.
     * <p>
     *
     * @param player - Player to get channel for
     * @return - Networking channel
     */
    Optional<Channel> getChannel(@NotNull Player player);

    /**
     * Creates a client pinger.
     * See documentation in {@link ClientPinger} for more info.
     * <p>
     *
     * @param startId - Start ID for ping range
     * @param endId   - End ID for ping range
     * @return - Client pinger instance
     */
    ClientPinger createPinger(int startId, int endId);

    /**
     * Creates a frame client pinger.
     * See documentation in {@link FrameClientPinger} for more info.
     * <p>
     *
     * @param startId - Start ID for ping range
     * @param endId   - End ID for ping range
     * @return - Frame client pinger instance
     */
    FrameClientPinger createFramePinger(int startId, int endId);

    /**
     * Destroys the API instance.
     * A new API instance can be retrieved and created using {@link PledgeImpl#getOrCreate(Plugin)}
     */
    void destroy();

    /**
     * Creates a new API instance using the provided plugin to register listeners.
     * If an API instance already exists, it returns the existing one instead.
     * The API instance can be destroyed using {@link PledgeImpl#destroy()}
     * <p>
     *
     * @param plugin - Plugin to register listeners under
     * @return - API instance
     */
    static @NotNull Pledge getOrCreate(@NotNull Plugin plugin) {
        if (PledgeImpl.instance == null) {
            PledgeImpl.instance = new PledgeImpl(plugin);
        }
        return PledgeImpl.instance;
    }
}
