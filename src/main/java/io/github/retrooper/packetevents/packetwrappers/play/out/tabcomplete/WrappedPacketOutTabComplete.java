package io.github.retrooper.packetevents.packetwrappers.play.out.tabcomplete;

import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;
import io.github.retrooper.packetevents.utils.server.ServerVersion;

import java.util.List;
import java.util.Optional;

// TODO: Make sendable
public class WrappedPacketOutTabComplete extends WrappedPacket {

    private static boolean v_1_13;
    private static Class<?> suggestionsClass;
    private int transactionID;
    private String[] matches;

    public WrappedPacketOutTabComplete(NMSPacket packet) {
        super(packet);
    }

    public WrappedPacketOutTabComplete(String[] matches) {
        this.transactionID = -1;
        this.matches = matches;
    }

    public WrappedPacketOutTabComplete(int transactionID, String[] matches) {
        this.transactionID = transactionID;
        this.matches = matches;
    }

    @Override
    protected void load() {
        v_1_13 = version.isNewerThanOrEquals(ServerVersion.v_1_13);

        try {
            suggestionsClass = Class.forName("com.mojang.brigadier.suggestion.Suggestions");
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    public Optional<Integer> getTransactionId() {
        if (v_1_13) {
            if (packet != null) {
                return Optional.of(readInt(0));
            } else {
                return Optional.of(transactionID);
            }
        } else {
            return Optional.empty();
        }
    }

    public void setTransactionId(int transactionID) {
        if (v_1_13) {
            if (packet != null) {
                writeInt(0, transactionID);
            } else {
                this.transactionID = transactionID;
            }
        }
    }

    public String[] getMatches() {
        if (packet != null) {
            if (v_1_13) {
                Object suggestions = readObject(0, suggestionsClass);
                WrappedPacket suggestionsWrapper = new WrappedPacket(new NMSPacket(suggestions));
                List<Object> suggestionList = suggestionsWrapper.readList(0);
                String[] matches = new String[suggestionList.size()];

                for (int i = 0; i < matches.length; i++) {
                    Object suggestion = suggestionList.get(i);
                    WrappedPacket suggestionWrapper = new WrappedPacket(new NMSPacket(suggestion));
                    matches[i] = suggestionWrapper.readString(0);
                }
                return matches;

            } else {
                return readStringArray(0);
            }
        } else {
            return matches;
        }
    }

    public void setMatches(String[] matches) {
        if (packet != null) {
            if (v_1_13) {
                Object suggestions = readObject(0, suggestionsClass);
                WrappedPacket suggestionsWrapper = new WrappedPacket(new NMSPacket(suggestions));
                List<Object> suggestionList = suggestionsWrapper.readList(0);

                for (int i = 0; i < matches.length; i++) {
                    Object suggestion = suggestionList.get(i);
                    WrappedPacket suggestionWrapper = new WrappedPacket(new NMSPacket(suggestion));
                    suggestionWrapper.writeString(0, matches[i]);
                }
            } else {
                writeStringArray(0, matches);
            }
        } else {
            this.matches = matches;
        }
    }
}
