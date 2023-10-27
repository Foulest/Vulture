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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * Utility class for settings.
 * <p>
 *
 * @author Foulest
 * @project Vulture
 */
@Getter
@Setter
public class Settings {

    public static File file;
    public static FileConfiguration config;

    public static String prefix;
    public static List<String> banMessage;
    public static long resetViolations;

    public static List<String> blacklistedPayloads;

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

        prefix = config.getString("vulture.prefix");
        banMessage = config.getStringList("vulture.banMessage");
        resetViolations = config.getLong("vulture.resetViolations");

        blacklistedPayloads = config.getStringList("vulture.blacklisted-payloads");

        PingSpoofB.maxPing = config.getLong("checks.pingspoof.B.maxPing");
        PingSpoofB.maxAveragePing = config.getLong("checks.pingspoof.B.maxAveragePing");
        PingSpoofB.maxPingDeviation = config.getLong("checks.pingspoof.B.maxPingDeviation");

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
    }

    /**
     * Saves the current settings into the configuration file.
     */
    public static void saveSettings() {
        config.set("vulture.prefix", prefix);
        config.set("vulture.banMessage", banMessage);
        config.set("vulture.resetViolations", resetViolations);

        config.set("vulture.blacklisted-payloads", blacklistedPayloads);

        config.set("checks.pingspoof.B.maxPing", PingSpoofB.maxPing);
        config.set("checks.pingspoof.B.maxAveragePing", PingSpoofB.maxAveragePing);
        config.set("checks.pingspoof.B.maxPingDeviation", PingSpoofB.maxPingDeviation);

        saveConfig();
    }

    /**
     * Initializes the configuration file.
     */
    private static void initConfig() {
        file = new File(Vulture.instance.getDataFolder(), "settings.yml");

        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException ex) {
                MessageUtil.log(Level.WARNING, "Couldn't create the config file.");
                ex.printStackTrace();
            }
        }

        config = YamlConfiguration.loadConfiguration(file);
    }

    /**
     * Sets the default values for the configuration file.
     */
    private static void setDefaultConfigValues() {
        config.addDefault("vulture.prefix", "&e[Vulture]");
        config.addDefault("vulture.banMessage", Collections.singletonList("&c%player% has been removed from the network."));
        config.addDefault("vulture.resetViolations", 600);

        config.addDefault("vulture.blacklisted-payloads", Collections.singletonList("GalactiCraft"));

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

            if (name.equals("pingspoof.B")) {
                config.addDefault("checks." + name + ".maxPing", 1000);
                config.addDefault("checks." + name + ".maxAveragePing", 500);
                config.addDefault("checks." + name + ".maxPingDeviation", 200);
            }
        }

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
     * Gets the name of the check.
     *
     * @param check The check class.
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
     * Changes the annotation value for the given key of the given annotation to newValue and returns
     * the previous value.
     */
    @SuppressWarnings("unchecked")
    public static void changeAnnotationValue(@NonNull Annotation annotation,
                                             @NonNull String key, @NonNull Object newValue) {
        Object handler = Proxy.getInvocationHandler(annotation);
        Field field;

        try {
            field = handler.getClass().getDeclaredField("memberValues");
        } catch (NoSuchFieldException | SecurityException e) {
            throw new IllegalStateException(e);
        }

        field.setAccessible(true);

        Map<String, Object> memberValues;

        try {
            memberValues = (Map<String, Object>) field.get(handler);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }

        Object oldValue = memberValues.get(key);

        if (oldValue == null || oldValue.getClass() != newValue.getClass()) {
            throw new IllegalArgumentException();
        }

        memberValues.put(key, newValue);
    }
}
