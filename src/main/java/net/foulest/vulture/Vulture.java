package net.foulest.vulture;

import dev.thomazz.pledge.Pledge;
import dev.thomazz.pledge.pinger.ClientPinger;
import dev.thomazz.pledge.pinger.ClientPingerListener;
import io.github.retrooper.packetevents.PacketEvents;
import lombok.Getter;
import lombok.SneakyThrows;
import net.foulest.vulture.cmds.VultureCmd;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.data.PlayerDataManager;
import net.foulest.vulture.listeners.CommandListener;
import net.foulest.vulture.listeners.ExploitListener;
import net.foulest.vulture.listeners.PlayerDataListener;
import net.foulest.vulture.processor.type.PacketDecodeProcessor;
import net.foulest.vulture.processor.type.PacketReceiveProcessor;
import net.foulest.vulture.processor.type.PacketSendProcessor;
import net.foulest.vulture.util.MessageUtil;
import net.foulest.vulture.util.Settings;
import net.foulest.vulture.util.command.CommandFramework;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

/**
 * Main class for Vulture.
 *
 * @author Foulest
 * @project Vulture
 */
@Getter
public class Vulture extends JavaPlugin implements ClientPingerListener {

    @Getter
    public static Vulture instance;
    public Pledge pledge;
    public CommandFramework framework;
    public PacketEvents packetEvents;
    public PacketDecodeProcessor decodeProcessor;
    public PacketReceiveProcessor receiveProcessor;
    public PacketSendProcessor sendProcessor;
    public boolean debugMode;

    @Override
    public void onLoad() {
        // Sets the instance.
        instance = this;

        // Loads PacketEvents.
        packetEvents = PacketEvents.create(this);
        packetEvents.load();
    }

    @Override
    @SneakyThrows
    public void onEnable() {
        // Kicks all online players.
        Bukkit.getOnlinePlayers().forEach(player -> player.kickPlayer("Disconnected"));

        // Initializes Pledge.
        MessageUtil.log(Level.INFO, "Loading Pledge...");
        pledge = Pledge.getOrCreate(this);
        ClientPinger pinger = pledge.createPinger(-1, -400);
        pinger.attach(this);

        // Initializes PacketEvents.
        MessageUtil.log(Level.INFO, "Loading PacketEvents...");
        packetEvents.init();

        // Loads the plugin's commands.
        MessageUtil.log(Level.INFO, "Loading Packet Processors...");
        decodeProcessor = new PacketDecodeProcessor();
        receiveProcessor = new PacketReceiveProcessor();
        sendProcessor = new PacketSendProcessor();

        // Creates the default settings config.
        MessageUtil.log(Level.INFO, "Loading Settings...");
        Settings.loadSettings();

        // Loads the plugin's listeners.
        MessageUtil.log(Level.INFO, "Loading Listeners...");
        loadListeners(new CommandListener(), new ExploitListener(), new PlayerDataListener());

        // Initializes the Command Framework.
        MessageUtil.log(Level.INFO, "Loading Command Framework...");
        framework = new CommandFramework(this);

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

    /**
     * Called when a ping packet is sent to a {@link Player}.
     *
     * @param player - Player that the ping response is sent to
     * @param id     - ID of ping
     */
    @Override
    public void onPingSendStart(Player player, int id) {
        PlayerData playerData = PlayerDataManager.getPlayerData(player);

        if (playerData != null) {
            playerData.onPingSendStart();
        }
    }

    /**
     * Called when a ping packet is sent to a {@link Player}.
     *
     * @param player - Player that the ping response is sent to
     * @param id     - ID of ping
     */
    @Override
    public void onPingSendEnd(Player player, int id) {
        PlayerData playerData = PlayerDataManager.getPlayerData(player);

        if (playerData != null) {
            playerData.onPingSendEnd();
        }
    }

    /**
     * Called when a pong packet is received from a {@link Player}.
     *
     * @param player - Player that the ping response is received from
     * @param id     - ID of ping
     */
    @Override
    public void onPongReceiveStart(Player player, int id) {
        PlayerData playerData = PlayerDataManager.getPlayerData(player);

        if (playerData != null) {
            playerData.onPongReceiveStart();
        }
    }

    /**
     * Called when a pong packet is received from a {@link Player}.
     *
     * @param player - Player that the ping response is received from
     * @param id     - ID of ping
     */
    @Override
    public void onPongReceiveEnd(Player player, int id) {
        PlayerData playerData = PlayerDataManager.getPlayerData(player);

        if (playerData != null) {
            playerData.onPongReceiveEnd();
        }
    }

    /**
     * Gets the current server time.
     *
     * @return Current server time
     */
    public long getCurrentServerTime() {
        return System.currentTimeMillis(); // Same as current system time
    }
}
