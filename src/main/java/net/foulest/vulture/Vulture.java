/*
 * Vulture - a server protection plugin designed for Minecraft 1.8.9 servers.
 * Copyright (C) 2024 Foulest (https://github.com/Foulest)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package net.foulest.vulture;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import net.foulest.packetevents.PacketEvents;
import net.foulest.pledge.Pledge;
import net.foulest.pledge.pinger.ClientPinger;
import net.foulest.pledge.pinger.ClientPingerListener;
import net.foulest.vulture.cmds.VultureCmd;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.data.PlayerDataManager;
import net.foulest.vulture.listeners.CommandListener;
import net.foulest.vulture.listeners.ExploitListener;
import net.foulest.vulture.listeners.ModDataListener;
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
 */
@Getter
public class Vulture extends JavaPlugin implements ClientPingerListener {

    @Getter
    @Setter
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
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.kickPlayer("Disconnected");
        }

        // Initializes Pledge.
        MessageUtil.log(Level.INFO, "Loading Pledge...");
        pledge = Pledge.getOrCreate(this);
        ClientPinger pinger = pledge.createPinger(-1, -400);
        pinger.attach(this);

        // Initializes PacketEvents.
        MessageUtil.log(Level.INFO, "Loading PacketEvents...");
        packetEvents.init();

        // Loads the plugin's settings.
        MessageUtil.log(Level.INFO, "Loading Settings...");
        Settings.loadSettings();

        // Loads the plugin's commands.
        MessageUtil.log(Level.INFO, "Loading Packet Processors...");
        decodeProcessor = new PacketDecodeProcessor();
        receiveProcessor = new PacketReceiveProcessor();
        sendProcessor = new PacketSendProcessor();

        // Loads the plugin's listeners.
        MessageUtil.log(Level.INFO, "Loading Listeners...");
        loadListeners(new CommandListener(), new ExploitListener(), new PlayerDataListener(), new ModDataListener());

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
}
