package net.foulest.vulture.util;

import lombok.Getter;
import lombok.Setter;
import net.foulest.vulture.action.ActionType;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.data.PlayerDataManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
public class SetbackUtil {

    /**
     * Sets the player back to their last on ground location.
     *
     * @param player The player to set back.
     */
    public static void setback(Player player) {
        PlayerData playerData = PlayerDataManager.getPlayerData(player);
        playerData.setTimestamp(ActionType.SETBACK);

        if (playerData.getTimestamp(ActionType.LAST_ON_GROUND_LOCATION_SET) == 0) {
            setback(player, player.getLocation());
        } else {
            setback(player, playerData.getLastOnGroundLocation());
        }
    }

    /**
     * Sets the player back to the specified location.
     *
     * @param player   The player to set back.
     * @param location The location to set the player back to.
     */
    public static void setback(@NotNull Player player, Location location) {
        if (player.isInsideVehicle()) {
            return;
        }

        TaskUtil.runTask(() -> {
            location.setPitch(player.getLocation().getPitch());
            location.setYaw(player.getLocation().getYaw());

            player.setVelocity(new Vector(player.getVelocity().getX(), 0, player.getVelocity().getZ()));
            player.teleport(location, PlayerTeleportEvent.TeleportCause.UNKNOWN);
            player.setVelocity(new Vector(player.getVelocity().getX(), 0, player.getVelocity().getZ()));
        });
    }
}
