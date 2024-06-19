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
package dev.thomazz.pledge.pinger.frame;

import dev.thomazz.pledge.pinger.ClientPinger;
import dev.thomazz.pledge.pinger.frame.data.Frame;
import org.bukkit.entity.Player;

/**
 * Implementation of a {@link ClientPinger} with extra functionality to determine for each tick if pings should be sent.
 * <p>
 * If a frame is created using {@link #getOrCreate(Player)},
 * all packets for the current server tick will have a ping sent before and after them.
 */
public interface FrameClientPinger extends ClientPinger {

    /**
     * Creates a frame, scheduling pings to be sent before and after all packets in the current server tick.
     * <p>
     *
     * @param player - Player to create frame for
     * @return - IDs of pings sent before {@link Frame#getStartId()} and after packets {@link Frame#getEndId()}
     */
    Frame getOrCreate(Player player);

    /**
     * Attaches a listener to listen to any events for {@link Frame} objects.
     * <p>
     *
     * @param listener - Listener to attach
     */
    void attach(FrameClientPingerListener listener);
}
