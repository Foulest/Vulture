package io.github.retrooper.packetevents.event;

import io.github.retrooper.packetevents.event.impl.*;
import io.github.retrooper.packetevents.utils.immutableset.ImmutableSetCustom;
import lombok.Getter;

/**
 * Abstract packet listener.
 *
 * @author retrooper
 * @since 1.8
 */
@Getter
public abstract class PacketListenerAbstract {

    private final PacketListenerPriority priority;

    public ImmutableSetCustom<Byte> serverSidedStatusAllowance;
    public ImmutableSetCustom<Byte> serverSidedLoginAllowance;
    public ImmutableSetCustom<Byte> serverSidedPlayAllowance;

    public ImmutableSetCustom<Byte> clientSidedStatusAllowance;
    public ImmutableSetCustom<Byte> clientSidedHandshakeAllowance;
    public ImmutableSetCustom<Byte> clientSidedLoginAllowance;
    public ImmutableSetCustom<Byte> clientSidedPlayAllowance;

    public PacketListenerAbstract(PacketListenerPriority priority) {
        this.priority = priority;

        serverSidedStatusAllowance = null;
        serverSidedLoginAllowance = null;
        serverSidedPlayAllowance = null;

        clientSidedStatusAllowance = null;
        clientSidedHandshakeAllowance = null;
        clientSidedLoginAllowance = null;
        clientSidedPlayAllowance = null;
    }

    public PacketListenerAbstract() {
        this(PacketListenerPriority.NORMAL);
    }

    public void onPacketDecode(PacketDecodeEvent event) {
    }

    public void onPacketStatusReceive(PacketStatusReceiveEvent event) {
    }

    public void onPacketStatusSend(PacketStatusSendEvent event) {
    }

    public void onPacketHandshakeReceive(PacketHandshakeReceiveEvent event) {
    }

    public void onPacketLoginReceive(PacketLoginReceiveEvent event) {
    }

    public void onPacketLoginSend(PacketLoginSendEvent event) {
    }

    public void onPacketPlayReceive(PacketPlayReceiveEvent event) {
    }

    public void onPacketPlaySend(PacketPlaySendEvent event) {
    }

    public void onPostPacketPlayReceive(PostPacketPlayReceiveEvent event) {
    }

    public void onPostPacketPlaySend(PostPacketPlaySendEvent event) {
    }

    public void onPostPlayerInject(PostPlayerInjectEvent event) {
    }

    public void onPlayerInject(PlayerInjectEvent event) {
    }

    public void onPlayerEject(PlayerEjectEvent event) {
    }

    public void onPacketEventExternal(PacketEvent event) {
    }

    public final void addServerSidedStatusFilter(Byte... statusPacketIDs) {
        if (serverSidedStatusAllowance == null) {
            serverSidedStatusAllowance = new ImmutableSetCustom<>(statusPacketIDs);
        } else {
            serverSidedStatusAllowance.addAll(statusPacketIDs);
        }
    }

    public final void addServerSidedLoginFilter(Byte... loginPacketIDs) {
        if (serverSidedLoginAllowance == null) {
            serverSidedLoginAllowance = new ImmutableSetCustom<>(loginPacketIDs);
        } else {
            serverSidedLoginAllowance.addAll(loginPacketIDs);
        }
    }

    public final void addServerSidedPlayFilter(Byte... playPacketIDs) {
        if (serverSidedPlayAllowance == null) {
            serverSidedPlayAllowance = new ImmutableSetCustom<>(playPacketIDs);
        } else {
            serverSidedPlayAllowance.addAll(playPacketIDs);
        }
    }

    public final void addClientSidedStatusFilter(Byte... statusPacketIDs) {
        if (clientSidedStatusAllowance == null) {
            clientSidedStatusAllowance = new ImmutableSetCustom<>(statusPacketIDs);
        } else {
            clientSidedStatusAllowance.addAll(statusPacketIDs);
        }
    }

    public final void addClientSidedHandshakeFilter(Byte... handshakePacketIDs) {
        if (clientSidedHandshakeAllowance == null) {
            clientSidedHandshakeAllowance = new ImmutableSetCustom<>(handshakePacketIDs);
        } else {
            clientSidedHandshakeAllowance.addAll(handshakePacketIDs);
        }
    }

    public final void addClientSidedLoginFilter(Byte... loginPacketIDs) {
        if (clientSidedLoginAllowance == null) {
            clientSidedLoginAllowance = new ImmutableSetCustom<>(loginPacketIDs);
        } else {
            clientSidedLoginAllowance.addAll(loginPacketIDs);
        }
    }

    public final void addClientSidedPlayFilter(Byte... playPacketIDs) {
        if (clientSidedPlayAllowance == null) {
            clientSidedPlayAllowance = new ImmutableSetCustom<>(playPacketIDs);
        } else {
            clientSidedPlayAllowance.addAll(playPacketIDs);
        }
    }

    public final void filterAll() {
        filterServerSidedStatus();
        filterServerSidedLogin();
        filterServerSidedPlay();

        filterClientSidedStatus();
        filterClientSidedHandshake();
        filterClientSidedLogin();
        filterClientSidedPlay();
    }

    public final void filterServerSidedStatus() {
        serverSidedStatusAllowance = new ImmutableSetCustom<>();
    }

    public final void filterServerSidedLogin() {
        serverSidedLoginAllowance = new ImmutableSetCustom<>();
    }

    public final void filterServerSidedPlay() {
        serverSidedPlayAllowance = new ImmutableSetCustom<>();
    }

    public final void filterClientSidedStatus() {
        clientSidedStatusAllowance = new ImmutableSetCustom<>();
    }

    public final void filterClientSidedHandshake() {
        clientSidedHandshakeAllowance = new ImmutableSetCustom<>();
    }

    public final void filterClientSidedLogin() {
        clientSidedLoginAllowance = new ImmutableSetCustom<>();
    }

    public final void filterClientSidedPlay() {
        clientSidedPlayAllowance = new ImmutableSetCustom<>();
    }
}
