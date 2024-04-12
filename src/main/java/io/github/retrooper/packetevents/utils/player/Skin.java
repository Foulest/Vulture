package io.github.retrooper.packetevents.utils.player;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Skin {

    private String value;
    private String signature;

    public Skin(String value, String signature) {
        this.value = value;
        this.signature = signature;
    }
}
