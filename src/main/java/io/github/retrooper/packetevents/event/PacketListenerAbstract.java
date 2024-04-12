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

        this.serverSidedStatusAllowance = null;
        this.serverSidedLoginAllowance = null;
        this.serverSidedPlayAllowance = null;

        this.clientSidedStatusAllowance = null;
        this.clientSidedHandshakeAllowance = null;
        this.clientSidedLoginAllowance = null;
        this.clientSidedPlayAllowance = null;
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
        if (this.serverSidedStatusAllowance == null) {
            this.serverSidedStatusAllowance = new ImmutableSetCustom<>(statusPacketIDs);
        } else {
            this.serverSidedStatusAllowance.addAll(statusPacketIDs);
        }
    }

    public final void addServerSidedLoginFilter(Byte... loginPacketIDs) {
        if (this.serverSidedLoginAllowance == null) {
            this.serverSidedLoginAllowance = new ImmutableSetCustom<>(loginPacketIDs);
        } else {
            this.serverSidedLoginAllowance.addAll(loginPacketIDs);
        }
    }

    public final void addServerSidedPlayFilter(Byte... playPacketIDs) {
        if (this.serverSidedPlayAllowance == null) {
            this.serverSidedPlayAllowance = new ImmutableSetCustom<>(playPacketIDs);
        } else {
            this.serverSidedPlayAllowance.addAll(playPacketIDs);
        }
    }

    public final void addClientSidedStatusFilter(Byte... statusPacketIDs) {
        if (this.clientSidedStatusAllowance == null) {
            this.clientSidedStatusAllowance = new ImmutableSetCustom<>(statusPacketIDs);
        } else {
            this.clientSidedStatusAllowance.addAll(statusPacketIDs);
        }
    }

    public final void addClientSidedHandshakeFilter(Byte... handshakePacketIDs) {
        if (this.clientSidedHandshakeAllowance == null) {
            this.clientSidedHandshakeAllowance = new ImmutableSetCustom<>(handshakePacketIDs);
        } else {
            this.clientSidedHandshakeAllowance.addAll(handshakePacketIDs);
        }
    }

    public final void addClientSidedLoginFilter(Byte... loginPacketIDs) {
        if (this.clientSidedLoginAllowance == null) {
            this.clientSidedLoginAllowance = new ImmutableSetCustom<>(loginPacketIDs);
        } else {
            this.clientSidedLoginAllowance.addAll(loginPacketIDs);
        }
    }

    public final void addClientSidedPlayFilter(Byte... playPacketIDs) {
        if (this.clientSidedPlayAllowance == null) {
            this.clientSidedPlayAllowance = new ImmutableSetCustom<>(playPacketIDs);
        } else {
            this.clientSidedPlayAllowance.addAll(playPacketIDs);
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
        this.serverSidedStatusAllowance = new ImmutableSetCustom<>();
    }

    public final void filterServerSidedLogin() {
        this.serverSidedLoginAllowance = new ImmutableSetCustom<>();
    }

    public final void filterServerSidedPlay() {
        this.serverSidedPlayAllowance = new ImmutableSetCustom<>();
    }

    public final void filterClientSidedStatus() {
        this.clientSidedStatusAllowance = new ImmutableSetCustom<>();
    }

    public final void filterClientSidedHandshake() {
        this.clientSidedHandshakeAllowance = new ImmutableSetCustom<>();
    }

    public final void filterClientSidedLogin() {
        this.clientSidedLoginAllowance = new ImmutableSetCustom<>();
    }

    public final void filterClientSidedPlay() {
        this.clientSidedPlayAllowance = new ImmutableSetCustom<>();
    }
}
