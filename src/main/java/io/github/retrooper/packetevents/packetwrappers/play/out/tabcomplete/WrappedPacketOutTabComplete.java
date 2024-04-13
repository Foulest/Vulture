package io.github.retrooper.packetevents.packetwrappers.play.out.tabcomplete;

import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;
import lombok.AllArgsConstructor;

// TODO: Make sendable
@AllArgsConstructor
public class WrappedPacketOutTabComplete extends WrappedPacket {

    private String[] matches;

    public WrappedPacketOutTabComplete(NMSPacket packet) {
        super(packet);
    }

    public String[] getMatches() {
        if (packet != null) {
            return readStringArray(0);
        } else {
            return matches;
        }
    }

    public void setMatches(String[] matches) {
        if (packet != null) {
            writeStringArray(0, matches);
        } else {
            this.matches = matches;
        }
    }
}
