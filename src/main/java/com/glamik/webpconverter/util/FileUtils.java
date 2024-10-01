package com.glamik.webpconverter.util;

import java.io.File;
import java.nio.file.Files;

public class FileUtils {
    public static String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        return (lastDot == -1) ? ".tmp" : filename.substring(lastDot);
    }

    public static String getFileNameWithoutExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        return (lastDot == -1) ? filename : filename.substring(0, lastDot);
    }

    public static void deleteIfExists(File file) {
        if (file != null && Files.exists(file.toPath())) {
            file.deleteOnExit();
        }
    }
}
