package com.glamik.webpconverter.scheduler;

import com.glamik.webpconverter.command.ProcessConversionTaskCommand;
import com.glamik.webpconverter.enums.ConversionTaskStatus;
import com.glamik.webpconverter.model.ConversionTask;
import com.glamik.webpconverter.repository.ConversionTaskRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ConversionTaskScheduler {

    private final ConversionTaskRepository conversionTaskRepository;
    private final ProcessConversionTaskCommand processConversionTaskCommand;

    @Scheduled(fixedDelay = 10000)
    public void processPendingConversionTasks() {
        List<ConversionTask> pendingTasks = conversionTaskRepository.findByStatus(ConversionTaskStatus.PENDING);
        for (ConversionTask task : pendingTasks) {
            processConversionTaskCommand.execute(task);
        }
    }

}
