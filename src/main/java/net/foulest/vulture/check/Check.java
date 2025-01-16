/*
 * Vulture - a server protection plugin designed for Minecraft 1.8.9 servers.
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

import com.github.retrooper.packetevents.event.ProtocolPacketEvent;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent;
import lombok.Data;
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
 */
@Data
@SuppressWarnings("unused")
public class Check implements Listener {

    protected final @NotNull PlayerData playerData;
    protected final Player player;
    private final CheckInfo checkInfo;
    private final @NotNull CheckInfoData checkInfoData;

    public Check(@NotNull PlayerData playerData) throws ClassNotFoundException {
        if (!getClass().isAnnotationPresent(CheckInfo.class)) {
            throw new ClassNotFoundException("Check is missing @CheckInfo annotation.");
        }

        this.playerData = playerData;
        player = playerData.getPlayer();
        checkInfo = getClass().getAnnotation(CheckInfo.class);
        checkInfoData = new CheckInfoData(checkInfo);
    }

    /**
     * This method is fired when the player sends a packet.
     *
     * @param event The packet event.
     */
    public void handle(PacketPlaySendEvent event) {
        // This method is intentionally left blank.
    }

    /**
     * This method is fired when the player receives a packet.
     *
     * @param event The packet event.
     */
    public void handle(PacketPlayReceiveEvent event) {
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
    @SuppressWarnings("EmptyMethod")
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
    @SuppressWarnings("EmptyMethod")
    public void handle(MovementEvent event, long timestamp) {
        // This method is intentionally left blank.
    }

    /**
     * This method is used to flag the player and cancel an event.
     *
     * @param event   The event to cancel.
     * @param verbose The optional data to include in the flag.
     */
    protected void flag(@NotNull ProtocolPacketEvent event, String... verbose) {
        PunishUtil.flag(playerData, checkInfoData, event, verbose);
    }

    /**
     * This method is used to flag the player with the given data.
     * <p>
     * When a player is flagged, all online staff members are alerted with the check they flagged and the data
     *
     * @param verbose The optional data to include in the flag.
     */
    protected void flag(String... verbose) {
        PunishUtil.flag(playerData, checkInfoData, verbose);
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
    private void handleAlert(@NotNull String verbose) {
        PunishUtil.handleAlert(playerData, checkInfoData, verbose);
    }

    /**
     * Handles the punishment to execute.
     *
     * @param verbose The verbose to add to the punishment.
     */
    private void handlePunishment(@NotNull String verbose) {
        PunishUtil.handlePunishment(playerData, checkInfoData, verbose);
    }
}
