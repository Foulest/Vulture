package net.foulest.vulture.tracking;

import lombok.Getter;
import lombok.Setter;
import net.foulest.vulture.util.Constants;
import net.foulest.vulture.util.data.Area;

/**
 * Tracking unit to determine the position of an entity on the client.
 * Uses an area for approximation in the case multiple positions are possible.
 */
@Getter
@Setter
public class EntityTrackerEntry {

    private final Area rootBase = new Area(); // First ping interpolation target area when still uncertain
    private final Area base = new Area(); // Area containing all possible interpolation targets from client
    private final Area position = new Area(); // Area containing all possible client positions from client

    private int interpolation; // Interpolation ticks
    private boolean certain; // If certain the client has received the interpolation target

    public EntityTrackerEntry(double x, double y, double z) {
        rootBase.set(x, y, z);
        base.set(x, y, z);
        position.set(x, y, z);
    }

    public void move(double dx, double dy, double dz) {
        rootBase.add(dx, dy, dz);
        base.addCoord(dx, dy, dz);

        interpolation = 3;
        certain = false;
    }

    public void teleport(double x, double y, double z) {
        double errorH = Constants.MIN_TELEPORT_HORIZONTAL;
        double errorV = Constants.MIN_TELEPORT_VERTICAL;

        rootBase.set(x, y, z);
        base.addCoord(x, y, z);

        // If the distance is too close to the client position it is possible for the base to remain unchanged
        if (position.distanceX(x) < errorH && position.distanceY(y) < errorV && position.distanceZ(z) < errorH) {
            rootBase.expand(errorH, errorV, errorH);
            base.expand(errorH, errorV, errorH);
        }

        interpolation = 3;
        certain = false;
    }

    // Marks the interpolation target as certainly received by the client
    public void markCertain() {
        certain = true;
        base.set(rootBase);
    }

    // Client-side interpolation
    public void interpolate() {
        /*
        If uncertain we need to assume all cases for interpolation or no interpolation.
        By including the interpolation target in the position all scenarios from 3 to 0 interpolation ticks are handled.
        We can start shrinking the position area once we are certain the client has received the target.
         */
        if (!certain) {
            position.contain(base);
            return;
        }

        if (interpolation > 0) {
            position.interpolate(base, interpolation--);
        }
    }
}
