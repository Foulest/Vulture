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
package dev.thomazz.pledge.pinger.frame.data;

import lombok.ToString;

import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;

@ToString
public class FrameData {

    private final Queue<Frame> expectingFrames = new ConcurrentLinkedQueue<>();
    private final AtomicReference<Frame> currentFrame = new AtomicReference<>();

    public boolean hasFrame() {
        return currentFrame.get() != null;
    }

    public void setFrame(Frame frame) {
        currentFrame.set(frame);
    }

    public Frame getFrame() {
        return currentFrame.get();
    }

    public Optional<Frame> continueFrame() {
        Frame frame = currentFrame.getAndSet(null);

        if (frame != null) {
            expectingFrames.add(frame);
        }
        return Optional.ofNullable(frame);
    }

    public Optional<Frame> matchStart(int id) {
        return Optional.ofNullable(expectingFrames.peek()).filter(frame -> frame.getStartId() == id);
    }

    public Optional<Frame> matchEnd(int id) {
        return Optional.ofNullable(expectingFrames.peek()).filter(frame -> frame.getEndId() == id);
    }

    public void popFrame() {
        expectingFrames.poll();
    }
}
