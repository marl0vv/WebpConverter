package com.glamik.webpconverter.service;

import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;

@Service
public class ConverterService {

    public Path convertToWebp(Path inputPath) throws IOException {
        String outputFileName = getFileNameWithoutExtension(inputPath.getFileName().toString()) + ".webp";
        Path outputPath = inputPath.getParent().resolve(outputFileName);

        BufferedImage image = ImageIO.read(inputPath.toFile());
        if (image == null) {
            throw new IOException("Invalid image file: " + inputPath.getFileName());
        }

        boolean success = ImageIO.write(image, "webp", outputPath.toFile());
        if (!success) {
            throw new IOException("Failed to write image: " + outputPath.getFileName());
        }

        return outputPath;
    }

    private String getFileNameWithoutExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        return (lastDot == -1) ? filename : filename.substring(0, lastDot);
    }
}
