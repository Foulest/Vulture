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

import io.github.retrooper.packetevents.event.eventtypes.CancellableEvent;
import io.github.retrooper.packetevents.event.eventtypes.CancellableNMSPacketEvent;
import io.github.retrooper.packetevents.packettype.PacketType;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import lombok.Getter;
import lombok.Setter;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.event.MovementEvent;
import net.foulest.vulture.event.RotationEvent;
import net.foulest.vulture.util.PunishUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

/**
 * Abstract class for checks.
 *
 * @author Foulest
 * @project Vulture
 */
@Getter
@Setter
@SuppressWarnings("unused")
public class Check implements Listener {

    protected final PlayerData playerData;
    protected final Player player;
    private final CheckInfo checkInfo;
    private final CheckInfoData checkInfoData;

    public Check(PlayerData playerData) throws ClassNotFoundException {
        if (!getClass().isAnnotationPresent(CheckInfo.class)) {
            throw new ClassNotFoundException("Check is missing @CheckInfo annotation.");
        }

        this.playerData = playerData;
        player = playerData.getPlayer();
        checkInfo = getClass().getAnnotation(CheckInfo.class);
        checkInfoData = new CheckInfoData(checkInfo);
    }

    /**
     * This method is fired when the player sends or receives a packet.
     *
     * @param packetId  the id of the packet
     * @param nmsPacket the nms packet
     * @param packet    the raw nms packet
     * @param timestamp the timestamp the packet was handled
     * @see PacketType
     * @see NMSPacket
     */
    public void handle(CancellableNMSPacketEvent nmsPacketEvent, byte packetId,
                       NMSPacket nmsPacket, Object packet, long timestamp) {
        // This method is intentionally left blank.
    }

    /**
     * This method is fired when the player rotates.
     *
     * @param event     The rotation event.
     * @param timestamp The timestamp the event was handled.
     * @see Location
     * @see RotationEvent
     */
    public void handle(RotationEvent event, long timestamp) {
        // This method is intentionally left blank.
    }

    /**
     * This method is fired when the player moves.
     *
     * @param event     The movement event.
     * @param timestamp The timestamp the event was handled.
     * @see Location
     * @see MovementEvent
     */
    public void handle(MovementEvent event, long timestamp) {
        // This method is intentionally left blank.
    }

    /**
     * This method is used to flag the player and cancel an event.
     *
     * @param event   the event to cancel
     * @param verbose the optional data to include in the flag
     */
    protected final void flag(boolean setback, @NotNull CancellableEvent event, String... verbose) {
        PunishUtil.flag(playerData, checkInfoData, setback, event, verbose);
    }

    /**
     * This method is used to flag the player with the given data.
     * <p>
     * When a player is flagged, all online staff members are alerted with the check they flagged and the data
     *
     * @param verbose the optional data to include in the flag
     */
    protected final void flag(boolean setback, String... verbose) {
        PunishUtil.flag(playerData, checkInfoData, setback, verbose);
    }

    /**
     * Handles adding a new violation to the player's data.
     *
     * @param verbose The verbose to add to the violation.
     */
    private void handleNewViolation(String... verbose) {
        PunishUtil.handleNewViolation(playerData, checkInfoData, verbose);
    }

    /**
     * Handles sending the alert message.
     *
     * @param verbose The verbose to add to the alert message.
     */
    private void handleAlert(String verbose) {
        PunishUtil.handleAlert(playerData, checkInfoData, verbose);
    }

    /**
     * Handles the punishment to execute.
     *
     * @param verbose The verbose to add to the punishment.
     */
    private void handlePunishment(String verbose) {
        PunishUtil.handlePunishment(playerData, checkInfoData, verbose);
    }
}
