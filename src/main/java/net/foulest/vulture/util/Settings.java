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
package net.foulest.vulture.util;

import lombok.Cleanup;
import lombok.Data;
import net.foulest.vulture.Vulture;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckManager;
import net.foulest.vulture.check.type.pingspoof.PingSpoofB;
import net.foulest.vulture.util.yaml.CustomYamlConfiguration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;

/**
 * Utility class for settings.
 *
 * @author Foulest
 */
@Data
@SuppressWarnings("WeakerAccess")
public class Settings {

    // File settings
    public static File file;
    public static FileConfiguration config;
    public static @NotNull String fileName = "config.yml";

    // General settings
    public static String prefix;
    public static List<String> banMessage;
    public static long resetViolations;

    // Blocked payloads
    public static List<String> blockedPayloads;

    // Blocked mods
    public static List<String> blockedMods;

    // Blocked commands
    public static List<String> blockedCommands;

    // IP Whitelist settings
    public static boolean ipWhitelistEnabled;
    public static Map<UUID, List<String>> ipWhitelist;

    // Exploit protections
    public static boolean worldInvalidBlockBreak;
    public static boolean worldInvalidBlockPlace;
    public static boolean worldBlockNetherCeiling;
    public static boolean worldBlockCommandsWhileSleeping;
    public static boolean worldFixPearlPhasing;
    public static boolean portalsBlockAffectedMobs;
    public static boolean pistonsBlockMovingEntities;

    // Packet protections
    public static int maxPacketsPerTick;
    public static int maxPacketsPerSecond;
    public static int maxPacketsSmoothed;
    public static long maxTimeOverServer;
    public static boolean invalidPacketSentToServer;

    // Packet protections; Abilities
    public static boolean abilitiesDuplicateFlying;
    public static boolean abilitiesInvalidFlightAllowed;
    public static boolean abilitiesInvalidFlying;
    public static boolean abilitiesInvalidCreativeMode;
    public static boolean abilitiesInvalidGodMode;

    // Packet protections; Beacon
    public static boolean beaconInvalidConditions;
    public static boolean beaconInvalidData;
    public static boolean beaconInvalidEffect;
    public static boolean beaconInvalidTier;

    // Packet protections; BlockPlacement
    public static boolean blockPlacementInvalidBlockPosition;
    public static boolean blockPlacementInvalidDistance;
    public static boolean blockPlacementInvalidUpCursorBounds;
    public static boolean blockPlacementInvalidUpCursorPosition;
    public static boolean blockPlacementInvalidUpPitch;
    public static boolean blockPlacementInvalidDownCursorBounds;
    public static boolean blockPlacementInvalidDownCursorPosition;
    public static boolean blockPlacementInvalidDownPitch;
    public static boolean blockPlacementInvalidEastCursorBounds;
    public static boolean blockPlacementInvalidEastCursorPosition;
    public static boolean blockPlacementInvalidEastYaw;
    public static boolean blockPlacementInvalidWestCursorBounds;
    public static boolean blockPlacementInvalidWestCursorPosition;
    public static boolean blockPlacementInvalidWestYaw;
    public static boolean blockPlacementInvalidNorthCursorBounds;
    public static boolean blockPlacementInvalidNorthCursorPosition;
    public static boolean blockPlacementInvalidNorthYaw;
    public static boolean blockPlacementInvalidSouthCursorBounds;
    public static boolean blockPlacementInvalidSouthCursorPosition;
    public static boolean blockPlacementInvalidSouthYaw;
    public static boolean blockPlacementInvalidOtherCursorPosition;
    public static boolean blockPlacementInvalidOtherBlockPosition;

    // Packet protections; BookEdit
    public static boolean bookEditInvalidConditions;
    public static boolean bookEditInvalidData;

    // Packet protections; BookOpen
    public static boolean bookOpenInvalidConditions;

    // Packet protections; BookSign
    public static boolean bookSignInvalidConditions;
    public static boolean bookSignInvalidData;

    // Packet protections; ChatMessage
    public static boolean chatMessageInvalidConditions;
    public static boolean chatMessageInvalidMessage;

