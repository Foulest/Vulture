/*
 * Vulture - an advanced anti-cheat plugin designed for Minecraft 1.8.9 servers.
 * Copyright (C) 2024 Foulest (https://github.com/Foulest)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package net.foulest.vulture.check;

import lombok.Data;
import net.foulest.vulture.check.type.badpackets.*;
import net.foulest.vulture.check.type.clientbrand.ClientBrand;
import net.foulest.vulture.check.type.inventory.*;
import net.foulest.vulture.check.type.pingspoof.PingSpoofA;
import net.foulest.vulture.check.type.pingspoof.PingSpoofB;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Manager for handling checks.
 *
 * @author Foulest
 */
@Data
public class CheckManager {

    /**
     * A {@link List} of registered check classes
     *
     * @see Check
     */
    public static final List<Class<? extends Check>> CHECK_CLASSES = Collections.unmodifiableList(Arrays.asList(
            // BadPackets
            BadPacketsA.class, BadPacketsB.class, BadPacketsC.class, BadPacketsD.class,
            BadPacketsE.class, BadPacketsF.class, BadPacketsG.class,

            // ClientBrand
            ClientBrand.class,

            // Inventory
            InventoryA.class, InventoryB.class, InventoryC.class, InventoryD.class, InventoryE.class,
            InventoryF.class, InventoryG.class, InventoryH.class, InventoryI.class, InventoryJ.class,
            InventoryK.class,

            // PingSpoof
            PingSpoofA.class, PingSpoofB.class
    ));
}
