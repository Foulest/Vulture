package net.foulest.vulture.util;

import lombok.Cleanup;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class FileUtil {

    /**
     * Prints data to a text file.
     *
     * @param data     The data to print.
     * @param fileName The file name to print to.
     */
    public static void printDataToFile(String data, String fileName) {
        try {
            @Cleanup BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
            writer.write(data);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
