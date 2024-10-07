package com.glamik.webpconverter.scheduler;

import com.glamik.webpconverter.command.ProcessConversionTaskCommand;
import com.glamik.webpconverter.model.ConversionTask;

import com.glamik.webpconverter.service.ConversionTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ConversionTaskProcessingScheduler {

    private final ConversionTaskService conversionTaskService;
    private final ProcessConversionTaskCommand processConversionTaskCommand;

    @Scheduled(fixedDelay = 10000)
    public void processPendingConversionTasks() {
        List<ConversionTask> pendingTasks = conversionTaskService.getPendingConversionTasks();
        for (ConversionTask task : pendingTasks) {
            processConversionTaskCommand.execute(task);
        }
    }

}
