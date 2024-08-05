package io.github.retrooper.packetevents.packetwrappers.play.out.tabcomplete;

import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;
import lombok.AllArgsConstructor;
import lombok.ToString;

// TODO: Make sendable
@ToString
@AllArgsConstructor
class WrappedPacketOutTabComplete extends WrappedPacket {

    private String[] matches;

    WrappedPacketOutTabComplete(NMSPacket packet) {
        super(packet);
    }

    public String[] getMatches() {
        if (nmsPacket != null) {
            return readStringArray(0);
        } else {
            return matches;
        }
    }

    public void setMatches(String[] matches) {
        if (nmsPacket != null) {
            writeStringArray(0, matches);
        } else {
            this.matches = matches;
        }
    }
}
