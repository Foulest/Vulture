package net.foulest.vulture;

import dev._2lstudios.hamsterapi.HamsterAPI;
import io.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.settings.PacketEventsSettings;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import net.foulest.vulture.cmds.VultureCmd;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.data.PlayerDataManager;
import net.foulest.vulture.listeners.CommandListener;
import net.foulest.vulture.listeners.PlayerDataListener;
import net.foulest.vulture.processor.type.PacketProcessor;
import net.foulest.vulture.util.MessageUtil;
import net.foulest.vulture.util.Settings;
import net.foulest.vulture.util.TaskUtil;
import net.foulest.vulture.util.command.CommandFramework;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

/**
 * Main class for Vulture.
 *
 * @author Foulest
 * @project Vulture
 */
@Getter
public class Vulture extends JavaPlugin {

    public static Vulture instance;
    public CommandFramework framework;
    public PacketEvents packetEvents;
    public PacketProcessor packetProcessor;
    public Runnable packetTracker;
    public Runnable lagTracker;
    public boolean loaded = false;
    public boolean debug = false;
    public long lastTimestamp;
    public int ticksRunningSlow;
    public int ticksRunningFast;
    public long lastLaggingSlow;
    public long lastLaggingFast;

    @Override
    public void onLoad() {
        // Sets the instance.
        instance = this;

        // Loads PacketEvents.
        MessageUtil.log(Level.INFO, "Loading PacketEvents...");
        packetEvents = PacketEvents.create(this);
        packetEvents.load();
    }

    @Override
    @SneakyThrows
    public void onEnable() {
        // Kicks all online players.
        Bukkit.getOnlinePlayers().forEach(player -> player.kickPlayer("Disconnected"));

        // Calculates server lag every tick.
        MessageUtil.log(Level.INFO, "Initializing Lag Tracker...");
        lagTracker = () -> {
            // Timestamps the tick to calculate server lag.
            long now = System.currentTimeMillis();
            long diff = now - lastTimestamp;

            // Calculates the server lag.
            if (diff != now) {
                if (diff > 52.0) {
                    ticksRunningSlow++;
                    ticksRunningFast = 0;

                    if (ticksRunningSlow >= 3 || diff > 100.0) {
                        lastLaggingSlow = now;
                    }

                } else if (diff < 48.0) {
                    ticksRunningFast++;
                    ticksRunningSlow = 0;

                    if (ticksRunningFast >= 4 || diff > 100.0) {
                        lastLaggingFast = now;
                    }

                } else {
                    ticksRunningSlow = 0;
                    ticksRunningFast = 0;
                }
            }

            // Sets the last timestamp to the current time.
            lastTimestamp = now;
        };
        TaskUtil.runTaskTimer(lagTracker, 0L, 1L);

        // Resets the packets sent in the tick for all players every tick.
        MessageUtil.log(Level.INFO, "Initializing Packet Tracker...");
        packetTracker = () -> {
            // Clears the packet counts for all players.
            for (PlayerData playerData : PlayerDataManager.playerDataMap.values()) {
                synchronized (playerData.getPacketCounts()) {
                    playerData.getPacketCounts().clear();
                }

                playerData.setPacketsSentPerTick(0);
            }
        };
        TaskUtil.runTaskTimer(packetTracker, 0L, 1L);

        // Initializes the Command Framework.
        MessageUtil.log(Level.INFO, "Initializing Command Framework...");
        framework = new CommandFramework(this);

        // Initializes PacketEvents.
        MessageUtil.log(Level.INFO, "Initializing PacketEvents...");
        packetEvents.init(new PacketEventsSettings()
                .checkForUpdates(false)
                .bStats(false));

        // Initializes HamsterAPI.
        MessageUtil.log(Level.INFO, "Initializing HamsterAPI...");
        HamsterAPI.initialize();

        // Loads the plugin's commands.
        MessageUtil.log(Level.INFO, "Loading Packet Processors...");
        packetProcessor = new PacketProcessor();

        // Creates the default settings config.
        MessageUtil.log(Level.INFO, "Loading Settings...");
        Settings.setupSettings();
        Settings.loadSettings();

        // Loads the plugin's listeners.
        MessageUtil.log(Level.INFO, "Loading Listeners...");
        loadListeners(new PlayerDataListener(), new CommandListener());

        // Loads the plugin's commands.
        MessageUtil.log(Level.INFO, "Loading Commands...");
        loadCommands(new VultureCmd());

        MessageUtil.log(Level.INFO, "Loaded successfully.");
        loaded = true;
    }

    @Override
    public void onDisable() {
        // Terminates PacketEvents.
        MessageUtil.log(Level.INFO, "Terminating PacketEvents...");
        packetEvents.terminate();

        // Saves all online players' player data.
        MessageUtil.log(Level.INFO, "Saving Player Data...");
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            PlayerData playerData = PlayerDataManager.getPlayerData(player);

            if (playerData != null) {
                HamsterAPI.uninject(playerData);
            }

            PlayerDataManager.removePlayerData(player);
        }

        // Saves the settings.
        MessageUtil.log(Level.INFO, "Saving Settings...");
        Settings.saveSettings();

        MessageUtil.log(Level.INFO, "Shut down successfully.");
    }

    /**
     * Loads the plugin's listeners.
     *
     * @param listeners Listener to load.
     */
    private void loadListeners(@NonNull Listener... listeners) {
        for (Listener listener : listeners) {
            Bukkit.getPluginManager().registerEvents(listener, this);
        }
    }

    /**
     * Loads the plugin's commands.
     *
     * @param commands Command to load.
     */
    private void loadCommands(@NonNull Object... commands) {
        for (Object command : commands) {
            framework.registerCommands(command);
        }
    }

    /**
     * Gets the time since the server was lagging (fast).
     *
     * @return The time since the server was lagging (fast).
     */
    public static long timeSinceLaggingFast() {
        return System.currentTimeMillis() - instance.lastLaggingFast;
    }

    /**
     * Gets the time since the server was lagging (slow).
     *
     * @return The time since the server was lagging (slow).
     */
    public static long timeSinceLaggingSlow() {
        return System.currentTimeMillis() - instance.lastLaggingSlow;
    }
}
