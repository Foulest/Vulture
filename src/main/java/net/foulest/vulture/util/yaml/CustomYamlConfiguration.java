/*
 * Vulture - an advanced anti-cheat plugin designed for Minecraft 1.8.9 servers.
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
package net.foulest.vulture.util.yaml;

import lombok.Cleanup;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * CustomYamlConfiguration is an extension of YamlConfiguration
 * that allows for comments to be stored and loaded.
 *
 * @author Foulest
 */
public class CustomYamlConfiguration extends YamlConfiguration {

    /**
     * Map to store the path of the YAML keys and their associated comments
     */
    private final Map<String, String> commentsMap = new LinkedHashMap<>();

    /**
     * Loads the CustomYamlConfiguration from a String.
     *
     * @param contents The YAML data to load.
     * @throws InvalidConfigurationException If the configuration is invalid.
     */
    @Override
    public void loadFromString(String contents) throws InvalidConfigurationException {
        super.loadFromString(contents); // Call the original method to load the data

        // Reset comments map to ensure it's clean on each load
        commentsMap.clear();

        // Implement logic to parse and store comments
        parseAndStoreComments(contents);
    }

    /**
     * Saves the CustomYamlConfiguration to a String.
     *
     * @return The YAML data as a String.
     */
    @Override
    public String saveToString() {
        // Strip all comments from the original data
        String dataWithoutComments = super.saveToString().trim();

        // Use a pattern to match YAML comments and remove them
        String dataStrippedOfComments = dataWithoutComments.replaceAll("(?m)^\\s*#.*$", "").trim();

        StringBuilder dataWithComments = new StringBuilder();

        // Insert header comments
        String headerComment = commentsMap.getOrDefault("__header__", "");
        if (!headerComment.isEmpty()) {
            for (String line : headerComment.split("\n")) {
                dataWithComments.append("# ").append(line).append("\n");
            }
        }

        // Iterate through each line of the stripped YAML data to reinsert comments
        String[] lines = dataStrippedOfComments.split("\n");

        for (String line : lines) {
            // Attempt to extract a key from the current line
            String key = getKeyFromLine(line);

            if (key != null && commentsMap.containsKey(key)) {
                // Insert comment for the key before the line
                String comment = commentsMap.get(key);

                if (!comment.isEmpty()) {
                    String[] commentLines = comment.split("\n");

                    // Determine the indentation of the current line
                    int indent = line.indexOf(key);
                    StringBuilder indentBuilder = new StringBuilder();

                    for (int i = 0; i < indent; i++) {
                        indentBuilder.append(" "); // Append space to match the indentation
                    }

                    String indentSpace = indentBuilder.toString(); // Create an indentation string

                    for (String commentLine : commentLines) {
                        dataWithComments.append(indentSpace).append("# ").append(commentLine).append(System.lineSeparator());
                    }
                }
            }

            // Append the current line of YAML data
            dataWithComments.append(line).append(System.lineSeparator());
        }

        // Add footer comments from commentsMap if present
        String footerComment = commentsMap.getOrDefault("__footer__", "");
        if (!footerComment.isEmpty()) {
            for (String line : footerComment.split("\n")) {
                dataWithComments.append("# ").append(line).append("\n");
            }
        }
        return dataWithComments.toString();
    }

    /**
     * Loads a CustomYamlConfiguration from a file.
     *
     * @param file The file to load the configuration from.
     * @throws IOException If an I/O error occurs.
     * @throws InvalidConfigurationException If the configuration is invalid.
     */
    @Override
    public void load(File file) throws IOException, InvalidConfigurationException {
        @Cleanup FileInputStream stream = new FileInputStream(file);
        @Cleanup InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
        load(reader);
    }

    /**
     * Loads a CustomYamlConfiguration from a reader.
     *
     * @param reader The reader to load the configuration from.
     * @throws IOException If an I/O error occurs.
     * @throws InvalidConfigurationException If the configuration is invalid.
     */
    @Override
    public void load(Reader reader) throws IOException, InvalidConfigurationException {
        @Cleanup BufferedReader input = reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader);
        String builder = input.lines().map(line -> line + '\n').collect(Collectors.joining());
        loadFromString(builder);
    }

    /**
     * Loads a CustomYamlConfiguration from a file.
     *
     * @param file The file to load the configuration from.
     * @return The loaded CustomYamlConfiguration.
     */
    public static @NotNull CustomYamlConfiguration loadConfiguration(File file) {
        CustomYamlConfiguration config = new CustomYamlConfiguration();

        try {
            config.load(file);
        } catch (FileNotFoundException ignored) {
        } catch (IOException | InvalidConfigurationException ex) {
            ex.printStackTrace();
        }
        return config;
    }

    /**
     * Loads a CustomYamlConfiguration from a reader.
     *
     * @param reader The reader to load the configuration from.
     * @return The loaded CustomYamlConfiguration.
     */
    public static @NotNull CustomYamlConfiguration loadConfiguration(Reader reader) {
        CustomYamlConfiguration config = new CustomYamlConfiguration();

        try {
            config.load(reader);
        } catch (IOException | InvalidConfigurationException ex) {
            ex.printStackTrace();
        }
        return config;
    }

    /**
     * Extracts the key from a YAML line, if present. Adjust regex as necessary.
     *
     * @param line The line of YAML to extract the key from.
     * @return The key if the line contains a key-value pair, otherwise null.
     */
    private static @Nullable String getKeyFromLine(CharSequence line) {
        Matcher matcher = Pattern.compile("^\\s*([\\w\\-]+):").matcher(line);
        return matcher.find() ? matcher.group(1) : null;
    }

    /**
     * Parses the YAML data and stores the comments in the comments map.
     * This method is called when loading YAML data to ensure comments are stored.
     *
     * @param contents The YAML data to parse and store comments for.
     */
    private void parseAndStoreComments(@NotNull String contents) {
        String[] lines = contents.split("\n");
        StringBuilder commentBuilder = new StringBuilder();
        String lastComment = commentBuilder.toString();
        boolean isHeader = true; // Assume the first comments are header comments

        // Define the pattern within this method
        Pattern keyPattern = Pattern.compile("^\\s*([\\w\\-]+):.*");

        for (String entry : lines) {
            String line = entry;

            if (!line.trim().isEmpty() && line.trim().charAt(0) == '#') {
                if (commentBuilder.length() > 0) {
                    commentBuilder.append("\n");
                }

                // Remove '#' and trim
                line = line.trim();
                line = line.replaceFirst("^#", "");
                line = line.trim();

                commentBuilder.append(line);

            } else {
                if (!line.trim().isEmpty() && isHeader) {
                    // Store header comments and mark that we've found a non-comment line
                    commentsMap.put("__header__", lastComment);
                    commentBuilder.setLength(0);
                    isHeader = false; // No longer reading header comments
                }

                Matcher matcher = keyPattern.matcher(line);

                if (matcher.find()) {
                    // Found a key, store the accumulated comments if any
                    String key = matcher.group(1);
                    if (commentBuilder.length() > 0) {
                        commentsMap.put(key, lastComment);
                        commentBuilder.setLength(0); // Reset comment builder
                    }
                }
            }
        }

        // In case the file ends with comments not associated with a key
        if (commentBuilder.length() > 0 && !isHeader) {
            commentsMap.put("__footer__", lastComment);
        }
    }
}
