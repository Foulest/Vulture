package io.github.retrooper.packetevents.utils.server;

/**
 * System Operating system.
 *
 * @author retrooper
 * @since 1.7
 */
public enum SystemOS {
    WINDOWS,
    MACOS,
    LINUX,
    OTHER;

    private static final SystemOS[] VALUES = values();
    private static SystemOS value;

    /**
     * Get the server's operating system.
     * This method will NOT cache.
     *
     * @return Operating System.
     */
    public static SystemOS getOSNoCache() {
        String os = System.getProperty("os.name").toLowerCase();

        for (SystemOS sysos : VALUES) {
            if (os.contains(sysos.name().toLowerCase())) {
                return sysos;
            }
        }
        return OTHER;
    }

    /**
     * Get the server's operating system.
     * This method will CACHE for you.
     *
     * @return Operating System.
     */
    public static SystemOS getOS() {
        if (value == null) {
            value = getOSNoCache();
        }
        return value;
    }
}
