package com.glamik.webpconverter.service;

import org.apache.tika.Tika;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

@Service
public class ConverterService {

    public File convertToWebp(File inputFile) throws IOException {
        checkInputMimeType(inputFile);
        File outputFile = new File(inputFile.getParent() + "/ConvertedTempImage.webp");

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

    private void checkInputMimeType(File inputFile) throws IOException {
        Tika tika = new Tika();
        String mimeType = tika.detect(inputFile);
        if (!mimeType.startsWith("image/")) {
            throw new IllegalArgumentException("Input file is not an image");
        }
    }


}
