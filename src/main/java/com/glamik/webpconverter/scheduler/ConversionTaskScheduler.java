package com.glamik.webpconverter.scheduler;

import com.glamik.webpconverter.command.ProcessConversionTaskCommand;
import com.glamik.webpconverter.enums.ConversionTaskStatus;
import com.glamik.webpconverter.model.ConversionTask;
import com.glamik.webpconverter.repository.ConversionTaskRepository;

import com.glamik.webpconverter.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ConversionTaskScheduler {

    private final ConversionTaskRepository conversionTaskRepository;
    private final ProcessConversionTaskCommand processConversionTaskCommand;
    private final FileService fileService;

    @Value("${deletion.time.millis}")
    private long deletionTimeMillis;

    @Scheduled(fixedDelay = 10000)
    public void processPendingConversionTasks() {
        List<ConversionTask> pendingTasks = conversionTaskRepository.findByStatusOrderByTaskCreationDate(ConversionTaskStatus.PENDING);
        for (ConversionTask task : pendingTasks) {
            processConversionTaskCommand.execute(task);
        }
    }


    @Scheduled(initialDelayString = "${deletion.time.millis}", fixedDelayString = "${deletion.time.millis}")
    public void deleteOldConvertedImages() {
        List<ConversionTask> pendingTasks = conversionTaskRepository.findByStatusOrderByTaskCreationDate(ConversionTaskStatus.SUCCESS);

        for (ConversionTask task : pendingTasks) {
            long timeBetween = Duration.between(task.getTaskCreationDate(), LocalDateTime.now()).toMillis();
            if (timeBetween >= deletionTimeMillis) {
                File convertedFile = fileService.getOutputFile(task.getConvertedName());
                convertedFile.delete();
            }
        }
    }
}
