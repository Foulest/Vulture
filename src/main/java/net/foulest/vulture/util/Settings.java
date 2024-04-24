package net.foulest.vulture.util;

import lombok.Cleanup;
import lombok.Getter;
import lombok.Setter;
import net.foulest.vulture.Vulture;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckManager;
import net.foulest.vulture.check.type.pingspoof.PingSpoofB;
import net.foulest.vulture.check.type.reach.ReachA;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Utility class for settings.
 *
 * @author Foulest
 * @project Vulture
 */
@Getter
@Setter
public class Settings {

    // File settings
    public static File file;
    public static FileConfiguration config;
    public static String fileName = "config.yml";

    // General settings
    public static String prefix;
    public static List<String> banMessage;
    public static long resetViolations;

    // Blocked payloads
    public static List<String> blockedPayloads;

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
    public static boolean itemsInvalidStackSize;

    // Packet protections
    public static int maxPacketsPerTick;
    public static int maxPacketsPerSecond;
    public static int maxPacketsSmoothed;
    public static boolean abilitiesDuplicateFlying;
    public static boolean abilitiesInvalidFlightAllowed;
    public static boolean abilitiesInvalidFlySpeed;
    public static boolean abilitiesInvalidFlying;
    public static boolean abilitiesInvalidInstantBuild;
    public static boolean abilitiesInvalidInvulnerable;
    public static boolean abilitiesInvalidWalkSpeed;
    public static boolean armAnimationInvalidConditions;
    public static boolean attackEntityInvalidConditions;
    public static boolean beaconInvalidConditions;
    public static boolean beaconInvalidData;
    public static boolean beaconInvalidEffect;
    public static boolean beaconInvalidTier;
    public static boolean blockDigInvalidDistance;
    public static boolean blockPlaceInvalidConditions;
    public static boolean blockPlaceInvalidCursorPosition;
    public static boolean blockPlaceInvalidDistance;
    public static boolean blockPlaceInvalidItem;
    public static boolean blockPlaceInvalidOtherBlockPosition;
    public static boolean blockPlaceInvalidOtherCursorPosition;
    public static boolean blockPlaceInvalidUpBlockPosition;
    public static boolean bookEditInvalidConditions;
    public static boolean bookEditInvalidData;
    public static boolean bookOpenInvalidConditions;
    public static boolean bookSignInvalidConditions;
    public static boolean bookSignInvalidData;
    public static boolean chatInvalidConditions;
    public static boolean chatInvalidMessage;
    public static boolean closeWindowClosedInventory;
    public static boolean closeWindowInvalidConditions;
    public static boolean commandBlockInvalidConditions;
    public static boolean customPayloadInvalidSize;
    public static boolean enchantItemInvalidWindowId;
    public static boolean entityActionInvalidJumpBoost;
    public static boolean flyingInvalidPitch;
    public static boolean flyingInvalidPositionData;
    public static boolean flyingInvalidRotationData;
    public static boolean flyingInvalidYData;
    public static boolean heldItemSlotInvalidConditions;
    public static boolean heldItemSlotInvalidSlot;
    public static boolean heldItemSlotInvalidSlotChange;
    public static boolean itemDropInvalidData;
    public static boolean itemNameInvalidData;
    public static boolean itemNameInvalidSize;
    public static boolean releaseUseItemInvalidConditions;
    public static boolean releaseUseItemInvalidData;
    public static boolean respawnInvalidConditions;
    public static boolean ridingJumpInvalidConditions;
    public static boolean ridingJumpInvalidJumpBoost;
    public static boolean setCreativeSlotInvalidConditions;
    public static boolean setCreativeSlotInvalidSlot;
    public static boolean settingsInvalidLocale;
    public static boolean settingsInvalidViewDistance;
    public static boolean spectateInvalidConditions;
    public static boolean startSneakingInvalidConditions;
    public static boolean startSprintingInvalidConditions;
    public static boolean steerVehicleInvalidConditions;
    public static boolean steerVehicleInvalidDismountValue;
    public static boolean steerVehicleInvalidNonDismountValue;
    public static boolean steerVehicleInvalidValue;
    public static boolean stopSleepingInvalidConditions;
    public static boolean stopSneakingInvalidConditions;
    public static boolean tabCompleteInvalidMessage;
    public static boolean tradeSelectInvalidData;
    public static boolean transactionInvalidWindowId;
    public static boolean transactionNotAccepted;
    public static boolean updateSignInvalidData;
    public static boolean useEntityInvalidConditions;
    public static boolean useEntityInvalidDistance;
    public static boolean windowClickInvalidCloneButton;
    public static boolean windowClickInvalidConditions;
    public static boolean windowClickInvalidPickupAllButton;
    public static boolean windowClickInvalidPickupButton;
    public static boolean windowClickInvalidQuickCraftButton;
    public static boolean windowClickInvalidQuickMoveButton;
    public static boolean windowClickInvalidSlot;
    public static boolean windowClickInvalidSwapButton;
    public static boolean windowClickInvalidThrowButton;

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
            @Cleanup InputStreamReader reader = new InputStreamReader(defConfigStream, StandardCharsets.UTF_8);
            CustomYamlConfiguration defConfig = CustomYamlConfiguration.loadConfiguration(reader);

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
    public static void loadConfigValues() {
        // General settings
        prefix = config.getString("vulture.general.prefix");
        banMessage = config.getStringList("vulture.general.banMessage");
        resetViolations = config.getLong("vulture.general.resetViolations");

        // Blocked payloads
        blockedPayloads = config.getStringList("vulture.blocked.payloads");

        // Blocked commands
        blockedCommands = config.getStringList("vulture.blocked.commands");

        // PingSpoof B settings
        PingSpoofB.maxPing = config.getLong("vulture.checks.pingspoof.B.maxPing");
        PingSpoofB.maxAveragePing = config.getLong("vulture.checks.pingspoof.B.maxAveragePing");
        PingSpoofB.maxPingDeviation = config.getLong("vulture.checks.pingspoof.B.maxPingDeviation");

        // Reach A settings
        ReachA.maxDistance = config.getDouble("vulture.checks.reach.A.maxDistance");
        ReachA.cancelHits = config.getBoolean("vulture.checks.reach.A.cancelHits");

        // Check settings
        for (Class<? extends Check> check : CheckManager.CHECK_CLASSES) {
            String name = getCheckName(check);
            CheckInfo checkInfo = check.getAnnotation(CheckInfo.class);

            if (config.contains("vulture.checks." + name + ".enabled")) {
                changeAnnotationValue(checkInfo, "enabled",
                        config.get("vulture.checks." + name + ".enabled"));
            }

            if (config.contains("vulture.checks." + name + ".maxViolations")) {
                changeAnnotationValue(checkInfo, "maxViolations",
                        config.get("vulture.checks." + name + ".maxViolations"));
            }

            if (config.contains("vulture.checks." + name + ".banCommand")) {
                changeAnnotationValue(checkInfo, "banCommand",
                        config.get("vulture.checks." + name + ".banCommand"));
            }
        }

        // IP Whitelist settings
        ipWhitelistEnabled = config.getBoolean("vulture.ip-whitelist.enabled");

        // Load IP whitelist
        ipWhitelist = new HashMap<>();
        ConfigurationSection whitelistSection = config.getConfigurationSection("vulture.ip-whitelist.whitelist");
        if (whitelistSection != null) {
            for (String uuidString : whitelistSection.getKeys(false)) {
                UUID uuid = UUID.fromString(uuidString);
                List<String> ips = whitelistSection.getStringList(uuidString + ".ips");
                ipWhitelist.put(uuid, ips);
            }
        }

        // Exploit protections
        worldInvalidBlockBreak = config.getBoolean("vulture.protections.exploits.world.invalid-block-break");
        worldInvalidBlockPlace = config.getBoolean("vulture.protections.exploits.world.invalid-block-place");
        worldBlockNetherCeiling = config.getBoolean("vulture.protections.exploits.world.block-nether-ceiling");
        worldBlockCommandsWhileSleeping = config.getBoolean("vulture.protections.exploits.world.block-commands-while-sleeping");
        worldFixPearlPhasing = config.getBoolean("vulture.protections.exploits.world.fix-pearl-phasing");
        portalsBlockAffectedMobs = config.getBoolean("vulture.protections.exploits.portals.block-affected-mobs");
        pistonsBlockMovingEntities = config.getBoolean("vulture.protections.exploits.pistons.block-moving-entities");
        itemsInvalidStackSize = config.getBoolean("vulture.protections.exploits.items.invalid-stack-size");

        // Packet protections; max packets per tick
        maxPacketsPerTick = config.getInt("vulture.protections.packets.max-packets-per-tick");
        maxPacketsPerSecond = config.getInt("vulture.protections.packets.max-packets-per-second");
        maxPacketsSmoothed = config.getInt("vulture.protections.packets.max-packets-smoothed");

        // Packet protections; Abilities
        abilitiesDuplicateFlying = config.getBoolean("vulture.protections.packets.abilities.duplicate-flying");
        abilitiesInvalidFlightAllowed = config.getBoolean("vulture.protections.packets.abilities.invalid-flight-allowed");
        abilitiesInvalidFlySpeed = config.getBoolean("vulture.protections.packets.abilities.invalid-fly-speed");
        abilitiesInvalidFlying = config.getBoolean("vulture.protections.packets.abilities.invalid-flying");
        abilitiesInvalidInstantBuild = config.getBoolean("vulture.protections.packets.abilities.invalid-instant-build");
        abilitiesInvalidInvulnerable = config.getBoolean("vulture.protections.packets.abilities.invalid-invulnerable");
        abilitiesInvalidWalkSpeed = config.getBoolean("vulture.protections.packets.abilities.invalid-walk-speed");

        // Packet protections; ArmAnimation
        armAnimationInvalidConditions = config.getBoolean("vulture.protections.packets.arm-animation.invalid-conditions");

        // Packet protections; AttackEntity
        attackEntityInvalidConditions = config.getBoolean("vulture.protections.packets.attack-entity.invalid-conditions");

        // Packet protections; Beacon
        beaconInvalidConditions = config.getBoolean("vulture.protections.packets.beacon.invalid-conditions");
        beaconInvalidData = config.getBoolean("vulture.protections.packets.beacon.invalid-data");
        beaconInvalidEffect = config.getBoolean("vulture.protections.packets.beacon.invalid-effect");
        beaconInvalidTier = config.getBoolean("vulture.protections.packets.beacon.invalid-tier");

        // Packet protections; BlockDig
        blockDigInvalidDistance = config.getBoolean("vulture.protections.packets.block-dig.invalid-distance");

        // Packet protections; BlockPlace
        blockPlaceInvalidConditions = config.getBoolean("vulture.protections.packets.block-place.invalid-conditions");
        blockPlaceInvalidCursorPosition = config.getBoolean("vulture.protections.packets.block-place.invalid-cursor-position");
        blockPlaceInvalidDistance = config.getBoolean("vulture.protections.packets.block-place.invalid-distance");
        blockPlaceInvalidItem = config.getBoolean("vulture.protections.packets.block-place.invalid-item");
        blockPlaceInvalidOtherBlockPosition = config.getBoolean("vulture.protections.packets.block-place.invalid-other-block-position");
        blockPlaceInvalidOtherCursorPosition = config.getBoolean("vulture.protections.packets.block-place.invalid-other-cursor-position");
        blockPlaceInvalidUpBlockPosition = config.getBoolean("vulture.protections.packets.block-place.invalid-up-block-position");

        // Packet protections; BookEdit
        bookEditInvalidConditions = config.getBoolean("vulture.protections.packets.book-edit.invalid-conditions");
        bookEditInvalidData = config.getBoolean("vulture.protections.packets.book-edit.invalid-data");

        // Packet protections; BookOpen
        bookOpenInvalidConditions = config.getBoolean("vulture.protections.packets.book-open.invalid-conditions");

        // Packet protections; BookSign
        bookSignInvalidConditions = config.getBoolean("vulture.protections.packets.book-sign.invalid-conditions");
        bookSignInvalidData = config.getBoolean("vulture.protections.packets.book-sign.invalid-data");

        // Packet protections; Chat
        chatInvalidConditions = config.getBoolean("vulture.protections.packets.chat.invalid-conditions");
        chatInvalidMessage = config.getBoolean("vulture.protections.packets.chat.invalid-message");

        // Packet protections; CloseWindow
        closeWindowClosedInventory = config.getBoolean("vulture.protections.packets.close-window.closed-inventory");
        closeWindowInvalidConditions = config.getBoolean("vulture.protections.packets.close-window.invalid-conditions");

        // Packet protections; CommandBlock
        commandBlockInvalidConditions = config.getBoolean("vulture.protections.packets.command-block.invalid-conditions");

        // Packet protections; CustomPayload
        customPayloadInvalidSize = config.getBoolean("vulture.protections.packets.custom-payload.invalid-size");

        // Packet protections; EnchantItem
        enchantItemInvalidWindowId = config.getBoolean("vulture.protections.packets.enchant-item.invalid-window-id");

        // Packet protections; EntityAction
        entityActionInvalidJumpBoost = config.getBoolean("vulture.protections.packets.entity-action.invalid-jump-boost");

        // Packet protections; Flying
        flyingInvalidPitch = config.getBoolean("vulture.protections.packets.flying.invalid-pitch");
        flyingInvalidPositionData = config.getBoolean("vulture.protections.packets.flying.invalid-position-data");
        flyingInvalidRotationData = config.getBoolean("vulture.protections.packets.flying.invalid-rotation-data");
        flyingInvalidYData = config.getBoolean("vulture.protections.packets.flying.invalid-y-data");

        // Packet protections; HeldItemSlot
        heldItemSlotInvalidConditions = config.getBoolean("vulture.protections.packets.held-item-slot.invalid-conditions");
        heldItemSlotInvalidSlot = config.getBoolean("vulture.protections.packets.held-item-slot.invalid-slot");
        heldItemSlotInvalidSlotChange = config.getBoolean("vulture.protections.packets.held-item-slot.invalid-slot-change");

        // Packet protections; ItemDrop
        itemDropInvalidData = config.getBoolean("vulture.protections.packets.item-drop.invalid-data");

        // Packet protections; ItemName
        itemNameInvalidData = config.getBoolean("vulture.protections.packets.item-name.invalid-data");
        itemNameInvalidSize = config.getBoolean("vulture.protections.packets.item-name.invalid-size");

        // Packet protections; ReleaseUseItem
        releaseUseItemInvalidConditions = config.getBoolean("vulture.protections.packets.release-use-item.invalid-conditions");
        releaseUseItemInvalidData = config.getBoolean("vulture.protections.packets.release-use-item.invalid-data");

        // Packet protections; Respawn
        respawnInvalidConditions = config.getBoolean("vulture.protections.packets.respawn.invalid-conditions");

        // Packet protections; RidingJump
        ridingJumpInvalidConditions = config.getBoolean("vulture.protections.packets.riding-jump.invalid-conditions");
        ridingJumpInvalidJumpBoost = config.getBoolean("vulture.protections.packets.riding-jump.invalid-jump-boost");

        // Packet protections; SetCreativeSlot
        setCreativeSlotInvalidConditions = config.getBoolean("vulture.protections.packets.set-creative-slot.invalid-conditions");
        setCreativeSlotInvalidSlot = config.getBoolean("vulture.protections.packets.set-creative-slot.invalid-slot");

        // Packet protections; Settings
        settingsInvalidLocale = config.getBoolean("vulture.protections.packets.settings.invalid-locale");
        settingsInvalidViewDistance = config.getBoolean("vulture.protections.packets.settings.invalid-view-distance");

        // Packet protections; Spectate
        spectateInvalidConditions = config.getBoolean("vulture.protections.packets.spectate.invalid-conditions");

        // Packet protections; StartSneaking
        startSneakingInvalidConditions = config.getBoolean("vulture.protections.packets.start-sneaking.invalid-conditions");

        // Packet protections; StartSprinting
        startSprintingInvalidConditions = config.getBoolean("vulture.protections.packets.start-sprinting.invalid-conditions");

        // Packet protections; SteerVehicle
        steerVehicleInvalidConditions = config.getBoolean("vulture.protections.packets.steer-vehicle.invalid-conditions");
        steerVehicleInvalidDismountValue = config.getBoolean("vulture.protections.packets.steer-vehicle.invalid-dismount-value");
        steerVehicleInvalidNonDismountValue = config.getBoolean("vulture.protections.packets.steer-vehicle.invalid-non-dismount-value");
        steerVehicleInvalidValue = config.getBoolean("vulture.protections.packets.steer-vehicle.invalid-value");

        // Packet protections; StopSleeping
        stopSleepingInvalidConditions = config.getBoolean("vulture.protections.packets.stop-sleeping.invalid-conditions");

        // Packet protections; StopSneaking
        stopSneakingInvalidConditions = config.getBoolean("vulture.protections.packets.stop-sneaking.invalid-conditions");

        // Packet protections; TabComplete
        tabCompleteInvalidMessage = config.getBoolean("vulture.protections.packets.tab-complete.invalid-message");

        // Packet protections; TradeSelect
        tradeSelectInvalidData = config.getBoolean("vulture.protections.packets.trade-select.invalid-data");

        // Packet protections; Transaction
        transactionInvalidWindowId = config.getBoolean("vulture.protections.packets.transaction.invalid-window-id");
        transactionNotAccepted = config.getBoolean("vulture.protections.packets.transaction.not-accepted");

        // Packet protections; UpdateSign
        updateSignInvalidData = config.getBoolean("vulture.protections.packets.update-sign.invalid-data");

        // Packet protections; UseEntity
        useEntityInvalidConditions = config.getBoolean("vulture.protections.packets.use-entity.invalid-conditions");
        useEntityInvalidDistance = config.getBoolean("vulture.protections.packets.use-entity.invalid-distance");

        // Packet protections; WindowClick
        windowClickInvalidCloneButton = config.getBoolean("vulture.protections.packets.window-click.invalid-clone-button");
        windowClickInvalidConditions = config.getBoolean("vulture.protections.packets.window-click.invalid-conditions");
        windowClickInvalidPickupAllButton = config.getBoolean("vulture.protections.packets.window-click.invalid-pickup-all-button");
        windowClickInvalidPickupButton = config.getBoolean("vulture.protections.packets.window-click.invalid-pickup-button");
        windowClickInvalidQuickCraftButton = config.getBoolean("vulture.protections.packets.window-click.invalid-quick-craft-button");
        windowClickInvalidQuickMoveButton = config.getBoolean("vulture.protections.packets.window-click.invalid-quick-move-button");
        windowClickInvalidSlot = config.getBoolean("vulture.protections.packets.window-click.invalid-slot");
        windowClickInvalidSwapButton = config.getBoolean("vulture.protections.packets.window-click.invalid-swap-button");
        windowClickInvalidThrowButton = config.getBoolean("vulture.protections.packets.window-click.invalid-throw-button");
    }

    /**
     * Gets the name of a check.
     *
     * @param check The check.
     * @return The name of the check.
     */
    private static @NotNull String getCheckName(@NotNull Class<? extends Check> check) {
        CheckInfo checkInfo = check.getAnnotation(CheckInfo.class);
        String name = checkInfo.name().replace(" ", "")
                .replace("(", ".")
                .replace(")", "").toLowerCase().trim();

        if (name.contains(".")) {
            char[] charArray = name.toCharArray();
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
    public static void changeAnnotationValue(Annotation annotation,
                                             String key, Object newValue) {
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
