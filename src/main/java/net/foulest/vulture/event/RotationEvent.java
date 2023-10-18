package net.foulest.vulture.event;

import io.github.retrooper.packetevents.packetwrappers.play.in.flying.WrappedPacketInFlying;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RotationEvent {

    public final WrappedPacketInFlying to;
    public final WrappedPacketInFlying from;
}
