package com.glamik.webpconverter.controller;

import com.glamik.webpconverter.BaseSpringBootApplicationTest;
import com.glamik.webpconverter.enums.ConversionTaskStatus;
import com.glamik.webpconverter.model.ConversionTask;
import com.glamik.webpconverter.repository.ConversionTaskRepository;
import com.glamik.webpconverter.service.FileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ConverterAsyncControllerIT extends BaseSpringBootApplicationTest {

    private static final String REQUEST_URL = "/convert-to-webp/async";
    private static final String IMAGE_FIELD_NAME = "image";
    private static final String TEST_IMAGE_NAME = "test-image.jpg";
    private static final String IMAGE_MEDIA_TYPE = MediaType.IMAGE_JPEG_VALUE;
    private static final String UUID_PATTERN = "^\"[0-9a-fA-F-]{36}\"$";

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ConversionTaskRepository conversionTaskRepository;

    @Autowired
    FileService fileService;

    private MockMultipartFile createMockMultipartFileFromResource() throws IOException {
        return new MockMultipartFile(
                IMAGE_FIELD_NAME,
                TEST_IMAGE_NAME,
                IMAGE_MEDIA_TYPE,
                new ClassPathResource(TEST_IMAGE_NAME).getInputStream()
        );
    }

    private UUID conversionTaskRequest(MockMultipartFile mockMultipartFile) throws Exception {
        String responseContent = mockMvc.perform(multipart(REQUEST_URL)
                        .file(mockMultipartFile))
                .andExpect(status().isOk())
                .andExpect(content().string(matchesPattern(UUID_PATTERN)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        return UUID.fromString(responseContent.replace("\"", ""));
    }

    private void assertTaskCommonProperties(ConversionTask task, ConversionTaskStatus expectedStatus) {
        assertThat(task).isNotNull();
        assertThat(task.getStatus()).isEqualTo(expectedStatus);
        assertThat(task.getOriginalName()).isEqualTo("test-image");
        assertThat(task.getTaskCreationDate()).isNotNull();

        if (expectedStatus == ConversionTaskStatus.SUCCESS) {
            assertThat(task.getTaskProcessingDate()).isNotNull();
        }
    }

    private void assertThatFileCreatedOnDisc(String filesystemName) {
        File inputFile = fileService.getInputFile(filesystemName);
        assertThat(inputFile).isNotNull();
    }

    @Test
    void addConversionTaskOk() throws Exception {
        // Arrange
        MockMultipartFile multipartFile = createMockMultipartFileFromResource();

        // Act & Assert
        UUID uuid = conversionTaskRequest(multipartFile);

        ConversionTask task = conversionTaskRepository.getById(uuid);
        assertTaskCommonProperties(task, ConversionTaskStatus.PENDING);
        assertThatFileCreatedOnDisc(task.getFilesystemName());
    }

}
