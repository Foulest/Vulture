package io.github.retrooper.packetevents.utils.version;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * PacketEvents version.
 * This class represents a PacketEvents version.
 *
 * @author retrooper
 * @since 1.8
 */
public class PEVersion {

    /**
     * Array containing the digits in the version.
     * For example, "1.8.9" will be stored as {1, 8, 9} in an array.
     */
    private final int[] versionIntArray;

    /**
     * Specify your version using an array.
     *
     * @param version Array version.
     */
    public PEVersion(int... version) {
        this.versionIntArray = version;
    }

    /**
     * Specify your version using a string, for example: "1.8.9".
     *
     * @param version String version.
     */
    public PEVersion(@NotNull String version) {
        String[] versionIntegers = version.split("\\.");
        int length = versionIntegers.length;
        this.versionIntArray = new int[length];

        for (int i = 0; i < length; i++) {
            versionIntArray[i] = Integer.parseInt(versionIntegers[i]);
        }
    }

    /**
     * Compare to another PEVersion.
     * If we are newer than the compared version,
     * this method will return 1.
     * If we are older than the compared version,
     * this method will return -1.
     * If we are equal to the compared version,
     * this method will return 0.
     * Similar to {@link Integer#compareTo(Integer)}.
     *
     * @param version Compared version
     * @return Comparing to another Version.
     */
    public int compareTo(@NotNull PEVersion version) {
        int localLength = versionIntArray.length;
        int oppositeLength = version.versionIntArray.length;
        int length = Math.max(localLength, oppositeLength);

        for (int i = 0; i < length; i++) {
            int localInteger = i < localLength ? versionIntArray[i] : 0;
            int oppositeInteger = i < oppositeLength ? version.versionIntArray[i] : 0;

            if (localInteger > oppositeInteger) {
                return 1;
            } else if (localInteger < oppositeInteger) {
                return -1;
            }
        }
        return 0;
    }

    /**
     * Does the {@link #compareTo(PEVersion)} return 1?
     *
     * @param version Compared version.
     * @return Is this newer than the compared version.
     */
    public boolean isNewerThan(PEVersion version) {
        return compareTo(version) == 1;
    }

    /**
     * Does the {@link #compareTo(PEVersion)} return -1?
     *
     * @param version Compared version.
     * @return Is this older than the compared version.
     */
    public boolean isOlderThan(PEVersion version) {
        return compareTo(version) == -1;
    }

    /**
     * Represented as an array.
     *
     * @return Array version.
     */
    public int[] asArray() {
        return versionIntArray;
    }

    /**
     * Is this version equal to the compared object.
     * The object must be a PEVersion and the array values must be equal.
     *
     * @param obj Compared object.
     * @return Are they equal?
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj instanceof PEVersion) {
            return Arrays.equals(versionIntArray, ((PEVersion) obj).versionIntArray);
        }
        return false;
    }

    /**
     * Clone the PEVersion.
     *
     * @return A clone.
     */
    @Override
    public PEVersion clone() {
        try {
            return (PEVersion) super.clone();
        } catch (CloneNotSupportedException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * Represent the version as a string.
     *
     * @return String representation.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(versionIntArray.length * 2 - 1).append(versionIntArray[0]);

        for (int i = 1; i < versionIntArray.length; i++) {
            sb.append(".").append(versionIntArray[i]);
        }
        return sb.toString();
    }
}
