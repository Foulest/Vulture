package net.foulest.vulture;

import io.github.retrooper.packetevents.PacketEvents;
import lombok.Getter;
import lombok.SneakyThrows;
import net.foulest.vulture.cmds.VultureCmd;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.data.PlayerDataManager;
import net.foulest.vulture.listeners.CommandListener;
import net.foulest.vulture.listeners.ExploitListener;
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
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Main class for Vulture.
 *
 * @author Foulest
 * @project Vulture
 */
@Getter
public class Vulture extends JavaPlugin {

    @Getter
    public static Vulture instance;
    public static Map<Player, Integer> packetsSentPerTick = new ConcurrentHashMap<>();
    public CommandFramework framework;
    public PacketEvents packetEvents;
    public PacketProcessor packetProcessor;
    public Runnable tickReset;
    public Runnable lagTracker;
    public boolean debugMode;
    public boolean verboseMode;

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

        // Resets the packets sent in the tick for all players every tick.
        tickReset = () -> {
            if (!Vulture.packetsSentPerTick.isEmpty()) {
                Vulture.packetsSentPerTick = new ConcurrentHashMap<>();
            }

            // Clears the packet counts for all players.
            for (PlayerData playerData : PlayerDataManager.playerDataMap.values()) {
                synchronized (playerData.getPacketCounts()) {
                    playerData.getPacketCounts().clear();
                }
            }
        };
        TaskUtil.runTaskTimer(tickReset, 0L, 1L);

        // Initializes the Command Framework.
        MessageUtil.log(Level.INFO, "Initializing Command Framework...");
        framework = new CommandFramework(this);

        // Initializes PacketEvents.
        MessageUtil.log(Level.INFO, "Initializing PacketEvents...");
        packetEvents.init();

        // Loads the plugin's commands.
        MessageUtil.log(Level.INFO, "Loading Packet Processors...");
        packetProcessor = new PacketProcessor();

        // Creates the default settings config.
        MessageUtil.log(Level.INFO, "Loading Settings...");
        Settings.loadSettings();

        // Loads the plugin's listeners.
        MessageUtil.log(Level.INFO, "Loading Listeners...");
        loadListeners(new CommandListener(), new ExploitListener(), new PlayerDataListener());

        // Loads the plugin's commands.
        MessageUtil.log(Level.INFO, "Loading Commands...");
        loadCommands(new VultureCmd());

        MessageUtil.log(Level.INFO, "Loaded successfully.");
    }

    @Override
    public void onDisable() {
        // Terminates PacketEvents.
        MessageUtil.log(Level.INFO, "Terminating PacketEvents...");
        packetEvents.terminate();

        // Saves all online players' player data.
        MessageUtil.log(Level.INFO, "Saving Player Data...");
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            PlayerDataManager.removePlayerData(player);
        }

        MessageUtil.log(Level.INFO, "Shut down successfully.");
    }

    /**
     * Loads the plugin's listeners.
     *
     * @param listeners Listener to load.
     */
    private void loadListeners(Listener @NotNull ... listeners) {
        for (Listener listener : listeners) {
            Bukkit.getPluginManager().registerEvents(listener, this);
        }
    }

    /**
     * Loads the plugin's commands.
     *
     * @param commands Command to load.
     */
    private void loadCommands(Object @NotNull ... commands) {
        for (Object command : commands) {
            framework.registerCommands(command);
        }
    }
}
