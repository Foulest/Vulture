package net.foulest.vulture.util;

import io.github.retrooper.packetevents.event.eventtypes.CancellableEvent;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.foulest.vulture.Vulture;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.data.PlayerDataManager;
import dev._2lstudios.hamsterapi.HamsterAPI;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Utility class for kicking players.
 *
 * @author Foulest
 * @project Vulture
 */
@Getter
@Setter
public final class KickUtil {

    private static final Set<UUID> currentlyKicking = Collections.synchronizedSet(new HashSet<>());

    private KickUtil() {
        throw new UnsupportedOperationException("KickUtil should not be instantiated.");
    }

    public static void kickPlayer(@NonNull Player player, @NonNull String debugMessage) {
        kick(player, null, debugMessage, "Disconnected", true);
    }

    public static void kickPlayer(@NonNull Player player, @NonNull String debugMessage, @NonNull String reason) {
        kick(player, null, debugMessage, reason, true);
    }

    public static void kickPlayer(@NonNull Player player, @NonNull CancellableEvent event,
                                  @NonNull String debugMessage, @NonNull String reason) {
        kick(player, event, debugMessage, reason, true);
    }

    public static void kickPlayer(@NonNull Player player, @NonNull CancellableEvent event,
                                  @NonNull String debugMessage) {
        kick(player, event, debugMessage, "Disconnected", true);
    }

    public static void kickPlayer(@NonNull Player player, @NonNull String debugMessage, boolean announceKick) {
        kick(player, null, debugMessage, "Disconnected", announceKick);
    }

    public static void kickPlayer(@NonNull Player player, @NonNull String debugMessage,
                                  @NonNull String reason, boolean announceKick) {
        kick(player, null, debugMessage, reason, announceKick);
    }

    /**
     * Kicks the specified player synchronously with the given reason.
     *
     * @param player The player to be kicked.
     * @param reason The reason for kicking the player.
     */
    public static void kick(@NonNull Player player, CancellableEvent eventToCancel,
                            @NonNull String debugMessage,
                            @NonNull String reason, boolean announceKick) {
        synchronized (currentlyKicking) {
            if (currentlyKicking.contains(player.getUniqueId())) {
                // Already in the process of kicking this player.
                return;
            }

            currentlyKicking.add(player.getUniqueId());
        }

        PlayerData playerData = PlayerDataManager.getPlayerData(player);

        if (announceKick) {
            MessageUtil.sendAlert("&f" + player.getName() + " &7has been kicked for cheating."
                    + (debugMessage.isEmpty() ? "" : " &8(" + debugMessage + "&8)"));
        }

        if (eventToCancel != null) {
            eventToCancel.setCancelled(true);
        }

        // Kicks the player with a message.
        if (Vulture.instance.isEnabled()) {
            TaskUtil.run(() -> {
                if (player.isOnline()) {
                    player.kickPlayer(MessageUtil.colorize(reason));
                } else {
                    HamsterAPI.closeChannel(playerData);
                }

                currentlyKicking.remove(player.getUniqueId());
            });
        } else {
            HamsterAPI.closeChannel(playerData);
            currentlyKicking.remove(player.getUniqueId());
        }
    }

    /**
     * Checks if the specified player is being kicked.
     *
     * @param player The player to check.
     * @return If the player is being kicked.
     */
    public static boolean isPlayerBeingKicked(@NonNull Player player) {
        synchronized (currentlyKicking) {
            return currentlyKicking.contains(player.getUniqueId());
        }
    }
}
