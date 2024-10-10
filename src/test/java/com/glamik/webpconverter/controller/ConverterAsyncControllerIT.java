package com.glamik.webpconverter.controller;

import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.junit5.api.DBRider;
import com.glamik.webpconverter.BaseSpringBootApplicationTest;
import com.glamik.webpconverter.enums.ConversionTaskStatus;
import com.glamik.webpconverter.enums.ErrorMessage;
import com.glamik.webpconverter.model.ConversionTask;
import com.glamik.webpconverter.repository.ConversionTaskRepository;
import com.glamik.webpconverter.service.FileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.MimeType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DBRider
@DBUnit(caseSensitiveTableNames = true)
class ConverterAsyncControllerIT extends BaseSpringBootApplicationTest {

    private static final String CONVERT_TO_WEBP_ASYNC_URL = "/convert-to-webp/async";
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
        String responseContent = mockMvc.perform(multipart(CONVERT_TO_WEBP_ASYNC_URL)
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

    private void getStatusRequest(UUID exampleId, ConversionTaskStatus expectedStatus, ErrorMessage expectedErrorMessage) throws Exception {
        var resultActions = mockMvc.perform(get("/convert-to-webp/async/{taskId}/status", exampleId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.status").value(expectedStatus.toString()));

        if (expectedErrorMessage != null) {
            resultActions.andExpect(jsonPath("$.errorMessage").value(expectedErrorMessage.toString()));
        } else {
            resultActions.andExpect(jsonPath("$.errorMessage").isEmpty());
        }
    }

    @Test
    @DataSet(value = "example-data-single-success.json", cleanAfter = true, cleanBefore = true)
    void getConversionTaskStatusSuccess() throws Exception {
        // Arrange
        UUID exampleId = UUID.fromString("607c09c6-3032-4711-a018-118d8f709c8c");

        // Act & Assert
        getStatusRequest(exampleId, ConversionTaskStatus.SUCCESS, null);
    }

    @Test
    void getConversionTaskStatusNotFound() throws Exception {
        // Arrange
        UUID exampleId = UUID.fromString("a18bd95f-3983-4de7-97b5-f340e5f5aadd");

        // Act & Assert
        mockMvc.perform(get("/convert-to-webp/async/{taskId}/status", exampleId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DataSet(value = "example-data-single-error-not-image.json", cleanAfter = true, cleanBefore = true)
    void getConversionTaskStatusErrorNotAnImage() throws Exception {
        // Arrange
        UUID exampleId = UUID.fromString("718091d1-70c6-43df-b8ce-fb0eaf6fcf30");

        // Act & Assert
        getStatusRequest(exampleId, ConversionTaskStatus.ERROR, ErrorMessage.INPUT_FILE_IS_NOT_AN_IMAGE);
    }

    @Test
    @DataSet(value = "example-data-single-error-null.json", cleanAfter = true, cleanBefore = true)
    void getConversionTaskStatusErrorNullOrCorrupted() throws Exception {
        // Arrange
        UUID exampleId = UUID.fromString("718091d1-70c6-43df-b8ce-fb0eaf6fcf30");

        // Act & Assert
        getStatusRequest(exampleId, ConversionTaskStatus.ERROR, ErrorMessage.INPUT_FILE_IS_NULL_OR_CORRUPTED);
    }


    @Test
    @DataSet(value = "example-data-single-success.json", cleanAfter = true, cleanBefore = true)
    void getConvertedImageSuccess(@Value("${base.directory}") String programDir) throws Exception {
        // Arrange
        UUID exampleId = UUID.fromString("607c09c6-3032-4711-a018-118d8f709c8c");

        File outFolder = new File(programDir, "out");

        File resource = new ClassPathResource("test-image.jpg").getFile();
        File convertedFile = new File(outFolder, "output-38968635-4feb-4d22-9503-06136521df3a.webp");
        Files.copy(resource.toPath(), convertedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        // Act & Assert
        var response = mockMvc.perform(get("/convert-to-webp/async/{taskId}", exampleId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.asMediaType(MimeType.valueOf("image/webp"))))
                .andReturn()
                .getResponse();

        String contentDisposition = response.getHeader("Content-Disposition");
        assertThat(contentDisposition).isNotEmpty().contains("attachment;").contains("test-image.webp");

    }

    @Test
    @DataSet(value = "example-data-single-error-not-image.json", cleanAfter = true, cleanBefore = true)
    void getConvertedImageError() throws Exception {
        // Arrange
        UUID exampleId = UUID.fromString("718091d1-70c6-43df-b8ce-fb0eaf6fcf30");

        // Act & Assert
        mockMvc.perform(get("/convert-to-webp/async/{taskId}", exampleId))
                .andExpect(status().isNotFound());
    }
}
