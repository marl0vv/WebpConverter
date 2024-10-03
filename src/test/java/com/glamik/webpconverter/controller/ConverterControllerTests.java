package com.glamik.webpconverter.controller;

import com.glamik.webpconverter.service.ConverterService;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.PathResource;
import org.springframework.http.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.MimeType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class ConverterControllerTests {

    private final ConverterService converterService = Mockito.mock(ConverterService.class);

    private final ConverterController converterController = new ConverterController(converterService);

    @Test
    void testConvertImageSuccess() throws Exception {
        // Arrange
        File convertedFile = new ClassPathResource("/test-image-reference.webp").getFile();
        File tempInputFile = File.createTempFile("input-", ".webp");
        IOUtils.copy(convertedFile.toURI().toURL(), tempInputFile);

        when(converterService.convertToWebp(any(File.class))).thenReturn(tempInputFile);
        MockMultipartFile mockMultipartFile = new MockMultipartFile(
                "image",
                "test-image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "Test Image Content".getBytes()
        );

        // Act
        ResponseEntity<PathResource> response = converterController.convertImage(mockMultipartFile);

        // Assert
        ContentDisposition contentDisposition = response.getHeaders().getContentDisposition();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.asMediaType(MimeType.valueOf("image/webp")));
        assertThat(contentDisposition.getType()).isEqualTo("attachment");
        assertThat(contentDisposition.getFilename()).isEqualTo("test-image.webp");
        assertThat(response.getBody()).isNotNull();

        Path actualPath = Paths.get(response.getBody().getPath());
        Path expectedPath = tempInputFile.toPath();
        assertThat(actualPath).isEqualTo(expectedPath);
    }

    @Test
    void testConvertImageFailure() throws Exception {
        // Arrange
        when(converterService.convertToWebp(any(File.class))).thenThrow(new IOException("Conversion error"));

        MockMultipartFile mockMultipartFile = new MockMultipartFile(
                "image",
                "test-image.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "Test Image Content".getBytes()
        );

        // Act
        ResponseEntity<PathResource> response = converterController.convertImage(mockMultipartFile);

        //Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void testConvertWrongInput() throws Exception {
        // Arrange
        when(converterService.convertToWebp(any(File.class))).thenThrow(new IllegalArgumentException("Input file is not an image"));

        MockMultipartFile mockMultipartFile = new MockMultipartFile(
                "image",
                "test-image.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "Test Image Content".getBytes()
        );

        // Act
        ResponseEntity<PathResource> response = converterController.convertImage(mockMultipartFile);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

}