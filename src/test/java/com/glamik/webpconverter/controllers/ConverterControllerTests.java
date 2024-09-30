package com.glamik.webpconverter.controllers;

import com.glamik.webpconverter.service.ConverterService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.io.PathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class ConverterControllerTests {

    private ConverterService converterService = Mockito.mock(ConverterService.class);

    private ConverterController converterController = new ConverterController(converterService);

    @Test
    void testConvertImageSuccess() throws Exception {
        // Arrange
        MockMultipartFile mockMultipartFile = new MockMultipartFile(
                "image",
                "test-image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "Test Image Content".getBytes()
        );

        // Act
        ResponseEntity<PathResource> response = converterController.convertImage(mockMultipartFile);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
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