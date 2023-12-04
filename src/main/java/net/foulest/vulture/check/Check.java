package net.foulest.vulture.check;

import io.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.event.eventtypes.CancellableNMSPacketEvent;
import io.github.retrooper.packetevents.packettype.PacketType;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.event.MovementEvent;
import net.foulest.vulture.event.RotationEvent;
import dev._2lstudios.hamsterapi.HamsterAPI;
import net.foulest.vulture.util.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

/**
 * Abstract class for checks.
 *
 * @author Foulest
 * @project Vulture
 */
@Getter
@Setter
public class Check {

    protected final PlayerData playerData;
    protected final Player player;
    private final CheckInfo checkInfo;
    private int violations;

    public Check(@NonNull PlayerData playerData) throws ClassNotFoundException {
        if (!getClass().isAnnotationPresent(CheckInfo.class)) {
            throw new ClassNotFoundException("Check is missing @CheckInfo annotation.");
        }

        this.playerData = playerData;
        player = playerData.getPlayer();
        checkInfo = getClass().getAnnotation(CheckInfo.class);
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
    public void handle(@NonNull CancellableNMSPacketEvent nmsPacketEvent, byte packetId,
                       @NonNull NMSPacket nmsPacket, @NonNull Object packet, long timestamp) {
    }

    /**
     * This method is fired when the player rotates.
     *
     * @param event     The rotation event.
     * @param timestamp The timestamp the event was handled.
     * @see Location
     * @see RotationEvent
     */
    public void handle(@NonNull RotationEvent event, long timestamp) {
    }

    /**
     * This method is fired when the player moves.
     *
     * @param event     The movement event.
     * @param timestamp The timestamp the event was handled.
     * @see Location
     * @see MovementEvent
     */
    public void handle(@NonNull MovementEvent event, long timestamp) {
    }

    /**
     * This method is used to flag the player without debugging.
     *
     * @param verbose the optional data to include in the flag
     */
    protected final void flag(boolean setback, @NonNull String... verbose) {
        flag(setback, false, verbose);
    }

    /**
     * This method is used to flag the player with debugging.
     *
     * @param verbose the optional data to include in the flag
     */
    protected final void debug(@NonNull String... verbose) {
        flag(false, true, verbose);
    }

    /**
     * This method is used to flag the player with the given data.
     * <p>
     * When a player is flagged, all online staff members are alerted with the check they flagged and the data
     *
     * @param verbose the optional data to include in the flag
     */
    protected final void flag(boolean setback, boolean debug, @NonNull String... verbose) {
        // Checks the player for exemptions.
        if (playerData.isNewViolationsPaused()
                || KickUtil.isPlayerBeingKicked(player)
                || PacketEvents.get().getServerUtils().getTPS() < 18
                || !checkInfo.enabled()) {
            return;
        }

        // Sets the player back to their previous position.
        if (setback) {
            SetbackUtil.setback(player);
        }

        String verboseString = verbose.length == 0 ? "" : " &7[" + String.join(", ", verbose) + "]";

        // If debug is enabled, send the debug message and return.
        if (debug) {
            MessageUtil.sendAlert("&f" + player.getName() + " &7failed &f" + checkInfo.name()
                    + " &c(Debug)" + verboseString);
            return;
        }

        // Removes older violations before adding new ones.
        try {
            for (Violation violation : playerData.getViolations()) {
                if (System.currentTimeMillis() - violation.getTimestamp() > Settings.resetViolations * 1000L) {
                    playerData.getViolations().remove(violation);

                    // Decrements the violations.
                    if (violation.getCheckInfo() == checkInfo) {
                        violations--;
                    }
                }
            }
        } catch (ConcurrentModificationException ignored) {
        }

        // Increments the violations.
        violations++;

        // Creates a new violation.
        Violation violation = new Violation(
                checkInfo,
                verbose,
                violations,
                player.getLocation(),
                PacketEvents.get().getPlayerUtils().getPing(player),
                PacketEvents.get().getServerUtils().getTPS(),
                System.currentTimeMillis()
        );

        // Adds the violation to the player's violations.
        playerData.getViolations().add(violation);

        // Sends the alert message.
        MessageUtil.sendAlert("&f" + player.getName() + " &7failed &f" + checkInfo.name()
                + " &c(x" + violations + ")" + verboseString);

        // Handles the ban/kick.
        if (violations >= checkInfo.maxViolations() && !checkInfo.experimental() && !checkInfo.banCommand().isEmpty()) {
            playerData.setNewViolationsPaused(true);

            boolean kicking = (checkInfo.banCommand().startsWith("vulture kick")
                    || checkInfo.banCommand().startsWith("kick"));

            // Sends the ban alert message.
            MessageUtil.sendAlert("&f" + player.getName() + " &7has been " + (kicking ? "kicked" : "banned")
                    + " for failing &f" + checkInfo.name() + " &c(x" + violations + ")" + verboseString);

            // Broadcasts the ban message, if one is set.
            if (!Settings.banMessage.isEmpty() && !kicking) {
                List<String> banMessageEdited = new ArrayList<>(Settings.banMessage);
                banMessageEdited.replaceAll(s -> s.replace("%player%", player.getName()));
                banMessageEdited.replaceAll(s -> s.replace("%check%", checkInfo.name()));
                MessageUtil.broadcastList(banMessageEdited);
            }

            // Executes the ban command.
            TaskUtil.run(() -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), checkInfo.banCommand()
                    .replace("%player%", player.getName())
                    .replace("%check%", checkInfo.name())));

            // Closes the player's channel to ensure they are disconnected.
            HamsterAPI.closeChannel(playerData);
        }
    }
}
