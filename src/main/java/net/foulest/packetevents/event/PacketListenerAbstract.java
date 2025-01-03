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
package net.foulest.packetevents.event;

import lombok.Getter;
import net.foulest.packetevents.event.impl.*;
import net.foulest.packetevents.utils.immutableset.ImmutableSetCustom;
import org.jetbrains.annotations.Nullable;

/**
 * Abstract packet listener.
 *
 * @author retrooper
 * @since 1.8
 */
@Getter
public abstract class PacketListenerAbstract {

    private final PacketListenerPriority priority;
    public @Nullable ImmutableSetCustom<Byte> serverSidedStatusAllowance;
    public @Nullable ImmutableSetCustom<Byte> serverSidedLoginAllowance;
    public @Nullable ImmutableSetCustom<Byte> serverSidedPlayAllowance;
    public @Nullable ImmutableSetCustom<Byte> clientSidedStatusAllowance;
    public @Nullable ImmutableSetCustom<Byte> clientSidedHandshakeAllowance;
    public @Nullable ImmutableSetCustom<Byte> clientSidedLoginAllowance;
    public @Nullable ImmutableSetCustom<Byte> clientSidedPlayAllowance;

    private PacketListenerAbstract(PacketListenerPriority priority) {
        this.priority = priority;
        serverSidedStatusAllowance = null;
        serverSidedLoginAllowance = null;
        serverSidedPlayAllowance = null;
        clientSidedStatusAllowance = null;
        clientSidedHandshakeAllowance = null;
        clientSidedLoginAllowance = null;
        clientSidedPlayAllowance = null;
    }

    protected PacketListenerAbstract() {
        this(PacketListenerPriority.NORMAL);
    }

    public void onPacketDecode(PacketDecodeEvent event) {
        // This method is intentionally left blank.
    }

    public void onPacketStatusReceive(PacketStatusReceiveEvent event) {
        // This method is intentionally left blank.
    }

    public void onPacketStatusSend(PacketStatusSendEvent event) {
        // This method is intentionally left blank.
    }

    public void onPacketHandshakeReceive(PacketHandshakeReceiveEvent event) {
        // This method is intentionally left blank.
    }

    public void onPacketLoginReceive(PacketLoginReceiveEvent event) {
        // This method is intentionally left blank.
    }

    public void onPacketLoginSend(PacketLoginSendEvent event) {
        // This method is intentionally left blank.
    }

    public void onPacketPlayReceive(PacketPlayReceiveEvent event) {
        // This method is intentionally left blank.
    }

    public void onPacketPlaySend(PacketPlaySendEvent event) {
        // This method is intentionally left blank.
    }

    public void onPostPacketPlayReceive(PostPacketPlayReceiveEvent event) {
        // This method is intentionally left blank.
    }

    public void onPostPacketPlaySend(PostPacketPlaySendEvent event) {
        // This method is intentionally left blank.
    }

    public void onPostPlayerInject(PostPlayerInjectEvent event) {
        // This method is intentionally left blank.
    }

    public void onPlayerInject(PlayerInjectEvent event) {
        // This method is intentionally left blank.
    }

    public void onPlayerEject(PlayerEjectEvent event) {
        // This method is intentionally left blank.
    }

    public void onPacketEventExternal(PacketEvent event) {
        // This method is intentionally left blank.
    }

    public void addServerSidedStatusFilter(Byte... statusPacketIDs) {
        if (serverSidedStatusAllowance == null) {
            serverSidedStatusAllowance = new ImmutableSetCustom<>(statusPacketIDs);
        } else {
            serverSidedStatusAllowance.addAll(statusPacketIDs);
        }
    }

    public void addServerSidedLoginFilter(Byte... loginPacketIDs) {
        if (serverSidedLoginAllowance == null) {
            serverSidedLoginAllowance = new ImmutableSetCustom<>(loginPacketIDs);
        } else {
            serverSidedLoginAllowance.addAll(loginPacketIDs);
        }
    }

    public void addServerSidedPlayFilter(Byte... playPacketIDs) {
        if (serverSidedPlayAllowance == null) {
            serverSidedPlayAllowance = new ImmutableSetCustom<>(playPacketIDs);
        } else {
            serverSidedPlayAllowance.addAll(playPacketIDs);
        }
    }

    public void addClientSidedStatusFilter(Byte... statusPacketIDs) {
        if (clientSidedStatusAllowance == null) {
            clientSidedStatusAllowance = new ImmutableSetCustom<>(statusPacketIDs);
        } else {
            clientSidedStatusAllowance.addAll(statusPacketIDs);
        }
    }

    public void addClientSidedHandshakeFilter(Byte... handshakePacketIDs) {
        if (clientSidedHandshakeAllowance == null) {
            clientSidedHandshakeAllowance = new ImmutableSetCustom<>(handshakePacketIDs);
        } else {
            clientSidedHandshakeAllowance.addAll(handshakePacketIDs);
        }
    }

    public void addClientSidedLoginFilter(Byte... loginPacketIDs) {
        if (clientSidedLoginAllowance == null) {
            clientSidedLoginAllowance = new ImmutableSetCustom<>(loginPacketIDs);
        } else {
            clientSidedLoginAllowance.addAll(loginPacketIDs);
        }
    }

    public void addClientSidedPlayFilter(Byte... playPacketIDs) {
        if (clientSidedPlayAllowance == null) {
            clientSidedPlayAllowance = new ImmutableSetCustom<>(playPacketIDs);
        } else {
            clientSidedPlayAllowance.addAll(playPacketIDs);
        }
    }

    public void filterAll() {
        filterServerSidedStatus();
        filterServerSidedLogin();
        filterServerSidedPlay();

        filterClientSidedStatus();
        filterClientSidedHandshake();
        filterClientSidedLogin();
        filterClientSidedPlay();
    }

    private void filterServerSidedStatus() {
        serverSidedStatusAllowance = new ImmutableSetCustom<>();
    }

    private void filterServerSidedLogin() {
        serverSidedLoginAllowance = new ImmutableSetCustom<>();
    }

    private void filterServerSidedPlay() {
        serverSidedPlayAllowance = new ImmutableSetCustom<>();
    }

    private void filterClientSidedStatus() {
        clientSidedStatusAllowance = new ImmutableSetCustom<>();
    }

    private void filterClientSidedHandshake() {
        clientSidedHandshakeAllowance = new ImmutableSetCustom<>();
    }

    private void filterClientSidedLogin() {
        clientSidedLoginAllowance = new ImmutableSetCustom<>();
    }

    private void filterClientSidedPlay() {
        clientSidedPlayAllowance = new ImmutableSetCustom<>();
    }
}
