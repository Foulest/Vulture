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
package dev.thomazz.pledge.pinger;

import org.bukkit.entity.Player;

import java.util.function.Predicate;

/**
 * Utility to send pings automatically and the start and end of a tick.
 * Several events can be listened to through a {@link ClientPingerListener}, such as when pings are sent and received.
 */
public interface ClientPinger {

    /**
     * Start of the ID range for pings used by this instance.
     * <p>
     *
     * @return - Start ID
     */
    int startId();

    /**
     * End of the ID range for pings used by this instance.
     * <p>
     *
     * @return - End ID
     */
    int endId();

    /**
     * Determines if a player should be registered to this
     * Always registers players by default unless a different predicate is provided.
     * <p>
     *
     * @param condition - If player should be registered or not
     */
    void filter(Predicate<Player> condition);

    /**
     * Attaches a client ping listener to this
     * <p>
     *
     * @param listener - Listener to attach
     */
    void attach(ClientPingerListener listener);
}
