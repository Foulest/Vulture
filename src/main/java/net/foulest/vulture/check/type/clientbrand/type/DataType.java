package net.foulest.vulture.check.type.clientbrand.type;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public enum DataType {
    BRAND,
    REGISTER_DATA_MOD,
    REGISTER_DATA_OTHER,
    CHANNEL;

    @Contract(pure = true)
    public @NotNull String getName() {
        switch (this) {
            case BRAND:
                return "Brand";
            case REGISTER_DATA_MOD:
                return "Mod";
            case REGISTER_DATA_OTHER:
                return "Register Data";
            case CHANNEL:
                return "Channel";
            default:
                return "Unknown";
        }
    }
}
