package net.foulest.vulture.util;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.foulest.vulture.Vulture;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckManager;
import net.foulest.vulture.check.type.pingspoof.PingSpoofB;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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

    public static File file;
    public static FileConfiguration config;

    // General settings
    public static String prefix = "&e[Vulture]";
    public static List<String> banMessage = Collections.singletonList("&c%player% has been removed from the network.");
    public static long resetViolations = 600;

    // Blacklisted payloads
    public static List<String> blacklistedPayloads = Collections.singletonList("GalactiCraft");

    // Blacklisted commands
    public static List<String> blacklistedCommands = Arrays.asList(
            // WorldEdit
            "/calc", "/eval", "/solve",

            // Holographic Displays
            "/h.* readtext",

            // PermissionsEx
            "/pe.*x promote", "/pe.*x demote", "/promote", "/demote",

            // Multiverse
            "/m.*v.* \\^", "/m.*v.*help <", "/\\$"
    );

    // Packet protections; max packets per tick
    public static int maxPacketsPerTick = 230;

    // Packet protections; Abilities
    public static boolean abilitiesDuplicateFlying = true;
    public static boolean abilitiesInvalidFlightAllowed = true;
    public static boolean abilitiesInvalidFlySpeed = true;
    public static boolean abilitiesInvalidFlying = true;
    public static boolean abilitiesInvalidInstantBuild = true;
    public static boolean abilitiesInvalidInvulnerable = true;
    public static boolean abilitiesInvalidWalkSpeed = true;

    // Packet protections; ArmAnimation
    public static boolean armAnimationInvalidConditions = true;

    // Packet protections; AttackEntity
    public static boolean attackEntityInvalidConditions = true;

    // Packet protections; Beacon
    public static boolean beaconInvalidConditions = true;
    public static boolean beaconInvalidData = true;
    public static boolean beaconInvalidEffect = true;
    public static boolean beaconInvalidTier = true;

    // Packet protections; BlockDig
    public static boolean blockDigInvalidDistance = true;

    // Packet protections; BlockPlace
    public static boolean blockPlaceInvalidConditions = true;
    public static boolean blockPlaceInvalidCursorPosition = true;
    public static boolean blockPlaceInvalidDistance = true;
    public static boolean blockPlaceInvalidItem = true;
    public static boolean blockPlaceInvalidOtherBlockPosition = true;
    public static boolean blockPlaceInvalidOtherCursorPosition = true;
    public static boolean blockPlaceInvalidUpBlockPosition = true;

    // Packet protections; BookEdit
    public static boolean bookEditInvalidConditions = true;
    public static boolean bookEditInvalidData = true;

    // Packet protections; BookOpen
    public static boolean bookOpenInvalidConditions = true;

    // Packet protections; BookSign
    public static boolean bookSignInvalidConditions = true;
    public static boolean bookSignInvalidData = true;

    // Packet protections; Chat
    public static boolean chatInvalidConditions = true;
    public static boolean chatInvalidMessage = true;

    // Packet protections; CloseWindow
    public static boolean closeWindowClosedInventory = true;
    public static boolean closeWindowInvalidConditions = true;

    // Packet protections; CommandBlock
    public static boolean commandBlockInvalidConditions = true;

    // Packet protections; CustomPayload
    public static boolean customPayloadInvalidSize = true;

    // Packet protections; EnchantItem
    public static boolean enchantItemInvalidWindowId = true;

    // Packet protections; EntityAction
    public static boolean entityActionInvalidJumpBoost = true;

    // Packet protections; Flying
    public static boolean flyingInvalidPitch = true;
    public static boolean flyingInvalidPositionData = true;
    public static boolean flyingInvalidRotationData = true;
    public static boolean flyingInvalidYData = true;

    // Packet protections; HeldItemSlot
    public static boolean heldItemSlotInvalidConditions = true;
    public static boolean heldItemSlotInvalidSlot = true;
    public static boolean heldItemSlotInvalidSlotChange = true;

    // Packet protections; ItemDrop
    public static boolean itemDropInvalidData = true;

    // Packet protections; ItemName
    public static boolean itemNameInvalidData = true;
    public static boolean itemNameInvalidSize = true;

    // Packet protections; ReleaseUseItem
    public static boolean releaseUseItemInvalidConditions = true;
    public static boolean releaseUseItemInvalidData = true;

    // Packet protections; Respawn
    public static boolean respawnInvalidConditions = true;

    // Packet protections; RidingJump
    public static boolean ridingJumpInvalidConditions = true;
    public static boolean ridingJumpInvalidJumpBoost = true;

    // Packet protections; SetCreativeSlot
    public static boolean setCreativeSlotInvalidConditions = true;
    public static boolean setCreativeSlotInvalidSlot = true;

    // Packet protections; Settings
    public static boolean settingsInvalidLocale = true;
    public static boolean settingsInvalidViewDistance = true;

    // Packet protections; Spectate
    public static boolean spectateInvalidConditions = true;

    // Packet protections; StartSneaking
    public static boolean startSneakingInvalidConditions = true;

    // Packet protections; StartSprinting
    public static boolean startSprintingInvalidConditions = true;

    // Packet protections; SteerVehicle
    public static boolean steerVehicleInvalidConditions = true;
    public static boolean steerVehicleInvalidDismountValue = true;
    public static boolean steerVehicleInvalidNonDismountValue = true;
    public static boolean steerVehicleInvalidValue = true;

    // Packet protections; StopSleeping
    public static boolean stopSleepingInvalidConditions = true;

    // Packet protections; StopSneaking
    public static boolean stopSneakingInvalidConditions = true;

    // Packet protections; TabComplete
    public static boolean tabCompleteInvalidMessage = true;

    // Packet protections; TradeSelect
    public static boolean tradeSelectInvalidData = true;

    // Packet protections; Transaction
    public static boolean transactionInvalidWindowId = true;
    public static boolean transactionNotAccepted = true;

    // Packet protections; UpdateSign
    public static boolean updateSignInvalidData = true;

    // Packet protections; UseEntity
    public static boolean useEntityInvalidConditions = true;
    public static boolean useEntityInvalidDistance = true;

    // Packet protections; WindowClick
    public static boolean windowClickInvalidCloneButton = true;
    public static boolean windowClickInvalidConditions = true;
    public static boolean windowClickInvalidPickupAllButton = true;
    public static boolean windowClickInvalidPickupButton = true;
    public static boolean windowClickInvalidQuickCraftButton = true;
    public static boolean windowClickInvalidQuickMoveButton = true;
    public static boolean windowClickInvalidSlot = true;
    public static boolean windowClickInvalidSwapButton = true;
    public static boolean windowClickInvalidThrowButton = true;

    /**
     * Initialize and set up default configuration values.
     */
    public static void setupSettings() {
        initConfig();
        setDefaultConfigValues();
        saveConfig();
    }

    /**
     * Loads configuration values into the relevant static fields.
     */
    public static void loadSettings() {
        if (!file.exists()) {
            setupSettings();
        }

        config = YamlConfiguration.loadConfiguration(file);

        // General settings
        prefix = config.getString("vulture.prefix");
        banMessage = config.getStringList("vulture.banMessage");
        resetViolations = config.getLong("vulture.resetViolations");

        // Blacklisted payloads
        blacklistedPayloads = config.getStringList("blacklisted.payloads");

        // Blacklisted commands
        blacklistedCommands = config.getStringList("blacklisted.commands");

        // PingSpoofB settings
        PingSpoofB.maxPing = config.getLong("checks.pingspoof.B.maxPing");
        PingSpoofB.maxAveragePing = config.getLong("checks.pingspoof.B.maxAveragePing");
        PingSpoofB.maxPingDeviation = config.getLong("checks.pingspoof.B.maxPingDeviation");

        // Check settings
        for (Class<? extends Check> check : CheckManager.CHECK_CLASSES) {
            String name = getCheckName(check);
            CheckInfo checkInfo = check.getAnnotation(CheckInfo.class);

            if (config.contains("checks." + name + ".enabled")) {
                changeAnnotationValue(checkInfo, "enabled", config.get("checks." + name + ".enabled"));
            }

            if (config.contains("checks." + name + ".maxViolations")) {
                changeAnnotationValue(checkInfo, "maxViolations", config.get("checks." + name + ".maxViolations"));
            }

            if (config.contains("checks." + name + ".banCommand")) {
                changeAnnotationValue(checkInfo, "banCommand", config.get("checks." + name + ".banCommand"));
            }
        }

        // Packet protections; max packets per tick
        maxPacketsPerTick = config.getInt("protections.packets.max-packets-per-tick");

        // Packet protections; Abilities
        abilitiesDuplicateFlying = config.getBoolean("protections.packets.abilities.duplicate-flying");
        abilitiesInvalidFlightAllowed = config.getBoolean("protections.packets.abilities.invalid-flight-allowed");
        abilitiesInvalidFlySpeed = config.getBoolean("protections.packets.abilities.invalid-fly-speed");
        abilitiesInvalidFlying = config.getBoolean("protections.packets.abilities.invalid-flying");
        abilitiesInvalidInstantBuild = config.getBoolean("protections.packets.abilities.invalid-instant-build");
        abilitiesInvalidInvulnerable = config.getBoolean("protections.packets.abilities.invalid-invulnerable");
        abilitiesInvalidWalkSpeed = config.getBoolean("protections.packets.abilities.invalid-walk-speed");

        // Packet protections; ArmAnimation
        armAnimationInvalidConditions = config.getBoolean("protections.packets.arm-animation.invalid-conditions");

        // Packet protections; AttackEntity
        attackEntityInvalidConditions = config.getBoolean("protections.packets.attack-entity.invalid-conditions");

        // Packet protections; Beacon
        beaconInvalidConditions = config.getBoolean("protections.packets.beacon.invalid-conditions");
        beaconInvalidData = config.getBoolean("protections.packets.beacon.invalid-data");
        beaconInvalidEffect = config.getBoolean("protections.packets.beacon.invalid-effect");
        beaconInvalidTier = config.getBoolean("protections.packets.beacon.invalid-tier");

        // Packet protections; BlockDig
        blockDigInvalidDistance = config.getBoolean("protections.packets.block-dig.invalid-distance");

        // Packet protections; BlockPlace
        blockPlaceInvalidConditions = config.getBoolean("protections.packets.block-place.invalid-conditions");
        blockPlaceInvalidCursorPosition = config.getBoolean("protections.packets.block-place.invalid-cursor-position");
        blockPlaceInvalidDistance = config.getBoolean("protections.packets.block-place.invalid-distance");
        blockPlaceInvalidItem = config.getBoolean("protections.packets.block-place.invalid-item");
        blockPlaceInvalidOtherBlockPosition = config.getBoolean("protections.packets.block-place.invalid-other-block-position");
        blockPlaceInvalidOtherCursorPosition = config.getBoolean("protections.packets.block-place.invalid-other-cursor-position");
        blockPlaceInvalidUpBlockPosition = config.getBoolean("protections.packets.block-place.invalid-up-block-position");

        // Packet protections; BookEdit
        bookEditInvalidConditions = config.getBoolean("protections.packets.book-edit.invalid-conditions");
        bookEditInvalidData = config.getBoolean("protections.packets.book-edit.invalid-data");

        // Packet protections; BookOpen
        bookOpenInvalidConditions = config.getBoolean("protections.packets.book-open.invalid-conditions");

        // Packet protections; BookSign
        bookSignInvalidConditions = config.getBoolean("protections.packets.book-sign.invalid-conditions");
        bookSignInvalidData = config.getBoolean("protections.packets.book-sign.invalid-data");

        // Packet protections; Chat
        chatInvalidConditions = config.getBoolean("protections.packets.chat.invalid-conditions");
        chatInvalidMessage = config.getBoolean("protections.packets.chat.invalid-message");

        // Packet protections; CloseWindow
        closeWindowClosedInventory = config.getBoolean("protections.packets.close-window.closed-inventory");
        closeWindowInvalidConditions = config.getBoolean("protections.packets.close-window.invalid-conditions");

        // Packet protections; CommandBlock
        commandBlockInvalidConditions = config.getBoolean("protections.packets.command-block.invalid-conditions");

        // Packet protections; CustomPayload
        customPayloadInvalidSize = config.getBoolean("protections.packets.custom-payload.invalid-size");

        // Packet protections; EnchantItem
        enchantItemInvalidWindowId = config.getBoolean("protections.packets.enchant-item.invalid-window-id");

        // Packet protections; EntityAction
        entityActionInvalidJumpBoost = config.getBoolean("protections.packets.entity-action.invalid-jump-boost");

        // Packet protections; Flying
        flyingInvalidPitch = config.getBoolean("protections.packets.flying.invalid-pitch");
        flyingInvalidPositionData = config.getBoolean("protections.packets.flying.invalid-position-data");
        flyingInvalidRotationData = config.getBoolean("protections.packets.flying.invalid-rotation-data");
        flyingInvalidYData = config.getBoolean("protections.packets.flying.invalid-y-data");

        // Packet protections; HeldItemSlot
        heldItemSlotInvalidConditions = config.getBoolean("protections.packets.held-item-slot.invalid-conditions");
        heldItemSlotInvalidSlot = config.getBoolean("protections.packets.held-item-slot.invalid-slot");
        heldItemSlotInvalidSlotChange = config.getBoolean("protections.packets.held-item-slot.invalid-slot-change");

        // Packet protections; ItemDrop
        itemDropInvalidData = config.getBoolean("protections.packets.item-drop.invalid-data");

        // Packet protections; ItemName
        itemNameInvalidData = config.getBoolean("protections.packets.item-name.invalid-data");
        itemNameInvalidSize = config.getBoolean("protections.packets.item-name.invalid-size");

        // Packet protections; ReleaseUseItem
        releaseUseItemInvalidConditions = config.getBoolean("protections.packets.release-use-item.invalid-conditions");
        releaseUseItemInvalidData = config.getBoolean("protections.packets.release-use-item.invalid-data");

        // Packet protections; Respawn
        respawnInvalidConditions = config.getBoolean("protections.packets.respawn.invalid-conditions");

        // Packet protections; RidingJump
        ridingJumpInvalidConditions = config.getBoolean("protections.packets.riding-jump.invalid-conditions");
        ridingJumpInvalidJumpBoost = config.getBoolean("protections.packets.riding-jump.invalid-jump-boost");

        // Packet protections; SetCreativeSlot
        setCreativeSlotInvalidConditions = config.getBoolean("protections.packets.set-creative-slot.invalid-conditions");
        setCreativeSlotInvalidSlot = config.getBoolean("protections.packets.set-creative-slot.invalid-slot");

        // Packet protections; Settings
        settingsInvalidLocale = config.getBoolean("protections.packets.settings.invalid-locale");
        settingsInvalidViewDistance = config.getBoolean("protections.packets.settings.invalid-view-distance");

        // Packet protections; Spectate
        spectateInvalidConditions = config.getBoolean("protections.packets.spectate.invalid-conditions");

        // Packet protections; StartSneaking
        startSneakingInvalidConditions = config.getBoolean("protections.packets.start-sneaking.invalid-conditions");

        // Packet protections; StartSprinting
        startSprintingInvalidConditions = config.getBoolean("protections.packets.start-sprinting.invalid-conditions");

        // Packet protections; SteerVehicle
        steerVehicleInvalidConditions = config.getBoolean("protections.packets.steer-vehicle.invalid-conditions");
        steerVehicleInvalidDismountValue = config.getBoolean("protections.packets.steer-vehicle.invalid-dismount-value");
        steerVehicleInvalidNonDismountValue = config.getBoolean("protections.packets.steer-vehicle.invalid-non-dismount-value");
        steerVehicleInvalidValue = config.getBoolean("protections.packets.steer-vehicle.invalid-value");

        // Packet protections; StopSleeping
        stopSleepingInvalidConditions = config.getBoolean("protections.packets.stop-sleeping.invalid-conditions");

        // Packet protections; StopSneaking
        stopSneakingInvalidConditions = config.getBoolean("protections.packets.stop-sneaking.invalid-conditions");

        // Packet protections; TabComplete
        tabCompleteInvalidMessage = config.getBoolean("protections.packets.tab-complete.invalid-message");

        // Packet protections; TradeSelect
        tradeSelectInvalidData = config.getBoolean("protections.packets.trade-select.invalid-data");

        // Packet protections; Transaction
        transactionInvalidWindowId = config.getBoolean("protections.packets.transaction.invalid-window-id");
        transactionNotAccepted = config.getBoolean("protections.packets.transaction.not-accepted");

        // Packet protections; UpdateSign
        updateSignInvalidData = config.getBoolean("protections.packets.update-sign.invalid-data");

        // Packet protections; UseEntity
        useEntityInvalidConditions = config.getBoolean("protections.packets.use-entity.invalid-conditions");
        useEntityInvalidDistance = config.getBoolean("protections.packets.use-entity.invalid-distance");

        // Packet protections; WindowClick
        windowClickInvalidCloneButton = config.getBoolean("protections.packets.window-click.invalid-clone-button");
        windowClickInvalidConditions = config.getBoolean("protections.packets.window-click.invalid-conditions");
        windowClickInvalidPickupAllButton = config.getBoolean("protections.packets.window-click.invalid-pickup-all-button");
        windowClickInvalidPickupButton = config.getBoolean("protections.packets.window-click.invalid-pickup-button");
        windowClickInvalidQuickCraftButton = config.getBoolean("protections.packets.window-click.invalid-quick-craft-button");
        windowClickInvalidQuickMoveButton = config.getBoolean("protections.packets.window-click.invalid-quick-move-button");
        windowClickInvalidSlot = config.getBoolean("protections.packets.window-click.invalid-slot");
        windowClickInvalidSwapButton = config.getBoolean("protections.packets.window-click.invalid-swap-button");
        windowClickInvalidThrowButton = config.getBoolean("protections.packets.window-click.invalid-throw-button");
    }

    /**
     * Saves the current settings into the configuration file.
     */
    public static void saveSettings() {
        // General settings
        config.set("vulture.prefix", prefix);
        config.set("vulture.banMessage", banMessage);
        config.set("vulture.resetViolations", resetViolations);

        // Blacklisted payloads
        config.set("blacklisted.payloads", blacklistedPayloads);

        // Blacklisted commands
        config.set("blacklisted.commands", blacklistedCommands);

        // PingSpoofB settings
        config.set("checks.pingspoof.B.maxPing", PingSpoofB.maxPing);
        config.set("checks.pingspoof.B.maxAveragePing", PingSpoofB.maxAveragePing);
        config.set("checks.pingspoof.B.maxPingDeviation", PingSpoofB.maxPingDeviation);

        // Packet protections; max packets per tick
        config.set("protections.packets.max-packets-per-tick", maxPacketsPerTick);

        // Packet protections; Abilities
        config.set("protections.packets.abilities.duplicate-flying", abilitiesDuplicateFlying);
        config.set("protections.packets.abilities.invalid-flight-allowed", abilitiesInvalidFlightAllowed);
        config.set("protections.packets.abilities.invalid-fly-speed", abilitiesInvalidFlySpeed);
        config.set("protections.packets.abilities.invalid-flying", abilitiesInvalidFlying);
        config.set("protections.packets.abilities.invalid-instant-build", abilitiesInvalidInstantBuild);
        config.set("protections.packets.abilities.invalid-invulnerable", abilitiesInvalidInvulnerable);
        config.set("protections.packets.abilities.invalid-walk-speed", abilitiesInvalidWalkSpeed);

        // Packet protections; ArmAnimation
        config.set("protections.packets.arm-animation.invalid-conditions", armAnimationInvalidConditions);

        // Packet protections; AttackEntity
        config.set("protections.packets.attack-entity.invalid-conditions", attackEntityInvalidConditions);

        // Packet protections; Beacon
        config.set("protections.packets.beacon.invalid-conditions", beaconInvalidConditions);
        config.set("protections.packets.beacon.invalid-data", beaconInvalidData);
        config.set("protections.packets.beacon.invalid-effect", beaconInvalidEffect);
        config.set("protections.packets.beacon.invalid-tier", beaconInvalidTier);

        // Packet protections; BlockDig
        config.set("protections.packets.block-dig.invalid-distance", blockDigInvalidDistance);

        // Packet protections; BlockPlace
        config.set("protections.packets.block-place.invalid-conditions", blockPlaceInvalidConditions);
        config.set("protections.packets.block-place.invalid-cursor-position", blockPlaceInvalidCursorPosition);
        config.set("protections.packets.block-place.invalid-distance", blockPlaceInvalidDistance);
        config.set("protections.packets.block-place.invalid-item", blockPlaceInvalidItem);
        config.set("protections.packets.block-place.invalid-other-block-position", blockPlaceInvalidOtherBlockPosition);
        config.set("protections.packets.block-place.invalid-other-cursor-position", blockPlaceInvalidOtherCursorPosition);
        config.set("protections.packets.block-place.invalid-up-block-position", blockPlaceInvalidUpBlockPosition);

        // Packet protections; BookEdit
        config.set("protections.packets.book-edit.invalid-conditions", bookEditInvalidConditions);
        config.set("protections.packets.book-edit.invalid-data", bookEditInvalidData);

        // Packet protections; BookOpen
        config.set("protections.packets.book-open.invalid-conditions", bookOpenInvalidConditions);

        // Packet protections; BookSign
        config.set("protections.packets.book-sign.invalid-conditions", bookSignInvalidConditions);
        config.set("protections.packets.book-sign.invalid-data", bookSignInvalidData);

        // Packet protections; Chat
        config.set("protections.packets.chat.invalid-conditions", chatInvalidConditions);
        config.set("protections.packets.chat.invalid-message", chatInvalidMessage);

        // Packet protections; CloseWindow
        config.set("protections.packets.close-window.closed-inventory", closeWindowClosedInventory);
        config.set("protections.packets.close-window.invalid-conditions", closeWindowInvalidConditions);

        // Packet protections; CommandBlock
        config.set("protections.packets.command-block.invalid-conditions", commandBlockInvalidConditions);

        // Packet protections; CustomPayload
        config.set("protections.packets.custom-payload.invalid-size", customPayloadInvalidSize);

        // Packet protections; EnchantItem
        config.set("protections.packets.enchant-item.invalid-window-id", enchantItemInvalidWindowId);

        // Packet protections; EntityAction
        config.set("protections.packets.entity-action.invalid-jump-boost", entityActionInvalidJumpBoost);

        // Packet protections; Flying
        config.set("protections.packets.flying.invalid-pitch", flyingInvalidPitch);
        config.set("protections.packets.flying.invalid-position-data", flyingInvalidPositionData);
        config.set("protections.packets.flying.invalid-rotation-data", flyingInvalidRotationData);
        config.set("protections.packets.flying.invalid-y-data", flyingInvalidYData);

        // Packet protections; HeldItemSlot
        config.set("protections.packets.held-item-slot.invalid-conditions", heldItemSlotInvalidConditions);
        config.set("protections.packets.held-item-slot.invalid-slot", heldItemSlotInvalidSlot);
        config.set("protections.packets.held-item-slot.invalid-slot-change", heldItemSlotInvalidSlotChange);

        // Packet protections; ItemDrop
        config.set("protections.packets.item-drop.invalid-data", itemDropInvalidData);

        // Packet protections; ItemName
        config.set("protections.packets.item-name.invalid-data", itemNameInvalidData);
        config.set("protections.packets.item-name.invalid-size", itemNameInvalidSize);

        // Packet protections; ReleaseUseItem
        config.set("protections.packets.release-use-item.invalid-conditions", releaseUseItemInvalidConditions);
        config.set("protections.packets.release-use-item.invalid-data", releaseUseItemInvalidData);

        // Packet protections; Respawn
        config.set("protections.packets.respawn.invalid-conditions", respawnInvalidConditions);

        // Packet protections; RidingJump
        config.set("protections.packets.riding-jump.invalid-conditions", ridingJumpInvalidConditions);
        config.set("protections.packets.riding-jump.invalid-jump-boost", ridingJumpInvalidJumpBoost);

        // Packet protections; SetCreativeSlot
        config.set("protections.packets.set-creative-slot.invalid-conditions", setCreativeSlotInvalidConditions);
        config.set("protections.packets.set-creative-slot.invalid-slot", setCreativeSlotInvalidSlot);

        // Packet protections; Settings
        config.set("protections.packets.settings.invalid-locale", settingsInvalidLocale);
        config.set("protections.packets.settings.invalid-view-distance", settingsInvalidViewDistance);

        // Packet protections; Spectate
        config.set("protections.packets.spectate.invalid-conditions", spectateInvalidConditions);

        // Packet protections; StartSneaking
        config.set("protections.packets.start-sneaking.invalid-conditions", startSneakingInvalidConditions);

        // Packet protections; StartSprinting
        config.set("protections.packets.start-sprinting.invalid-conditions", startSprintingInvalidConditions);

        // Packet protections; SteerVehicle
        config.set("protections.packets.steer-vehicle.invalid-conditions", steerVehicleInvalidConditions);
        config.set("protections.packets.steer-vehicle.invalid-dismount-value", steerVehicleInvalidDismountValue);
        config.set("protections.packets.steer-vehicle.invalid-non-dismount-value", steerVehicleInvalidNonDismountValue);
        config.set("protections.packets.steer-vehicle.invalid-value", steerVehicleInvalidValue);

        // Packet protections; StopSleeping
        config.set("protections.packets.stop-sleeping.invalid-conditions", stopSleepingInvalidConditions);

        // Packet protections; StopSneaking
        config.set("protections.packets.stop-sneaking.invalid-conditions", stopSneakingInvalidConditions);

        // Packet protections; TabComplete
        config.set("protections.packets.tab-complete.invalid-message", tabCompleteInvalidMessage);

        // Packet protections; TradeSelect
        config.set("protections.packets.trade-select.invalid-data", tradeSelectInvalidData);

        // Packet protections; Transaction
        config.set("protections.packets.transaction.invalid-window-id", transactionInvalidWindowId);
        config.set("protections.packets.transaction.not-accepted", transactionNotAccepted);

        // Packet protections; UpdateSign
        config.set("protections.packets.update-sign.invalid-data", updateSignInvalidData);

        // Packet protections; UseEntity
        config.set("protections.packets.use-entity.invalid-conditions", useEntityInvalidConditions);
        config.set("protections.packets.use-entity.invalid-distance", useEntityInvalidDistance);

        // Packet protections; WindowClick
        config.set("protections.packets.window-click.invalid-clone-button", windowClickInvalidCloneButton);
        config.set("protections.packets.window-click.invalid-conditions", windowClickInvalidConditions);
        config.set("protections.packets.window-click.invalid-pickup-all-button", windowClickInvalidPickupAllButton);
        config.set("protections.packets.window-click.invalid-pickup-button", windowClickInvalidPickupButton);
        config.set("protections.packets.window-click.invalid-quick-craft-button", windowClickInvalidQuickCraftButton);
        config.set("protections.packets.window-click.invalid-quick-move-button", windowClickInvalidQuickMoveButton);
        config.set("protections.packets.window-click.invalid-slot", windowClickInvalidSlot);
        config.set("protections.packets.window-click.invalid-swap-button", windowClickInvalidSwapButton);
        config.set("protections.packets.window-click.invalid-throw-button", windowClickInvalidThrowButton);

        saveConfig();
    }

    /**
     * Initializes the configuration file.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void initConfig() {
        file = new File(Vulture.instance.getDataFolder(), "settings.yml");

        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException ex) {
                MessageUtil.log(Level.WARNING, "Failed to create settings file.");
                ex.printStackTrace();
            }
        }

        config = YamlConfiguration.loadConfiguration(file);
    }

    /**
     * Sets the default values for the configuration file.
     */
    private static void setDefaultConfigValues() {
        // General settings
        config.addDefault("vulture.prefix", prefix);
        config.addDefault("vulture.banMessage", banMessage);
        config.addDefault("vulture.resetViolations", resetViolations);

        // Blacklisted payloads
        config.addDefault("blacklisted.payloads", blacklistedPayloads);

        // Blacklisted commands
        config.addDefault("blacklisted.commands", blacklistedCommands);

        // Check settings
        for (Class<? extends Check> check : CheckManager.CHECK_CLASSES) {
            String name = getCheckName(check);
            CheckInfo checkInfo = check.getAnnotation(CheckInfo.class);

            boolean enabled = checkInfo.enabled();
            int maxViolations = checkInfo.maxViolations();
            String banCommand = checkInfo.banCommand();

            config.addDefault("checks." + name + ".enabled", enabled);

            if (checkInfo.punishable()) {
                config.addDefault("checks." + name + ".maxViolations", maxViolations);
                config.addDefault("checks." + name + ".banCommand", banCommand);
            }

            // PingSpoofB settings
            if (name.equals("pingspoof.B")) {
                config.addDefault("checks." + name + ".maxPing", 1000);
                config.addDefault("checks." + name + ".maxAveragePing", 500);
                config.addDefault("checks." + name + ".maxPingDeviation", 200);
            }
        }

        // Packet protections; max packets per tick
        config.addDefault("protections.packets.max-packets-per-tick", maxPacketsPerTick);

        // Packet protections; Abilities
        config.addDefault("protections.packets.abilities.duplicate-flying", abilitiesDuplicateFlying);
        config.addDefault("protections.packets.abilities.invalid-flight-allowed", abilitiesInvalidFlightAllowed);
        config.addDefault("protections.packets.abilities.invalid-fly-speed", abilitiesInvalidFlySpeed);
        config.addDefault("protections.packets.abilities.invalid-flying", abilitiesInvalidFlying);
        config.addDefault("protections.packets.abilities.invalid-instant-build", abilitiesInvalidInstantBuild);
        config.addDefault("protections.packets.abilities.invalid-invulnerable", abilitiesInvalidInvulnerable);
        config.addDefault("protections.packets.abilities.invalid-walk-speed", abilitiesInvalidWalkSpeed);

        // Packet protections; ArmAnimation
        config.addDefault("protections.packets.arm-animation.invalid-conditions", armAnimationInvalidConditions);

        // Packet protections; AttackEntity
        config.addDefault("protections.packets.attack-entity.invalid-conditions", attackEntityInvalidConditions);

        // Packet protections; Beacon
        config.addDefault("protections.packets.beacon.invalid-conditions", beaconInvalidConditions);
        config.addDefault("protections.packets.beacon.invalid-data", beaconInvalidData);
        config.addDefault("protections.packets.beacon.invalid-effect", beaconInvalidEffect);
        config.addDefault("protections.packets.beacon.invalid-tier", beaconInvalidTier);

        // Packet protections; BlockDig
        config.addDefault("protections.packets.block-dig.invalid-distance", blockDigInvalidDistance);

        // Packet protections; BlockPlace
        config.addDefault("protections.packets.block-place.invalid-conditions", blockPlaceInvalidConditions);
        config.addDefault("protections.packets.block-place.invalid-cursor-position", blockPlaceInvalidCursorPosition);
        config.addDefault("protections.packets.block-place.invalid-distance", blockPlaceInvalidDistance);
        config.addDefault("protections.packets.block-place.invalid-item", blockPlaceInvalidItem);
        config.addDefault("protections.packets.block-place.invalid-other-block-position", blockPlaceInvalidOtherBlockPosition);
        config.addDefault("protections.packets.block-place.invalid-other-cursor-position", blockPlaceInvalidOtherCursorPosition);
        config.addDefault("protections.packets.block-place.invalid-up-block-position", blockPlaceInvalidUpBlockPosition);

        // Packet protections; BookEdit
        config.addDefault("protections.packets.book-edit.invalid-conditions", bookEditInvalidConditions);
        config.addDefault("protections.packets.book-edit.invalid-data", bookEditInvalidData);

        // Packet protections; BookOpen
        config.addDefault("protections.packets.book-open.invalid-conditions", bookOpenInvalidConditions);

        // Packet protections; BookSign
        config.addDefault("protections.packets.book-sign.invalid-conditions", bookSignInvalidConditions);
        config.addDefault("protections.packets.book-sign.invalid-data", bookSignInvalidData);

        // Packet protections; Chat
        config.addDefault("protections.packets.chat.invalid-conditions", chatInvalidConditions);
        config.addDefault("protections.packets.chat.invalid-message", chatInvalidMessage);

        // Packet protections; CloseWindow
        config.addDefault("protections.packets.close-window.closed-inventory", closeWindowClosedInventory);
        config.addDefault("protections.packets.close-window.invalid-conditions", closeWindowInvalidConditions);

        // Packet protections; CommandBlock
        config.addDefault("protections.packets.command-block.invalid-conditions", commandBlockInvalidConditions);

        // Packet protections; CustomPayload
        config.addDefault("protections.packets.custom-payload.invalid-size", customPayloadInvalidSize);

        // Packet protections; EnchantItem
        config.addDefault("protections.packets.enchant-item.invalid-window-id", enchantItemInvalidWindowId);

        // Packet protections; EntityAction
        config.addDefault("protections.packets.entity-action.invalid-jump-boost", entityActionInvalidJumpBoost);

        // Packet protections; Flying
        config.addDefault("protections.packets.flying.invalid-pitch", flyingInvalidPitch);
        config.addDefault("protections.packets.flying.invalid-position-data", flyingInvalidPositionData);
        config.addDefault("protections.packets.flying.invalid-rotation-data", flyingInvalidRotationData);
        config.addDefault("protections.packets.flying.invalid-y-data", flyingInvalidYData);

        // Packet protections; HeldItemSlot
        config.addDefault("protections.packets.held-item-slot.invalid-conditions", heldItemSlotInvalidConditions);
        config.addDefault("protections.packets.held-item-slot.invalid-slot", heldItemSlotInvalidSlot);
        config.addDefault("protections.packets.held-item-slot.invalid-slot-change", heldItemSlotInvalidSlotChange);

        // Packet protections; ItemDrop
        config.addDefault("protections.packets.item-drop.invalid-data", itemDropInvalidData);

        // Packet protections; ItemName
        config.addDefault("protections.packets.item-name.invalid-data", itemNameInvalidData);
        config.addDefault("protections.packets.item-name.invalid-size", itemNameInvalidSize);

        // Packet protections; ReleaseUseItem
        config.addDefault("protections.packets.release-use-item.invalid-conditions", releaseUseItemInvalidConditions);
        config.addDefault("protections.packets.release-use-item.invalid-data", releaseUseItemInvalidData);

        // Packet protections; Respawn
        config.addDefault("protections.packets.respawn.invalid-conditions", respawnInvalidConditions);

        // Packet protections; RidingJump
        config.addDefault("protections.packets.riding-jump.invalid-conditions", ridingJumpInvalidConditions);
        config.addDefault("protections.packets.riding-jump.invalid-jump-boost", ridingJumpInvalidJumpBoost);

        // Packet protections; SetCreativeSlot
        config.addDefault("protections.packets.set-creative-slot.invalid-conditions", setCreativeSlotInvalidConditions);
        config.addDefault("protections.packets.set-creative-slot.invalid-slot", setCreativeSlotInvalidSlot);

        // Packet protections; Settings
        config.addDefault("protections.packets.settings.invalid-locale", settingsInvalidLocale);
        config.addDefault("protections.packets.settings.invalid-view-distance", settingsInvalidViewDistance);

        // Packet protections; Spectate
        config.addDefault("protections.packets.spectate.invalid-conditions", spectateInvalidConditions);

        // Packet protections; StartSneaking
        config.addDefault("protections.packets.start-sneaking.invalid-conditions", startSneakingInvalidConditions);

        // Packet protections; StartSprinting
        config.addDefault("protections.packets.start-sprinting.invalid-conditions", startSprintingInvalidConditions);

        // Packet protections; SteerVehicle
        config.addDefault("protections.packets.steer-vehicle.invalid-conditions", steerVehicleInvalidConditions);
        config.addDefault("protections.packets.steer-vehicle.invalid-dismount-value", steerVehicleInvalidDismountValue);
        config.addDefault("protections.packets.steer-vehicle.invalid-non-dismount-value", steerVehicleInvalidNonDismountValue);
        config.addDefault("protections.packets.steer-vehicle.invalid-value", steerVehicleInvalidValue);

        // Packet protections; StopSleeping
        config.addDefault("protections.packets.stop-sleeping.invalid-conditions", stopSleepingInvalidConditions);

        // Packet protections; StopSneaking
        config.addDefault("protections.packets.stop-sneaking.invalid-conditions", stopSneakingInvalidConditions);

        // Packet protections; TabComplete
        config.addDefault("protections.packets.tab-complete.invalid-message", tabCompleteInvalidMessage);

        // Packet protections; TradeSelect
        config.addDefault("protections.packets.trade-select.invalid-data", tradeSelectInvalidData);

        // Packet protections; Transaction
        config.addDefault("protections.packets.transaction.invalid-window-id", transactionInvalidWindowId);
        config.addDefault("protections.packets.transaction.not-accepted", transactionNotAccepted);

        // Packet protections; UpdateSign
        config.addDefault("protections.packets.update-sign.invalid-data", updateSignInvalidData);

        // Packet protections; UseEntity
        config.addDefault("protections.packets.use-entity.invalid-conditions", useEntityInvalidConditions);
        config.addDefault("protections.packets.use-entity.invalid-distance", useEntityInvalidDistance);

        // Packet protections; WindowClick
        config.addDefault("protections.packets.window-click.invalid-clone-button", windowClickInvalidCloneButton);
        config.addDefault("protections.packets.window-click.invalid-conditions", windowClickInvalidConditions);
        config.addDefault("protections.packets.window-click.invalid-pickup-all-button", windowClickInvalidPickupAllButton);
        config.addDefault("protections.packets.window-click.invalid-pickup-button", windowClickInvalidPickupButton);
        config.addDefault("protections.packets.window-click.invalid-quick-craft-button", windowClickInvalidQuickCraftButton);
        config.addDefault("protections.packets.window-click.invalid-quick-move-button", windowClickInvalidQuickMoveButton);
        config.addDefault("protections.packets.window-click.invalid-slot", windowClickInvalidSlot);
        config.addDefault("protections.packets.window-click.invalid-swap-button", windowClickInvalidSwapButton);
        config.addDefault("protections.packets.window-click.invalid-throw-button", windowClickInvalidThrowButton);

        config.options().copyDefaults(true);
    }

    /**
     * Saves the configuration file.
     */
    private static void saveConfig() {
        try {
            config.save(file);
        } catch (IOException ex) {
            MessageUtil.log(Level.WARNING, "Couldn't save the config file.");
        }
    }

    /**
     * Gets the name of a check.
     *
     * @param check The check.
     * @return The name of the check.
     */
    private static String getCheckName(Class<? extends Check> check) {
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
    public static void changeAnnotationValue(@NonNull Annotation annotation,
                                             @NonNull String key, @NonNull Object newValue) {
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
