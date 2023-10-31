package net.foulest.vulture;

import io.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.settings.PacketEventsSettings;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import net.foulest.vulture.cmds.VultureCmd;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.data.PlayerDataManager;
import net.foulest.vulture.hamsterapi.HamsterAPI;
import net.foulest.vulture.listeners.CommandListener;
import net.foulest.vulture.listeners.PlayerDataListener;
import net.foulest.vulture.processor.type.PacketProcessor;
import net.foulest.vulture.util.Settings;
import net.foulest.vulture.util.command.CommandFramework;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main class for Vulture.
 *
 * @author Foulest
 * @project Vulture
 */
@Getter
public class Vulture extends JavaPlugin {

    public static Vulture instance;
    public final String pluginName = "Vulture";
    public boolean loaded = false;
    public CommandFramework framework;
    public PacketEvents packetEvents;
    public PacketProcessor packetProcessor;

    @Override
    public void onLoad() {
        // Sets the instance.
        instance = this;

        // Loads PacketEvents.
        Bukkit.getLogger().info("[" + pluginName + "] Loading PacketEvents...");
        packetEvents = PacketEvents.create(this);
        packetEvents.load();
    }

    @Override
    @SneakyThrows
    public void onEnable() {
        // Kick all online players.
        Bukkit.getOnlinePlayers().forEach(player -> player.kickPlayer("Disconnected"));

        // Initializes the Command Framework.
        Bukkit.getLogger().info("[" + pluginName + "] Initializing Command Framework...");
        framework = new CommandFramework(this);

        // Initializes PacketEvents.
        Bukkit.getLogger().info("[" + pluginName + "] Initializing PacketEvents...");
        packetEvents.init(new PacketEventsSettings()
                .checkForUpdates(false)
                .bStats(false));

        // Initializes HamsterAPI.
        Bukkit.getLogger().info("[" + pluginName + "] Initializing HamsterAPI...");
        HamsterAPI.initialize();

        // Loads the plugin's commands.
        Bukkit.getLogger().info("[" + pluginName + "] Loading Packet Processors...");
        packetProcessor = new PacketProcessor();

        // Creates the default settings config.
        Bukkit.getLogger().info("[" + pluginName + "] Loading Settings...");
        Settings.setupSettings();
        Settings.loadSettings();

        // Loads the plugin's listeners.
        Bukkit.getLogger().info("[" + pluginName + "] Loading Listeners...");
        loadListeners(new PlayerDataListener(), new CommandListener());

        // Loads the plugin's commands.
        Bukkit.getLogger().info("[" + pluginName + "] Loading Commands...");
        loadCommands(new VultureCmd());

        Bukkit.getLogger().info("[" + pluginName + "] Loaded successfully.");
        loaded = true;
    }

    @Override
    public void onDisable() {
        // Terminates PacketEvents.
        Bukkit.getLogger().info("[" + pluginName + "] Terminating PacketEvents...");
        packetEvents.terminate();

        // Saves all online players' player data.
        Bukkit.getLogger().info("[" + pluginName + "] Saving player data...");
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            PlayerData playerData = PlayerDataManager.getPlayerData(player);

            if (playerData != null) {
                HamsterAPI.uninject(playerData);
            }

            PlayerDataManager.removePlayerData(player);
        }

        // Saves the settings.
        Bukkit.getLogger().info("[" + pluginName + "] Saving Settings...");
        Settings.saveSettings();

        Bukkit.getLogger().info("[" + pluginName + "] Shut down successfully.");
    }

    /**
     * Loads the plugin's listeners.
     * <p>
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
     * <p>
     *
     * @param commands Command to load.
     */
    private void loadCommands(@NonNull Object... commands) {
        for (Object command : commands) {
            framework.registerCommands(command);
        }
    }
}
