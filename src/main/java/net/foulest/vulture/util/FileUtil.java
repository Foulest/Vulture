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

import lombok.Data;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

@Data
public class FileUtil {

    /**
     * Prints data to a text file.
     *
     * @param data     The data to print.
     * @param fileName The file name to print to.
     */
    public static void printDataToFile(String data, String fileName) {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(fileName), StandardCharsets.UTF_8)) {
            writer.write(data);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
