package net.foulest.vulture.check.type.clientbrand.type;

public enum DataType {
    BRAND,
    REGISTER_DATA_MOD,
    REGISTER_DATA_OTHER,
    CHANNEL;

    public String getName() {
        switch (this) {
            case BRAND:
                return "Brand";
            case REGISTER_DATA_MOD:
                return "Mod";
            case REGISTER_DATA_OTHER:
                return "Other";
            case CHANNEL:
                return "Channel";
            default:
                return "Unknown";
        }
    }
}
