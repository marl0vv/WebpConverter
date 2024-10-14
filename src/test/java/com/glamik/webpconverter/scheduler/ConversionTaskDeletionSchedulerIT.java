package com.glamik.webpconverter.scheduler;

import com.github.database.rider.core.api.dataset.DataSet;
import com.glamik.webpconverter.BaseSpringBootApplicationTest;
import com.glamik.webpconverter.enums.ConversionTaskStatus;
import com.glamik.webpconverter.model.ConversionTask;
import com.glamik.webpconverter.repository.ConversionTaskRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.TestPropertySource;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.awaitility.Awaitility.await;

@TestPropertySource(properties = {"processing.time.millis=3000", "deletion.time.minutes=1", "deletion.time.millis=5000"})
class ConversionTaskDeletionSchedulerIT extends BaseSpringBootApplicationTest {

    @Autowired
    ConversionTaskRepository conversionTaskRepository;

    @Test
    @DataSet(value = "example-data-single-for-deletion.json", cleanAfter = true, cleanBefore = true)
    void fileDeletionOk(@Value("${base.directory}") String programDir) throws Exception {
        // Arrange
        File outFolder = new File(programDir, "out");

        File resource = new ClassPathResource("test-image.jpg").getFile();
        File convertedFile = new File(outFolder, "output-38968635-4feb-4d22-9503-06136521df3a.webp");
        Files.copy(resource.toPath(), convertedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        // Act & Assert
        assertThat(convertedFile).exists();

        await().atMost(15, TimeUnit.SECONDS).until(() -> !convertedFile.exists());

        assertThat(convertedFile).doesNotExist();

        Optional<ConversionTask> task = conversionTaskRepository.findById(UUID.fromString("607c09c6-3032-4711-a018-118d8f709c8c"));
        assertThat(task.get().getStatus()).isEqualTo(ConversionTaskStatus.DELETED);
    }

}
