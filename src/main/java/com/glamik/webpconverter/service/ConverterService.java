package com.glamik.webpconverter.service;

import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

@Service
public class ConverterService {

    public File convertToWebp(File inputFile) throws IOException {
        String outputFileName = getFileNameWithoutExtension(inputFile.getName()) + ".webp";
        File outputFile = new File(inputFile.getParent() + outputFileName);

        BufferedImage image = ImageIO.read(inputFile);
        if (image == null) {
            throw new IOException("Invalid image file: " + inputFile.getName());
        }

        boolean success = ImageIO.write(image, "webp", outputFile);
        if (!success) {
            throw new IOException("Failed to write image: " + outputFile.getName());
        }

        return outputFile;
    }

    private String getFileNameWithoutExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        return (lastDot == -1) ? filename : filename.substring(0, lastDot);
    }
}
