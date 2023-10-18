package net.foulest.vulture.util;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.foulest.vulture.action.ActionType;
import net.foulest.vulture.data.DataManager;
import net.foulest.vulture.data.PlayerData;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.Vector;

@Getter
@Setter
public class SetbackUtil {

    public static void setback(@NonNull Player player) {
        PlayerData playerData = DataManager.getPlayerData(player);
        setback(player, playerData.getLastOnGroundLocation());
    }

    public static void setback(@NonNull Player player, @NonNull Location location) {
        PlayerData playerData = DataManager.getPlayerData(player);

        if (player.isInsideVehicle()) {
            return;
        }

        TaskUtil.run(() -> {
            playerData.setTimestamp(ActionType.SETBACK);

            location.setPitch(player.getLocation().getPitch());
            location.setYaw(player.getLocation().getYaw());

            player.setVelocity(new Vector(player.getVelocity().getX(), 0, player.getVelocity().getZ()));
            player.teleport(location, PlayerTeleportEvent.TeleportCause.UNKNOWN);
            player.setVelocity(new Vector(player.getVelocity().getX(), 0, player.getVelocity().getZ()));
        });
    }
}
