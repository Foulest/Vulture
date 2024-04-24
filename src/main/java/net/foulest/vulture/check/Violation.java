package net.foulest.vulture.check;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;

/**
 * Data class for violations.
 *
 * @author Foulest
 * @project Vulture
 */
@Getter
@Setter
@AllArgsConstructor
public class Violation {

    // Check Data
    private final CheckInfoData checkInfo;
    private final String[] data;
    private final int violations;

    // Player Data
    private final Location location;
    private final int ping;

    // Server Data
    private final double tps;

    // Timestamp
    private final long timestamp;
}
