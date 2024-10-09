package com.glamik.webpconverter.controller;

import com.glamik.webpconverter.BaseSpringBootApplicationTest;
import com.glamik.webpconverter.enums.ConversionTaskStatus;
import com.glamik.webpconverter.enums.ErrorMessage;
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
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ConverterAsyncControllerIT extends BaseSpringBootApplicationTest {

    private static final String REQUEST_URL = "/convert-to-webp/async";
    private static final String IMAGE_FIELD_NAME = "image";
    private static final String TEST_IMAGE_NAME = "test-image.jpg";
    private static final String IMAGE_MEDIA_TYPE = MediaType.IMAGE_JPEG_VALUE;
    private static final String TEST_TEXT_NAME = "test-image.txt";
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

    private MockMultipartFile createMockMultipartFile(String filename, MediaType mediaType, byte[] content) {
        return new MockMultipartFile(
                IMAGE_FIELD_NAME,
                filename,
                mediaType.toString(),
                content
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

    private ConversionTask waitForTaskConversion(UUID uuid, ConversionTaskStatus expectedStatus) {
        await().atMost(20, TimeUnit.SECONDS).until(() -> {
            ConversionTask task = conversionTaskRepository.getById(uuid);
            return task != null && task.getStatus() == expectedStatus;
        });

        ConversionTask task = conversionTaskRepository.getById(uuid);
        assertThat(task).isNotNull();
        return task;
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

    /* Я написал эти тесты, а сейчас думаю, что они же не контроллер проверяют,
        а планировщик задач, потому что выжидают время, пока планировщик поменяет записи в БД.
        Пока что оставлю так, возможно, нужно вынести в отдельный файл.
     */
    @Test
    void processConversionTaskSuccess() throws Exception {
        // Arrange
        MockMultipartFile multipartFile = createMockMultipartFileFromResource();

        // Act & Assert
        UUID uuid = conversionTaskRequest(multipartFile);

        ConversionTask task = waitForTaskConversion(uuid, ConversionTaskStatus.SUCCESS);
        assertTaskCommonProperties(task, ConversionTaskStatus.SUCCESS);
        assertThatFileCreatedOnDisc(task.getFilesystemName());
    }

    @Test
    void processConversionTaskErrorNotAnImage() throws Exception {
        // Arrange
        MockMultipartFile mockMultipartFile = createMockMultipartFile(TEST_TEXT_NAME, MediaType.TEXT_PLAIN, "Test Image Content".getBytes());

        // Act & Assert
        UUID uuid = conversionTaskRequest(mockMultipartFile);

        ConversionTask task = waitForTaskConversion(uuid, ConversionTaskStatus.ERROR);
        assertTaskCommonProperties(task, ConversionTaskStatus.ERROR);
        assertThat(task.getErrorMessage()).isEqualTo(ErrorMessage.INPUT_FILE_IS_NOT_AN_IMAGE);
        assertThatFileCreatedOnDisc(task.getFilesystemName());
    }

    @Test
    void processConversionTaskErrorNullOrCorrupted() throws Exception {
        // Arrange
        MockMultipartFile mockMultipartFile = createMockMultipartFile(TEST_IMAGE_NAME, MediaType.IMAGE_JPEG, null);

        // Act & Assert
        UUID uuid = conversionTaskRequest(mockMultipartFile);

        ConversionTask task = waitForTaskConversion(uuid, ConversionTaskStatus.ERROR);
        assertTaskCommonProperties(task, ConversionTaskStatus.ERROR);
        assertThat(task.getErrorMessage()).isEqualTo(ErrorMessage.INPUT_FILE_IS_NULL_OR_CORRUPTED);
        assertThatFileCreatedOnDisc(task.getFilesystemName());
    }

}
