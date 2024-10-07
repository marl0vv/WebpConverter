package com.glamik.webpconverter.service;

import com.glamik.webpconverter.enums.ConversionTaskStatus;
import com.glamik.webpconverter.enums.ErrorMessage;
import com.glamik.webpconverter.model.ConversionTask;
import com.glamik.webpconverter.repository.ConversionTaskRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConversionTaskService {

    private final ConversionTaskRepository conversionTaskRepository;

    @Value("${deletion.time.minutes}")
    private int deletionTimeMinutes;


    @Transactional
    public ConversionTask saveConversionTask(String originalFileName, String filesystemName) {
        ConversionTask conversionTask = ConversionTask.builder()
                .status(ConversionTaskStatus.PENDING)
                .originalName(originalFileName)
                .filesystemName(filesystemName)
                .taskCreationDate(LocalDateTime.now())
                .build();

        return conversionTaskRepository
                .save(conversionTask);
    }

    @Transactional
    public void setConversionSuccessStatus(UUID id, ConversionTaskStatus status, String convertedName) {
        ConversionTask task = conversionTaskRepository.getById(id);
        task.setStatus(status);
        task.setConvertedName(convertedName);
        task.setTaskProcessingDate(LocalDateTime.now());
        conversionTaskRepository.save(task);
    }

    @Transactional
    public void setConversionErrorStatus(UUID id, ConversionTaskStatus status, ErrorMessage errorMessage) {
        ConversionTask task = conversionTaskRepository.getById(id);
        task.setStatus(status);
        task.setErrorMessage(errorMessage);
        conversionTaskRepository.save(task);
    }


    public ConversionTask getConversionTask(UUID id) {
        return conversionTaskRepository.getById(id);
    }

    public List<ConversionTask> getPendingConversionTasks() {
        return conversionTaskRepository.findByStatusOrderByTaskCreationDate(ConversionTaskStatus.PENDING);
    }

    public List<ConversionTask> getSuccessConversionTasksForDeletion() {
        return conversionTaskRepository.findTasksForDeletionNative(deletionTimeMinutes);
    }


}
