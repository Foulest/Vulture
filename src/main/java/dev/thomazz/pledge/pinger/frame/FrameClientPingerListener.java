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

import dev.thomazz.pledge.pinger.ClientPingerListener;
import dev.thomazz.pledge.pinger.frame.data.Frame;
import org.bukkit.entity.Player;

/**
 * Listener that can be attached to a {@link FrameClientPinger} for events regarding {@link Frame} objects.
 */
public interface FrameClientPingerListener extends ClientPingerListener {

    /**
     * Called when a {@link Frame} is created.
     * <p>
     *
     * @param player - Player frame was created for
     * @param frame  - Frame created
     */
    default void onFrameCreate(Player player, Frame frame) {
    }

    /**
     * Called when a {@link Frame} is sent.
     * <p>
     *
     * @param player - Player frame was sent for
     * @param frame  - Frame sent
     */
    default void onFrameSend(Player player, Frame frame) {
    }

    /**
     * Called when a {@link Frame} start ID is received.
     * <p>
     *
     * @param player - Player frame start ID was received for
     * @param frame  - Frame received
     */
    default void onFrameReceiveStart(Player player, Frame frame) {
    }

    /**
     * Called when a {@link Frame} end ID is received.
     * <p>
     *
     * @param player - Player frame end ID was received for
     * @param frame  - Frame received
     */
    default void onFrameReceiveEnd(Player player, Frame frame) {
    }
}
