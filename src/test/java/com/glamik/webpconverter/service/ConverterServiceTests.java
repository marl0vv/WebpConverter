package com.glamik.webpconverter.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.ClassPathResource;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConverterServiceTests {

    private ConverterService converterService = new ConverterService();

    @TempDir
    Path tempDir;

    @Test
    void testSuccessfulConversion() throws IOException {
        Path resource = new ClassPathResource("/test-image.jpg").getFile().toPath();
        Path outputPath = converterService.convertToWebp(resource);

        assertThat(outputPath).isNotEmptyFile();

        InputStream convertedStream = new ClassPathResource("/test-image.webp").getInputStream();
        byte[] convertedBytes = convertedStream.readAllBytes();
        InputStream referenceStream = new ClassPathResource("/test-image-reference.webp").getInputStream();
        byte[] referenceBytes = referenceStream.readAllBytes();
        assertThat(convertedBytes).isEqualTo(referenceBytes);
    }

    @Test
    void testWrongInput() {
        Path inputPath = Path.of("/testWrongPath");

        assertThatThrownBy(() -> converterService.convertToWebp(inputPath))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("Can't read input file!");
    }

    @Test
    void testNullInput() {
        assertThatThrownBy(() -> converterService.convertToWebp(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void testEmptyInput() throws IOException {
        Path emptyFile = tempDir.resolve("empty.jpg");
        Files.createFile(emptyFile);
        Files.write(emptyFile, new byte[0]);

        assertThatThrownBy(() -> converterService.convertToWebp(emptyFile))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("Invalid image file: " + emptyFile.getFileName());
    }
}
