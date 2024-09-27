package com.glamik.webpconverter.controllers;

import com.glamik.webpconverter.service.ConverterService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.io.InputStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ConverterControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ConverterController converterController;

    @MockBean
    private ConverterService converterService;

    @Test
    void testConvertImageSuccess() throws Exception {
        byte[] sampleWebpBytes = {1, 2, 3, 4};
        when(converterService.convertToWebp(any(InputStream.class))).thenReturn(sampleWebpBytes);

        MockMultipartFile mockMultipartFile = new MockMultipartFile(
                "image",
                "test-image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "Test Image Content".getBytes()
        );

        mockMvc.perform(multipart("/convert-to-webp")
                        .file(mockMultipartFile))
                .andExpect(status().isOk())
                .andExpect(content().bytes(sampleWebpBytes));
    }

    @Test
    void testConvertImageFailure() throws Exception {
        when(converterService.convertToWebp(any(InputStream.class))).thenThrow(new IOException("Conversion error"));

        MockMultipartFile mockMultipartFile = new MockMultipartFile(
                "image",
                "test-image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "Test Image Content".getBytes()
        );

        mockMvc.perform(multipart("/convert-to-webp")
                        .file(mockMultipartFile))
                .andExpect(status().isInternalServerError());
    }
}