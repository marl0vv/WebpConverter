package com.glamik.webpconverter.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConverterServiceTests {

    private final ConverterService converterService = new ConverterService();

    @Test
    void testSuccessfulConversion() throws IOException {
        // Arrange
        File resource = new ClassPathResource("test-image.jpg").getFile();
        // Act
        File outputFile = converterService.convertToWebp(resource);

        // Assert
        assertThat(outputFile).isNotEmpty();

        InputStream convertedStream = new FileInputStream(outputFile);
        InputStream referenceStream = new ClassPathResource("test-image-reference.webp").getInputStream();

        byte[] convertedBytes = convertedStream.readAllBytes();
        byte[] referenceBytes = referenceStream.readAllBytes();

        assertThat(convertedBytes).isEqualTo(referenceBytes);
    }

    @Test
    void testWrongInput() {
        //Arrange
        File inputFile = new File("/testWrongPath");

        // Act & Assert
        assertThatThrownBy(() -> converterService.convertToWebp(inputFile))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("The system cannot find the file specified");
    }

    @Test
    void testNullInput() {
        // Act & Assert
        assertThatThrownBy(() -> converterService.convertToWebp(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void testEmptyInput(@TempDir Path tempDir) throws IOException {
        // Arrange
        //File emptyFile = new File("empty.jpg");
        File emptyFile = tempDir.resolve("empty.jpg").toFile();
        Files.write(emptyFile.toPath(), new byte[0]);

        // Act & Assert
        assertThatThrownBy(() -> converterService.convertToWebp(emptyFile))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("Failed to process the image. The file '" + emptyFile.getName() + "' is not a valid (may be null) or could not be read.");
    }
}
