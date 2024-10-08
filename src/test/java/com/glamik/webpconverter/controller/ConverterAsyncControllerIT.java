package com.glamik.webpconverter.controller;

import com.glamik.webpconverter.SpringBootApplicationTest;
import com.glamik.webpconverter.enums.ConversionTaskStatus;
import com.glamik.webpconverter.model.ConversionTask;
import com.glamik.webpconverter.repository.ConversionTaskRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ConverterAsyncControllerIT extends SpringBootApplicationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ConversionTaskRepository conversionTaskRepository;

    @Test
    void addConversionTaskOk() throws Exception {
        // Arrange
        MockMultipartFile multipartFile = new MockMultipartFile(
                "image",
                "test-image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                new ClassPathResource("test-image.jpg").getInputStream()
        );

        // Act & Assert
        MvcResult result = mockMvc.perform(multipart("/convert-to-webp/async")
                        .file(multipartFile))
                .andExpect(status().isOk())
                .andExpect(content().string(matchesPattern("^\"[0-9a-fA-F-]{36}\"$")))
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        String taskId = responseContent.replace("\"", "");
        UUID uuid = UUID.fromString(taskId);

        Optional<ConversionTask> taskOptional = Optional.ofNullable(conversionTaskRepository.getById(uuid));
        assertThat(taskOptional).isPresent();

        ConversionTask task = taskOptional.get();
        assertThat(task.getStatus()).isEqualTo(ConversionTaskStatus.PENDING);
        assertThat(task.getOriginalName()).isEqualTo("test-image");
        assertThat(task.getTaskCreationDate()).isNotNull();
    }

}
