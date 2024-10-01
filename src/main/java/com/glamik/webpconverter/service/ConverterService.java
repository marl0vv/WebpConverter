package com.glamik.webpconverter.service;

import com.glamik.webpconverter.enums.ConversionTaskStatus;
import com.glamik.webpconverter.model.ConversionTask;
import com.glamik.webpconverter.repository.ConversionTaskRepository;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
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
    private final ConversionTaskRepository conversionTaskRepository;

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

    public ConversionTask saveConversionTask(String filename) {
        return conversionTaskRepository
                .save(new ConversionTask(ConversionTaskStatus.PENDING, filename));
    }

    private void checkInputMimeType(File inputFile) throws IOException {
        String mimeType = TIKA.detect(inputFile);
        if (!mimeType.startsWith("image/")) {
            throw new IllegalArgumentException("Input file is not an image");
        }
    }

}
