package com.org.education_management.upgrade.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class ISUUtil {
    public static String readFileAsString(String filePath) {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append(System.lineSeparator()); // Append each line with a new line
            }
        } catch (IOException e) {
            System.err.println("Error reading the file: " + e.getMessage());
            return null;
        }
        return content.toString(); // Return the content as a String
    }
}
