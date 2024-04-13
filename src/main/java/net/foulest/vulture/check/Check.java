package net.foulest.vulture.check;

import io.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.event.eventtypes.CancellableEvent;
import io.github.retrooper.packetevents.event.eventtypes.CancellableNMSPacketEvent;
import io.github.retrooper.packetevents.packettype.PacketType;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import lombok.Getter;
import lombok.Setter;
import net.foulest.vulture.Vulture;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.event.MovementEvent;
import net.foulest.vulture.event.RotationEvent;
import net.foulest.vulture.util.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

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

    public Check(PlayerData playerData) throws ClassNotFoundException {
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
    public void handle(CancellableNMSPacketEvent nmsPacketEvent, byte packetId,
                       NMSPacket nmsPacket, Object packet, long timestamp) {
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
    }

    /**
     * This method is used to flag the player and cancel an event.
     *
     * @param event   the event to cancel
     * @param verbose the optional data to include in the flag
     */
    protected final void flag(boolean setback, @NotNull CancellableEvent event, String... verbose) {
        event.setCancelled(true);
        flag(setback, verbose);
    }

    /**
     * This method is used to flag the player with the given data.
     * <p>
     * When a player is flagged, all online staff members are alerted with the check they flagged and the data
     *
     * @param verbose the optional data to include in the flag
     */
    protected final void flag(boolean setback, String... verbose) {
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

        // Handles adding a violation.
        handleNewViolation(verbose);

        // Handles sending the alert message.
        handleAlert(verboseString);

        // Handles the punishment to execute.
        handlePunishment(verboseString);
    }

    /**
     * Handles adding a new violation to the player's data.
     *
     * @param verbose The verbose to add to the violation.
     */
    private void handleNewViolation(String... verbose) {
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
        } catch (ConcurrentModificationException ex) {
            ex.printStackTrace();
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
    }

    /**
     * Handles sending the alert message.
     *
     * @param verbose The verbose to add to the alert message.
     */
    private void handleAlert(String verbose) {
        MessageUtil.sendAlert("&f" + player.getName() + " &7failed &f"
                + checkInfo.name() + " &c(x" + violations + ")"
                + (Vulture.instance.verboseMode ? verbose : ""));
    }

    /**
     * Handles the punishment to execute.
     *
     * @param verbose The verbose to add to the punishment.
     */
    private void handlePunishment(String verbose) {
        if (violations >= checkInfo.maxViolations() && !checkInfo.experimental() && !checkInfo.banCommand().isEmpty()) {
            // Pauses any new violations from being added.
            playerData.setNewViolationsPaused(true);

            boolean kicking = (checkInfo.banCommand().startsWith("vulture kick")
                    || checkInfo.banCommand().startsWith("kick"));

            // Sends the private punishment message.
            MessageUtil.sendAlert("&f" + player.getName() + " &7has been " + (kicking ? "kicked" : "banned")
                    + " for failing &f" + checkInfo.name() + " &c(x" + violations + ")" + verbose);

            // Sends the public punishment message, if one is set.
            // Punishment messages are not sent if the player is being kicked.
            if (!Settings.banMessage.isEmpty() && !kicking) {
                List<String> banMessageEdited = new ArrayList<>(Settings.banMessage);
                banMessageEdited.replaceAll(s -> s.replace("%player%", player.getName()));
                banMessageEdited.replaceAll(s -> s.replace("%check%", checkInfo.name()));
                MessageUtil.broadcast(banMessageEdited);
            }

            // Executes the punishment command.
            TaskUtil.runTask(() -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), checkInfo.banCommand()
                    .replace("%player%", player.getName())
                    .replace("%check%", checkInfo.name())));
        }
    }
}
