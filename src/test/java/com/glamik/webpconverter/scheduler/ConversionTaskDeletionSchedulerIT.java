package com.glamik.webpconverter.scheduler;

import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.junit5.api.DBRider;
import com.glamik.webpconverter.BaseSpringBootApplicationTest;
import com.glamik.webpconverter.model.ConversionTask;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.awaitility.Awaitility.await;

@DirtiesContext
@DBRider
@DBUnit(caseSensitiveTableNames = true)
@TestPropertySource(properties = {"processing.time.millis=3000", "deletion.time.minutes=1", "deletion.time.millis=1000"})
class ConversionTaskDeletionSchedulerIT extends BaseSpringBootApplicationTest {

    @Test
    @DataSet(value = "example-data-single-for-deletion.json", cleanAfter = true, cleanBefore = true)
    void fileDeletionOk(@Value("${base.directory}") String programDir) throws Exception {
        // Arrange

        File outFolder = new File(programDir, "out");

        File resource = new ClassPathResource("test-image.jpg").getFile();
        File convertedFile = new File(outFolder, "output-38968635-4feb-4d22-9503-06136521df3a.webp");
        Files.copy(resource.toPath(), convertedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        // Act & Assert
        assertThat(convertedFile.exists()).isTrue();

        await().atMost(15, TimeUnit.SECONDS).until(() -> !convertedFile.exists());

        assertThat(convertedFile.exists()).isFalse();
    }

}
