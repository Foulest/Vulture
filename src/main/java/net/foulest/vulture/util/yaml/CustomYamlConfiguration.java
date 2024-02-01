package net.foulest.vulture.util.yaml;

import com.google.common.base.Charsets;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CustomYamlConfiguration extends YamlConfiguration {

    // Map to store the path of the YAML keys and their associated comments
    private final Map<String, String> commentsMap = new LinkedHashMap<>();

    public CustomYamlConfiguration() {
        super();
    }

    @Override
    public void loadFromString(String contents) throws InvalidConfigurationException {
        super.loadFromString(contents); // Call the original method to load the data

        // Reset comments map to ensure it's clean on each load
        commentsMap.clear();

        // Implement logic to parse and store comments
        parseAndStoreComments(contents);
    }

    @Override
    public String saveToString() {
        String dataWithoutComments = super.saveToString().trim();
        StringBuilder dataWithComments = new StringBuilder();

        // Process header comments if present
        String headerKey = "__header__";
        if (commentsMap.containsKey(headerKey)) {
            String headerComment = commentsMap.get(headerKey);
            StringBuilder headerBuilder = new StringBuilder();
            String[] headerLines = headerComment.split("\n");

            for (String line : headerLines) {
                headerBuilder.append("#").append(line).append(System.lineSeparator());
            }

            String header = headerBuilder.toString().trim();

            // Check if the actual data starts with any other YAML content before the header
            if (!dataWithoutComments.startsWith(header)) {
                // If the header is not at the beginning, prepend it
                dataWithComments.append(headerBuilder).append(System.lineSeparator());
            }
        }

        // Split the data into lines for processing
        String[] lines = dataWithoutComments.split("\n");

        // Temporary storage to build the current line's comment, if any
        StringBuilder commentBuilder = new StringBuilder();

        // Iterate through each line of the YAML data
        for (String line : lines) {
            // Check if the line starts with a key that has a stored comment
            String key = getKeyFromLine(line);

            if (key != null && commentsMap.containsKey(key)) {
                // If there's a comment for this key, prepare it
                String comment = commentsMap.get(key);
                String[] commentLines = comment.split("\n");

                for (String commentLine : commentLines) {
                    commentBuilder.append("# ").append(commentLine).append("\n");
                }
            }

            // Append the comment before the line it's associated with
            if (commentBuilder.length() > 0) {
                dataWithComments.append(commentBuilder);
                commentBuilder.setLength(0); // Clear the comment builder for the next use
            }

            // Append the current line of YAML data
            dataWithComments.append(line).append("\n");
        }

        // Return the YAML data as a string, now including comments
        return dataWithComments.toString();
    }

    @Override
    public void load(File file) throws IOException, InvalidConfigurationException {
        FileInputStream stream = new FileInputStream(file);
        load(new InputStreamReader(stream, Charsets.UTF_8));
    }

    @Override
    public void load(Reader reader) throws IOException, InvalidConfigurationException {
        BufferedReader input = reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader);
        StringBuilder builder = new StringBuilder();
        String line;

        try {
            while ((line = input.readLine()) != null) {
                builder.append(line);
                builder.append('\n');
            }
        } finally {
            input.close();
        }

        loadFromString(builder.toString());
    }

    public static @NotNull CustomYamlConfiguration loadConfiguration(File file) {
        CustomYamlConfiguration config = new CustomYamlConfiguration();

        try {
            config.load(file);
        } catch (FileNotFoundException ignored) {
        } catch (IOException | InvalidConfigurationException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Cannot load " + file, ex);
        }
        return config;
    }

    public static @NotNull CustomYamlConfiguration loadConfiguration(Reader reader) {
        CustomYamlConfiguration config = new CustomYamlConfiguration();

        try {
            config.load(reader);
        } catch (IOException | InvalidConfigurationException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Cannot load configuration from stream", ex);
        }
        return config;
    }

    /**
     * Extracts the key from a YAML line, if present.
     * This is a simplistic approach and might need refining for complex YAML structures.
     *
     * @param line The line of YAML to extract the key from.
     * @return The key if the line contains a key-value pair, otherwise null.
     */
    private @Nullable String getKeyFromLine(String line) {
        // Match lines that start with a YAML key (followed by a colon).
        // This regex needs to be refined based on the actual YAML structure and indentation.
        Pattern pattern = Pattern.compile("^\\s*([\\w\\-]+):");
        Matcher matcher = pattern.matcher(line);

        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private void parseAndStoreComments(@NotNull String contents) {
        String[] lines = contents.split("\n");
        StringBuilder commentBuilder = new StringBuilder();
        boolean isHeader = true; // Assume the first comments are header comments

        // Define the pattern within this method
        Pattern keyPattern = Pattern.compile("^\\s*([\\w\\-]+):.*");

        for (String line : lines) {
            if (line.trim().startsWith("#")) {
                if (commentBuilder.length() > 0) {
                    commentBuilder.append("\n");
                }

                commentBuilder.append(line.trim().substring(1).trim()); // Remove '#' and trim

            } else {
                if (!line.trim().isEmpty()) {
                    // If we encounter a non-empty line that's not a comment, assume subsequent comments are not headers
                    isHeader = false;
                }

                Matcher matcher = keyPattern.matcher(line);

                if (matcher.find()) {
                    // Found a key, store the accumulated comments if any
                    String key = matcher.group(1);

                    if (commentBuilder.length() > 0) {
                        String lastComment = commentBuilder.toString();
                        commentsMap.put(key, lastComment);
                        commentBuilder.setLength(0); // Reset comment builder
                    }

                } else if (isHeader && commentBuilder.length() > 0) {
                    // Handle header comments specifically
                    String lastComment = commentBuilder.toString();
                    commentsMap.put("__header__", lastComment);
                    System.out.println("Header: " + lastComment);
                    commentBuilder.setLength(0); // Reset for the next header or key comment
                }
            }
        }
    }
}
