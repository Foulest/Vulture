/*
 * This file is part of packetevents - https://github.com/retrooper/packetevents
 * Copyright (C) 2022 retrooper and contributors
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
package io.github.retrooper.packetevents.utils.player;

/**
 * The {@code Hand} enum represents what hand was used in an interaction.
 *
 * @author retrooper
 * @see <a href="https://wiki.vg/Protocol#Open_Book">https://wiki.vg/Protocol#Open_Book</a>
 * @since 1.8
 */
public enum Hand {
    /**
     * The right hand in vanilla minecraft.
     * Some clients allow you to render the main hand as the left hand.
     */
    MAIN_HAND,

    /**
     * The left hand in vanilla minecraft.
     * This hand does not exist on 1.7.10 and 1.8.x's protocol.
     * It will always be the {@link Hand#MAIN_HAND} on those protocols.
     */
    OFF_HAND
}
