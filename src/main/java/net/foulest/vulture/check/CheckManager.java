package net.foulest.vulture.check;

import lombok.Getter;
import lombok.Setter;
import net.foulest.vulture.check.type.aimassist.AimAssistA;
import net.foulest.vulture.check.type.autoblock.AutoBlockA;
import net.foulest.vulture.check.type.autoblock.AutoBlockB;
import net.foulest.vulture.check.type.autoblock.AutoBlockC;
import net.foulest.vulture.check.type.autoblock.AutoBlockD;
import net.foulest.vulture.check.type.autoclicker.AutoClickerA;
import net.foulest.vulture.check.type.badpackets.*;
import net.foulest.vulture.check.type.clientbrand.ClientBrand;
import net.foulest.vulture.check.type.flight.FlightA;
import net.foulest.vulture.check.type.flight.FlightB;
import net.foulest.vulture.check.type.flight.FlightC;
import net.foulest.vulture.check.type.flight.FlightD;
import net.foulest.vulture.check.type.groundspoof.GroundSpoofA;
import net.foulest.vulture.check.type.groundspoof.GroundSpoofB;
import net.foulest.vulture.check.type.inventory.*;
import net.foulest.vulture.check.type.pingspoof.PingSpoofA;
import net.foulest.vulture.check.type.pingspoof.PingSpoofB;
import net.foulest.vulture.check.type.reach.ReachA;
import net.foulest.vulture.check.type.speed.*;
import net.foulest.vulture.check.type.velocity.VelocityA;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Manager for handling checks.
 *
 * @author Foulest
 * @project Vulture
 */
@Getter
@Setter
public class CheckManager {

    /**
     * A {@link List} of registered check classes
     *
     * @see Check
     */
    public static final List<Class<? extends Check>> CHECK_CLASSES = Collections.unmodifiableList(Arrays.asList(
            // AimAssist
            AimAssistA.class,

            // AutoBlock
            AutoBlockA.class, AutoBlockB.class, AutoBlockC.class, AutoBlockD.class,

            // AutoClicker
            AutoClickerA.class,

            // BadPackets
            BadPacketsA.class, BadPacketsB.class, BadPacketsC.class, BadPacketsD.class, BadPacketsE.class,
            BadPacketsF.class, BadPacketsG.class,

            // ClientBrand
            ClientBrand.class,

            // Flight
            FlightA.class, FlightB.class, FlightC.class, FlightD.class,

            // GroundSpoof
            GroundSpoofA.class, GroundSpoofB.class,

            // Inventory
            InventoryA.class, InventoryB.class, InventoryC.class, InventoryD.class, InventoryE.class,
            InventoryF.class, InventoryG.class, InventoryH.class, InventoryI.class, InventoryJ.class,
            InventoryK.class, InventoryL.class,

            // PingSpoof
            PingSpoofA.class, PingSpoofB.class,

            // Reach
            ReachA.class,

            // Speed
            SpeedA.class, SpeedB.class, SpeedC.class, SpeedD.class, SpeedE.class,

            // Velocity
            VelocityA.class
    ));
}
