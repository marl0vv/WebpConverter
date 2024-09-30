package com.glamik.webpconverter.service;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConverterServiceTests {
    private final ConverterService converterService = new ConverterService();

    @Test
    void testSuccessfulConversion() throws IOException {
        File resource = new ClassPathResource("/test-image.jpg").getFile();
        File outputFile = converterService.convertToWebp(resource);

        assertThat(outputFile).isNotEmpty();

        InputStream convertedStream = new ClassPathResource("/test-image.webp").getInputStream();
        byte[] convertedBytes = convertedStream.readAllBytes();
        InputStream referenceStream = new ClassPathResource("/test-image-reference.webp").getInputStream();
        byte[] referenceBytes = referenceStream.readAllBytes();
        assertThat(convertedBytes).isEqualTo(referenceBytes);
    }

    @Test
    void testWrongInput() {
        File inputFile = new File("/testWrongPath");

        assertThatThrownBy(() -> converterService.convertToWebp(inputFile))
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
        File emptyFile = new File("empty.jpg");

        Files.write(emptyFile.toPath(), new byte[0]);

        assertThatThrownBy(() -> converterService.convertToWebp(emptyFile))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("Invalid image file: " + emptyFile.getName());
    }
}
