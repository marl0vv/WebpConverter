package com.glamik.webpconverter.scheduler;

import com.glamik.webpconverter.exception.FileDeleteException;
import com.glamik.webpconverter.model.ConversionTask;
import com.glamik.webpconverter.service.ConversionTaskService;
import com.glamik.webpconverter.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.List;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "deletion.time.millis")
@Slf4j
public class ConversionTaskDeletionScheduler {

    private final ConversionTaskService conversionTaskService;
    private final FileService fileService;

    @Scheduled(initialDelayString = "${deletion.time.millis}", fixedDelayString = "${deletion.time.millis}")
    public void deleteOldConvertedImages() {

        List<ConversionTask> pendingTasks = conversionTaskService.getSuccessConversionTasksForDeletion();
        for (ConversionTask task : pendingTasks) {
            File convertedFile = fileService.getOutputFile(task.getConvertedName());
            try {
                Files.delete(convertedFile.toPath());
            } catch (NoSuchFileException e) {
                log.warn("File not found, might have already been deleted: {}", convertedFile.getAbsolutePath(), e);
            } catch (IOException e) {
                log.error("Failed to delete file: {}", convertedFile.getAbsolutePath(), e);
            }
        }
    }

}
