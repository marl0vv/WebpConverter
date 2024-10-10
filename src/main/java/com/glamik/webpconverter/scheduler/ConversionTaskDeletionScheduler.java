package com.glamik.webpconverter.scheduler;

import com.glamik.webpconverter.model.ConversionTask;
import com.glamik.webpconverter.service.ConversionTaskService;
import com.glamik.webpconverter.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "processing.time.millis")
public class ConversionTaskDeletionScheduler {

    private final ConversionTaskService conversionTaskService;
    private final FileService fileService;

    @Scheduled(initialDelayString = "${deletion.time.millis}", fixedDelayString = "${deletion.time.millis}")
    public void deleteOldConvertedImages() {

        List<ConversionTask> pendingTasks = conversionTaskService.getSuccessConversionTasksForDeletion();
        for (ConversionTask task : pendingTasks) {
            File convertedFile = fileService.getOutputFile(task.getConvertedName());
            convertedFile.delete();
        }
    }

}