    // Packet protections; CloseWindow
    public static boolean closeWindowInvalidConditions;

    // Packet protections; CommandBlock
    public static boolean commandBlockInvalidConditions;

    // Packet protections; Digging
    public static boolean diggingInvalidBlockType;
    public static boolean diggingInvalidBlockPosition;
    public static boolean diggingInvalidDistance;

    // Packet protections; DropItem
    public static boolean dropItemInvalidData;

    // Packet protections; EntityAction
    public static boolean entityActionInvalidEntityID;
    public static boolean entityActionInvalidJumpBoost;

    // Packet protections; Flying
    public static boolean flyingInvalidPitch;
    public static boolean flyingInvalidPositionData;
    public static boolean flyingInvalidRotationData;
    public static boolean flyingInvalidYData;

    // Packet protections; HeldItemChange
    public static boolean heldItemChangeInvalidConditions;
    public static boolean heldItemChangeInvalidSlot;
    public static boolean heldItemChangeInvalidSlotChange;

    // Packet protections; InteractEntity
    public static boolean interactEntityInvalidConditions;
    public static boolean interactEntityInvalidDistance;

    // Packet protections; ItemName
    public static boolean itemNameInvalidData;
    public static boolean itemNameInvalidSize;

    // Packet protections; OpenHorseInventory
    public static boolean openHorseInventoryInvalidConditions;
    public static boolean openHorseInventoryInvalidVehicle;
    public static boolean openHorseInventoryInvalidTamed;

    // Packet protections; PluginMessage
    public static boolean pluginMessageInvalidChannelName;
    public static boolean pluginMessageInvalidSize;

    // Packet protections; ReleaseUseItem
    public static boolean releaseUseItemInvalidData;

    // Packet protections; Respawn
    public static boolean respawnInvalidConditions;

    // Packet protections; RidingJump
    public static boolean ridingJumpInvalidConditions;
    public static boolean ridingJumpInvalidJumpBoost;

    // Packet protections; CreativeInventoryAction
    public static boolean creativeInventoryActionInvalidConditions;
    public static boolean creativeInventoryActionInvalidSlot;

    // Packet protections; Settings
    public static boolean settingsInvalidLocale;
    public static boolean settingsInvalidViewDistance;

    // Packet protections; Spectate
    public static boolean spectateInvalidConditions;
    public static boolean spectateInvalidTarget;

    // Packet protections; StartSneaking
    public static boolean startSneakingInvalidConditions;

    // Packet protections; SteerVehicle
    public static boolean steerVehicleInvalidConditions;
    public static boolean steerVehicleInvalidDismountValue;
    public static boolean steerVehicleInvalidNonDismountValue;
    public static boolean steerVehicleInvalidJump;
    public static boolean steerVehicleInvalidValue;

    // Packet protections; StopSleeping
    public static boolean stopSleepingInvalidConditions;

    // Packet protections; TabComplete
    public static boolean tabCompleteInvalidMessage;

    // Packet protections; TradeSelect
    public static boolean tradeSelectInvalidData;

    // Packet protections; UpdateSign
    public static boolean updateSignInvalidData;

    // Packet protections; WindowClick
    public static boolean windowClickInvalidCloneButton;
    public static boolean windowClickInvalidPickupAllButton;
    public static boolean windowClickInvalidPickupButton;
    public static boolean windowClickInvalidQuickCraftButton;
    public static boolean windowClickInvalidQuickMoveButton;
    public static boolean windowClickInvalidSwapButton;
    public static boolean windowClickInvalidThrowButton;
    public static boolean windowClickInvalidSlot;
    public static boolean windowClickInvalidType;

    // Packet protections; WindowConfirmation
    public static boolean windowConfirmationInvalidWindowId;
    public static boolean windowConfirmationNotAccepted;

    /**
     * Loads the configuration file and values.
     */
    public static void loadSettings() {
        loadConfigFile();
        loadConfigValues();
    }

