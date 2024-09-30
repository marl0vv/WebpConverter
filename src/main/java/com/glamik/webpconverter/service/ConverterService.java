package com.glamik.webpconverter.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class ConverterService {

    private static final Tika TIKA = new Tika();

    public File convertToWebp(@NonNull File inputFile) throws IOException {
        checkInputMimeType(inputFile);
        File outputFile = File.createTempFile("ConvertedTempImage-", ".webp");

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
        String mimeType = TIKA.detect(inputFile);
        if (!mimeType.startsWith("image/")) {
            throw new IllegalArgumentException("Input file is not an image");
        }
    }


}
