package com.glamik.webpconverter.scheduler;

import com.glamik.webpconverter.enums.ConversionTaskStatus;
import com.glamik.webpconverter.model.ConversionTask;
import com.glamik.webpconverter.repository.ConversionTaskRepository;
import com.glamik.webpconverter.service.ConversionTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConversionTaskScheduler {

    private final ConversionTaskRepository conversionTaskRepository;
    private final ConversionTaskService conversionTaskService;

    @Scheduled(fixedDelay = 10000)
    public void processPendingConversionTasks() throws IOException {
        List<ConversionTask> pendingTasks = conversionTaskRepository.findByStatus(ConversionTaskStatus.PENDING);
        for (ConversionTask task : pendingTasks) {
            conversionTaskService.processConversionTask(task);
        }
    }

}