    /**
     * Initializes the configuration file and loads defaults.
     */
    @SuppressWarnings("OverlyBroadCatchBlock")
    private static void loadConfigFile() {
        try {
            // First, attempt to load the default configuration as a stream to check if it exists in the plugin JAR
            @Cleanup InputStream defConfigStream = Vulture.getInstance().getResource(fileName);

            if (defConfigStream == null) {
                // Log a warning if the default configuration cannot be found within the JAR
                MessageUtil.log(Level.WARNING, "Could not find " + fileName + " in the plugin JAR.");
                return;
            }

            // Proceed to check if the config file exists in the plugin's data folder
            // and save the default config from the JAR if not
            File dataFolder = Vulture.getInstance().getDataFolder();
            file = new File(dataFolder, fileName);
            if (!file.exists()) {
                Vulture.getInstance().saveResource(fileName, false);
            }

            // Now that we've ensured the file exists (either it already did, or we've just created it),
            // we can safely load it into our CustomYamlConfiguration object
            config = CustomYamlConfiguration.loadConfiguration(file);
            @Cleanup @NotNull InputStreamReader reader = new InputStreamReader(defConfigStream, StandardCharsets.UTF_8);
            @NotNull CustomYamlConfiguration defConfig = CustomYamlConfiguration.loadConfiguration(reader);

            // Ensure defaults are applied
            config.setDefaults(defConfig);
            config.options().copyDefaults(true);
            saveConfig(); // Save the config with defaults applied
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Saves the configuration file.
     */
    public static void saveConfig() {
        try {
            config.save(file);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Loads configuration values into the relevant static fields.
     */
    private static void loadConfigValues() {
        // General settings
        prefix = config.getString("vulture.general.prefix", "&e[Vulture]");
        banMessage = config.getStringList("vulture.general.banMessage");
        resetViolations = config.getLong("vulture.general.resetViolations", 600);

        // Blocked payloads
        blockedPayloads = config.getStringList("vulture.blocked.payloads");

        // Blocked mods
        blockedMods = config.getStringList("vulture.blocked.mods");

        // Blocked commands
        blockedCommands = config.getStringList("vulture.blocked.commands");

        // PingSpoof B settings
        PingSpoofB.maxPing = config.getLong("vulture.checks.pingspoof.B.maxPing", 1000);
        PingSpoofB.maxAveragePing = config.getLong("vulture.checks.pingspoof.B.maxAveragePing", 500);
        PingSpoofB.maxPingDeviation = config.getLong("vulture.checks.pingspoof.B.maxPingDeviation", 500);

        // Check settings
        for (@NotNull Class<? extends Check> check : CheckManager.CHECK_CLASSES) {
            @NotNull String name = getCheckName(check);
            CheckInfo checkInfo = check.getAnnotation(CheckInfo.class);

            if (config.contains("vulture.checks." + name + ".enabled")) {
                Object enabled = config.get("vulture.checks." + name + ".enabled", true);
                changeAnnotationValue(checkInfo, "enabled", enabled);
            }

            if (config.contains("vulture.checks." + name + ".maxViolations")) {
                Object maxViolations = config.get("vulture.checks." + name + ".maxViolations");
                changeAnnotationValue(checkInfo, "maxViolations", maxViolations);
            }

            if (config.contains("vulture.checks." + name + ".banCommand")) {
                Object banCommand = config.get("vulture.checks." + name + ".banCommand");
                changeAnnotationValue(checkInfo, "banCommand", banCommand);
            }
        }

        // IP Whitelist settings
        ipWhitelistEnabled = config.getBoolean("vulture.ip-whitelist.enabled", false);

        // Load IP whitelist
        ipWhitelist = new HashMap<>();
        ConfigurationSection whitelistSection = config.getConfigurationSection("vulture.ip-whitelist.whitelist");
        if (whitelistSection != null) {
            for (@NotNull String uuidString : whitelistSection.getKeys(false)) {
                @NotNull UUID uuid = UUID.fromString(uuidString);
                List<String> ips = whitelistSection.getStringList(uuidString + ".ips");
                ipWhitelist.put(uuid, ips);
            }
        }

        // Exploit protections
        worldInvalidBlockBreak = config.getBoolean("vulture.protections.exploits.world.invalid-block-break", true);
        worldInvalidBlockPlace = config.getBoolean("vulture.protections.exploits.world.invalid-block-place", true);
        worldBlockNetherCeiling = config.getBoolean("vulture.protections.exploits.world.block-nether-ceiling", true);
        worldBlockCommandsWhileSleeping = config.getBoolean("vulture.protections.exploits.world.block-commands-while-sleeping", true);
        worldFixPearlPhasing = config.getBoolean("vulture.protections.exploits.world.fix-pearl-phasing", true);
        portalsBlockAffectedMobs = config.getBoolean("vulture.protections.exploits.portals.block-affected-mobs", true);
        pistonsBlockMovingEntities = config.getBoolean("vulture.protections.exploits.pistons.block-moving-entities", true);

        // Packet protections
        maxPacketsPerTick = config.getInt("vulture.protections.packets.max-packets-per-tick", 400);
        maxPacketsPerSecond = config.getInt("vulture.protections.packets.max-packets-per-second", 500);
        maxPacketsSmoothed = config.getInt("vulture.protections.packets.max-packets-smoothed", 600);
        maxTimeOverServer = config.getLong("vulture.protections.packets.max-time-over-server", 100);
        invalidPacketSentToServer = config.getBoolean("vulture.protections.packets.invalid-packet-sent-to-server", true);

        // Packet protections; Abilities
        abilitiesDuplicateFlying = config.getBoolean("vulture.protections.packets.abilities.duplicate-flying", true);
        abilitiesInvalidFlightAllowed = config.getBoolean("vulture.protections.packets.abilities.invalid-flight-allowed", true);
        abilitiesInvalidFlying = config.getBoolean("vulture.protections.packets.abilities.invalid-flying", true);
        abilitiesInvalidCreativeMode = config.getBoolean("vulture.protections.packets.abilities.invalid-creative-mode", true);
        abilitiesInvalidGodMode = config.getBoolean("vulture.protections.packets.abilities.invalid-god-mode", true);

        // Packet protections; Beacon
        beaconInvalidConditions = config.getBoolean("vulture.protections.packets.beacon.invalid-conditions", true);
        beaconInvalidData = config.getBoolean("vulture.protections.packets.beacon.invalid-data", true);
        beaconInvalidEffect = config.getBoolean("vulture.protections.packets.beacon.invalid-effect", true);
        beaconInvalidTier = config.getBoolean("vulture.protections.packets.beacon.invalid-tier", true);

        // Packet protections; BlockPlacement
        blockPlacementInvalidBlockPosition = config.getBoolean("vulture.protections.packets.block-placement.invalid-block-position", true);
        blockPlacementInvalidDistance = config.getBoolean("vulture.protections.packets.block-placement.invalid-distance", true);
        blockPlacementInvalidUpCursorBounds = config.getBoolean("vulture.protections.packets.block-placement.invalid-up-cursor-bounds", true);
        blockPlacementInvalidUpCursorPosition = config.getBoolean("vulture.protections.packets.block-placement.invalid-up-cursor-position", true);
        blockPlacementInvalidUpPitch = config.getBoolean("vulture.protections.packets.block-placement.invalid-up-pitch", true);
        blockPlacementInvalidDownCursorBounds = config.getBoolean("vulture.protections.packets.block-placement.invalid-down-cursor-bounds", true);
        blockPlacementInvalidDownCursorPosition = config.getBoolean("vulture.protections.packets.block-placement.invalid-down-cursor-position", true);
        blockPlacementInvalidDownPitch = config.getBoolean("vulture.protections.packets.block-placement.invalid-down-pitch", true);
        blockPlacementInvalidEastCursorBounds = config.getBoolean("vulture.protections.packets.block-placement.invalid-east-cursor-bounds", true);
        blockPlacementInvalidEastCursorPosition = config.getBoolean("vulture.protections.packets.block-placement.invalid-east-cursor-position", true);
        blockPlacementInvalidEastYaw = config.getBoolean("vulture.protections.packets.block-placement.invalid-east-yaw", true);
        blockPlacementInvalidWestCursorBounds = config.getBoolean("vulture.protections.packets.block-placement.invalid-west-cursor-bounds", true);
        blockPlacementInvalidWestCursorPosition = config.getBoolean("vulture.protections.packets.block-placement.invalid-west-cursor-position", true);
        blockPlacementInvalidWestYaw = config.getBoolean("vulture.protections.packets.block-placement.invalid-west-yaw", true);
        blockPlacementInvalidNorthCursorBounds = config.getBoolean("vulture.protections.packets.block-placement.invalid-north-cursor-bounds", true);
        blockPlacementInvalidNorthCursorPosition = config.getBoolean("vulture.protections.packets.block-placement.invalid-north-cursor-position", true);
        blockPlacementInvalidNorthYaw = config.getBoolean("vulture.protections.packets.block-placement.invalid-north-yaw", true);
        blockPlacementInvalidSouthCursorBounds = config.getBoolean("vulture.protections.packets.block-placement.invalid-south-cursor-bounds", true);
        blockPlacementInvalidSouthCursorPosition = config.getBoolean("vulture.protections.packets.block-placement.invalid-south-cursor-position", true);
        blockPlacementInvalidSouthYaw = config.getBoolean("vulture.protections.packets.block-placement.invalid-south-yaw", true);
        blockPlacementInvalidOtherCursorPosition = config.getBoolean("vulture.protections.packets.block-placement.invalid-other-cursor-position", true);
        blockPlacementInvalidOtherBlockPosition = config.getBoolean("vulture.protections.packets.block-placement.invalid-other-block-position", true);

        // Packet protections; BookEdit
        bookEditInvalidConditions = config.getBoolean("vulture.protections.packets.book-edit.invalid-conditions", true);
        bookEditInvalidData = config.getBoolean("vulture.protections.packets.book-edit.invalid-data", true);

        // Packet protections; BookOpen
        bookOpenInvalidConditions = config.getBoolean("vulture.protections.packets.book-open.invalid-conditions", true);

        // Packet protections; BookSign
        bookSignInvalidConditions = config.getBoolean("vulture.protections.packets.book-sign.invalid-conditions", true);
        bookSignInvalidData = config.getBoolean("vulture.protections.packets.book-sign.invalid-data", true);

        // Packet protections; ChatMessage
        chatMessageInvalidConditions = config.getBoolean("vulture.protections.packets.chat-message.invalid-conditions", true);
        chatMessageInvalidMessage = config.getBoolean("vulture.protections.packets.chat-message.invalid-message", true);

        // Packet protections; CloseWindow
        closeWindowInvalidConditions = config.getBoolean("vulture.protections.packets.close-window.invalid-conditions", true);

        // Packet protections; CommandBlock
        commandBlockInvalidConditions = config.getBoolean("vulture.protections.packets.command-block.invalid-conditions", true);

        // Packet protections; CreativeInventoryAction
        creativeInventoryActionInvalidConditions = config.getBoolean("vulture.protections.packets.creative-inventory-action.invalid-conditions", true);
        creativeInventoryActionInvalidSlot = config.getBoolean("vulture.protections.packets.creative-inventory-action.invalid-slot", true);

        // Packet protections; Digging
        diggingInvalidBlockType = config.getBoolean("vulture.protections.packets.digging.invalid-block-type", true);
        diggingInvalidBlockPosition = config.getBoolean("vulture.protections.packets.digging.invalid-block-position", true);
        diggingInvalidDistance = config.getBoolean("vulture.protections.packets.digging.invalid-distance", true);

        // Packet protections; EntityAction
        entityActionInvalidEntityID = config.getBoolean("vulture.protections.packets.entity-action.invalid-entity-id", true);
        entityActionInvalidJumpBoost = config.getBoolean("vulture.protections.packets.entity-action.invalid-jump-boost", true);

        // Packet protections; Flying
        flyingInvalidPitch = config.getBoolean("vulture.protections.packets.flying.invalid-pitch", true);
        flyingInvalidPositionData = config.getBoolean("vulture.protections.packets.flying.invalid-position-data", true);
        flyingInvalidRotationData = config.getBoolean("vulture.protections.packets.flying.invalid-rotation-data", true);
        flyingInvalidYData = config.getBoolean("vulture.protections.packets.flying.invalid-y-data", true);

        // Packet protections; HeldItemChange
        heldItemChangeInvalidConditions = config.getBoolean("vulture.protections.packets.held-item-change.invalid-conditions", true);
        heldItemChangeInvalidSlot = config.getBoolean("vulture.protections.packets.held-item-change.invalid-slot", true);
        heldItemChangeInvalidSlotChange = config.getBoolean("vulture.protections.packets.held-item-change.invalid-slot-change", true);

        // Packet protections; DropItem
        dropItemInvalidData = config.getBoolean("vulture.protections.packets.drop-item.invalid-data", true);

        // Packet protections; ItemName
        itemNameInvalidData = config.getBoolean("vulture.protections.packets.item-name.invalid-data", true);
        itemNameInvalidSize = config.getBoolean("vulture.protections.packets.item-name.invalid-size", true);

        // Packet protections; OpenHorseInventory
        openHorseInventoryInvalidConditions = config.getBoolean("vulture.protections.packets.open-horse-inventory.invalid-conditions", true);
        openHorseInventoryInvalidVehicle = config.getBoolean("vulture.protections.packets.open-horse-inventory.invalid-vehicle", true);
        openHorseInventoryInvalidTamed = config.getBoolean("vulture.protections.packets.open-horse-inventory.invalid-tamed", true);

        // Packet protections; PluginMessage
        pluginMessageInvalidChannelName = config.getBoolean("vulture.protections.packets.plugin-message.invalid-channel-name", true);
        pluginMessageInvalidSize = config.getBoolean("vulture.protections.packets.plugin-message.invalid-size", true);

        // Packet protections; ReleaseUseItem
        releaseUseItemInvalidData = config.getBoolean("vulture.protections.packets.release-use-item.invalid-data", true);

        // Packet protections; Respawn
        respawnInvalidConditions = config.getBoolean("vulture.protections.packets.respawn.invalid-conditions", true);

        // Packet protections; RidingJump
        ridingJumpInvalidConditions = config.getBoolean("vulture.protections.packets.riding-jump.invalid-conditions", true);
        ridingJumpInvalidJumpBoost = config.getBoolean("vulture.protections.packets.riding-jump.invalid-jump-boost", true);

        // Packet protections; Settings
        settingsInvalidLocale = config.getBoolean("vulture.protections.packets.settings.invalid-locale", true);
        settingsInvalidViewDistance = config.getBoolean("vulture.protections.packets.settings.invalid-view-distance", true);

        // Packet protections; Spectate
        spectateInvalidConditions = config.getBoolean("vulture.protections.packets.spectate.invalid-conditions", true);
        spectateInvalidTarget = config.getBoolean("vulture.protections.packets.spectate.invalid-target", true);

        // Packet protections; StartSneaking
        startSneakingInvalidConditions = config.getBoolean("vulture.protections.packets.start-sneaking.invalid-conditions", true);

        // Packet protections; SteerVehicle
        steerVehicleInvalidConditions = config.getBoolean("vulture.protections.packets.steer-vehicle.invalid-conditions", true);
        steerVehicleInvalidDismountValue = config.getBoolean("vulture.protections.packets.steer-vehicle.invalid-dismount-value", true);
        steerVehicleInvalidNonDismountValue = config.getBoolean("vulture.protections.packets.steer-vehicle.invalid-non-dismount-value", true);
        steerVehicleInvalidJump = config.getBoolean("vulture.protections.packets.steer-vehicle.invalid-jump", true);
        steerVehicleInvalidValue = config.getBoolean("vulture.protections.packets.steer-vehicle.invalid-value", true);

        // Packet protections; StopSleeping
        stopSleepingInvalidConditions = config.getBoolean("vulture.protections.packets.stop-sleeping.invalid-conditions", true);

        // Packet protections; TabComplete
        tabCompleteInvalidMessage = config.getBoolean("vulture.protections.packets.tab-complete.invalid-message", true);

        // Packet protections; TradeSelect
        tradeSelectInvalidData = config.getBoolean("vulture.protections.packets.trade-select.invalid-data", true);

        // Packet protections; UpdateSign
        updateSignInvalidData = config.getBoolean("vulture.protections.packets.update-sign.invalid-data", true);

        // Packet protections; InteractEntity
        interactEntityInvalidConditions = config.getBoolean("vulture.protections.packets.interact-entity.invalid-conditions", true);
        interactEntityInvalidDistance = config.getBoolean("vulture.protections.packets.interact-entity.invalid-distance", true);

        // Packet protections; WindowClick
        windowClickInvalidCloneButton = config.getBoolean("vulture.protections.packets.window-click.invalid-clone-button", true);
        windowClickInvalidPickupAllButton = config.getBoolean("vulture.protections.packets.window-click.invalid-pickup-all-button", true);
        windowClickInvalidPickupButton = config.getBoolean("vulture.protections.packets.window-click.invalid-pickup-button", true);
        windowClickInvalidQuickCraftButton = config.getBoolean("vulture.protections.packets.window-click.invalid-quick-craft-button", true);
        windowClickInvalidQuickMoveButton = config.getBoolean("vulture.protections.packets.window-click.invalid-quick-move-button", true);
        windowClickInvalidSwapButton = config.getBoolean("vulture.protections.packets.window-click.invalid-swap-button", true);
        windowClickInvalidThrowButton = config.getBoolean("vulture.protections.packets.window-click.invalid-throw-button", true);
        windowClickInvalidSlot = config.getBoolean("vulture.protections.packets.window-click.invalid-slot", true);
        windowClickInvalidType = config.getBoolean("vulture.protections.packets.window-click.invalid-type", true);

        // Packet protections; WindowConfirmation
        windowConfirmationInvalidWindowId = config.getBoolean("vulture.protections.packets.window-confirmation.invalid-window-id", true);
        windowConfirmationNotAccepted = config.getBoolean("vulture.protections.packets.window-confirmation.not-accepted", true);
    }

    /**
     * Gets the name of a check.
     *
     * @param check The check.
     * @return The name of the check.
     */
    private static @NotNull String getCheckName(@NotNull Class<? extends Check> check) {
        CheckInfo checkInfo = check.getAnnotation(CheckInfo.class);
        @NotNull String name = checkInfo.name().replace(" ", "")
                .replace("(", ".")
                .replace(")", "").toLowerCase(Locale.ROOT).trim();

        if (name.contains(".")) {
            char @NotNull [] charArray = name.toCharArray();
            int lastIndex = charArray.length - 1;
            charArray[lastIndex] = Character.toUpperCase(charArray[lastIndex]);
            name = new String(charArray);
        }
        return name;
    }

    /**
     * Changes the value of an annotation.
     *
     * @param annotation The annotation.
     * @param key        The key.
     * @param newValue   The new value.
     */
    @SuppressWarnings("unchecked")
    private static void changeAnnotationValue(@NotNull Annotation annotation,
                                              String key, @NotNull Object newValue) {
        Object handler = Proxy.getInvocationHandler(annotation);
        Field field;

        try {
            field = handler.getClass().getDeclaredField("memberValues");
        } catch (NoSuchFieldException | SecurityException ex) {
            throw new IllegalStateException(ex);
        }

        field.setAccessible(true);

        Map<String, Object> memberValues;

        try {
            memberValues = (Map<String, Object>) field.get(handler);
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            throw new IllegalStateException(ex);
        }

        Object oldValue = memberValues.get(key);

        if (oldValue == null || oldValue.getClass() != newValue.getClass()) {
            throw new IllegalArgumentException();
        }

        memberValues.put(key, newValue);
    }
}
