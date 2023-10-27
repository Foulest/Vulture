package net.foulest.vulture.event;

import io.github.retrooper.packetevents.event.eventtypes.CancellableEvent;
import io.github.retrooper.packetevents.event.eventtypes.CancellableNMSPacketEvent;
import io.github.retrooper.packetevents.packetwrappers.play.in.flying.WrappedPacketInFlying;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.foulest.vulture.data.PlayerData;
import org.bukkit.Location;

@Getter
@Setter
@AllArgsConstructor
public class MovementEvent implements CancellableEvent {

    public final PlayerData playerData;
    public final WrappedPacketInFlying to;
    public final WrappedPacketInFlying from;
    public final CancellableNMSPacketEvent event;

    @Override
    public boolean isCancelled() {
        return event.isCancelled();
    }

    @Override
    public void setCancelled(boolean cancelled) {
        event.setCancelled(cancelled);
    }

    public double getDeltaX() {
        return to.getPosition().getX() - from.getPosition().getX();
    }

    public double getDeltaY() {
        return to.getPosition().getY() - from.getPosition().getY();
    }

    public double getDeltaZ() {
        return to.getPosition().getZ() - from.getPosition().getZ();
    }

    public double getDeltaXZ() {
        double deltaX = getDeltaX();
        double deltaZ = getDeltaZ();
        return Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
    }

    public boolean isTeleport(PlayerData playerData) {
        return playerData.isTeleporting(to.getPosition())
                || playerData.isTeleporting(from.getPosition());
    }

    public boolean isYLevel() {
        return to.getPosition().getY() % 0.015625 == 0.0;
    }

    public Location getToLocation() {
        return new Location(playerData.getPlayer().getWorld(), to.getPosition().getX(),
                to.getPosition().getY(), to.getPosition().getZ());
    }

    public Location getFromLocation() {
        return new Location(playerData.getPlayer().getWorld(), from.getPosition().getX(),
                from.getPosition().getY(), from.getPosition().getZ());
    }
}
