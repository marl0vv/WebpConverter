package com.glamik.webpconverter.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
public class ConverterServiceTests {

    @Autowired
    private ConverterService converterService;

    @Test
    void testSuccessfulConversion() throws IOException {
        InputStream inputStream = Files.newInputStream(Path.of("src/test/resources/test-image.jpg"));
        byte[] webpBytes = converterService.convertToWebp(inputStream);

        assertThat(webpBytes).isNotNull().isNotEmpty();

        BufferedImage image = ImageIO.read(new ByteArrayInputStream(webpBytes));
        assertThat(image).isNotNull();
    }

    @Test
    void testWrongInput() {
        InputStream inputStream = new ByteArrayInputStream("data".getBytes());

        assertThatThrownBy(() -> converterService.convertToWebp(inputStream))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("image == null");
    }

    @Test
    void testNullInput() {
        assertThatThrownBy(() -> converterService.convertToWebp(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testEmptyInput() {
        InputStream inputStream = new ByteArrayInputStream(new byte[0]);
        assertThatThrownBy(() -> converterService.convertToWebp(inputStream))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
