package dev.thomazz.pledge.pinger.frame.data;

import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;

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
