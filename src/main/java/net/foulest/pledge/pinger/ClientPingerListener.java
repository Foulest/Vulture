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
package net.foulest.pledge.pinger;

import org.bukkit.entity.Player;

/**
 * Listener to attach to a {@link ClientPinger}
 */
public interface ClientPingerListener {

    /**
     * Called when a player receives the first transaction ID of the {@link ClientPinger}.
     * After this the player can be considered active on the server.
     * <p>
     *
     * @param player - Player that the ping response is received from
     * @param id     - ID of ping
     */
    default void onValidation(Player player, int id) {
    }

    /**
     * Called when a ping is sent at the start of the tick to a player.
     * <p>
     *
     * @param player - Player that the ping response is sent to
     * @param id     - ID of ping
     */
    default void onPingSendStart(Player player, int id) {
    }

    /**
     * Called when a ping is sent at the end of the tick to a player.
     * <p>
     *
     * @param player - Player that the ping response is sent to
     * @param id     - ID of ping
     */
    default void onPingSendEnd(Player player, int id) {
    }

    /**
     * Called when the response to a ping that was sent at the start of the tick to a player is received.
     * <p>
     *
     * @param player - Player that the ping response is received from
     * @param id     - ID of ping
     */
    default void onPongReceiveStart(Player player, int id) {
    }

    /**
     * Called when the response to a ping that was sent at the end of the tick to a player is received.
     * <p>
     *
     * @param player - Player that the ping response is received from
     * @param id     - ID of ping
     */
    default void onPongReceiveEnd(Player player, int id) {
    }
}
