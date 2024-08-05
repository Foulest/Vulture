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
package dev.thomazz.pledge.pinger.data;

import dev.thomazz.pledge.pinger.ClientPingerImpl;
import lombok.Getter;
import lombok.ToString;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Getter
@ToString
public class PingData {

    private final Queue<Ping> expectingIds = new ConcurrentLinkedQueue<>();
    private final Player player;
    private final ClientPingerImpl pinger;

    private boolean validated;
    private int id;

    public PingData(Player player, @NotNull ClientPingerImpl pinger) {
        this.player = player;
        this.pinger = pinger;
        id = pinger.startId();
    }

    public int pullId() {
        int startId = pinger.startId();
        int endId = pinger.endId();

        boolean direction = endId - startId > 0;
        int oldId = id;
        int newId = oldId + (direction ? 1 : -1);

        if (direction ? newId > endId : newId < endId) {
            newId = startId;
        }

        id = newId;
        return oldId;
    }

    public void offer(@NotNull Ping ping) {
        expectingIds.add(ping);
    }

    public Optional<Ping> confirm(int id) {
        Ping ping = expectingIds.peek();

        if (ping != null && ping.getId() == id) {
            // Make sure to notify validation with the first correct ping received
            if (!validated) {
                pinger.getPingListeners().forEach(listener -> listener.onValidation(player, id));
                validated = true;
            }
            return Optional.ofNullable(expectingIds.poll());
        }
        return Optional.empty();
    }
}
