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
package net.foulest.vulture.util;

import lombok.experimental.UtilityClass;

/**
 * ConstantUtil used in the Minecraft client.
 */
@UtilityClass
public class ConstantUtil {

    // Combat
    public final double RAY_LENGTH = 6.0; // Max server reach cutoff
    private final double MAX_RANGE = 3.0;
    public final float COLLISION_BORDER_SIZE = 0.1F;

    // Velocity
    public final double ON_GROUND_VELOCITY = -0.0784000015258789;

    // Gravity
    public final double GRAVITY_DECAY = 0.08;
    public final double GRAVITY_MULTIPLIER = 0.9800000190735147;

    // Movement
    public final double FAST_MATH_ERROR = MAX_RANGE / 4096.0; // Error for sin table of 4096 and max range
    public final double MIN_MOVE_UPDATE_ROOT = 0.03;

    // Entity move
    public final double MIN_TELEPORT_HORIZONTAL = 0.03125; // See packet listener
    public final double MIN_TELEPORT_VERTICAL = 0.015625;

    // Timing
    public final int MAX_CATCHUP_TICKS = 10;
    public final long TICK_MILLIS = 50L;

    // Messages
    public final String NO_PERMISSION = "&cNo permission.";
    public final String UNABLE_TO_REGISTER_TAB_COMPLETER = "Unable to register tab completer: ";
}
