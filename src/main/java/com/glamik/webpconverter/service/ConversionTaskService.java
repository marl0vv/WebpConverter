package com.glamik.webpconverter.service;

import com.glamik.webpconverter.enums.ConversionTaskStatus;
import com.glamik.webpconverter.enums.ErrorMessage;
import com.glamik.webpconverter.model.ConversionTask;
import com.glamik.webpconverter.repository.ConversionTaskRepository;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConversionTaskService {

    private final ConversionTaskRepository conversionTaskRepository;

    @Value("${deletion.time.minutes:1}")
    private int deletionTimeMinutes;

    @Transactional
    public ConversionTask saveConversionTask(@NonNull String originalFileName, @NonNull String filesystemName) {
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
    public void setConversionSuccessStatus(@NonNull UUID id, @NonNull ConversionTaskStatus status, @NonNull String convertedName) {
        ConversionTask task = conversionTaskRepository.getById(id);
        task.setStatus(status);
        task.setConvertedName(convertedName);
        task.setTaskProcessingDate(LocalDateTime.now());
        conversionTaskRepository.save(task);
    }

    @Transactional
    public void setConversionErrorStatus(@NonNull UUID id, @NonNull ConversionTaskStatus status, ErrorMessage errorMessage) {
        ConversionTask task = conversionTaskRepository.getById(id);
        task.setStatus(status);
        task.setErrorMessage(errorMessage);
        conversionTaskRepository.save(task);
    }

    @Transactional
    public void setConversionDeletedStatus(@NonNull UUID id, @NonNull ConversionTaskStatus status) {
        Optional<ConversionTask> task = conversionTaskRepository.findById(id);
        task.ifPresent(conversionTask -> conversionTask.setStatus(ConversionTaskStatus.DELETED));
        conversionTaskRepository.save(task.get());
    }
    @Transactional
    public ConversionTask getConversionTask(@NonNull UUID id) {
        return conversionTaskRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Couldn't find conversion task with id: " + id));
    }

    @Transactional
    public List<ConversionTask> getPendingConversionTasks() {
        return conversionTaskRepository.findByStatusOrderByTaskCreationDate(ConversionTaskStatus.PENDING);
    }

    @Transactional
    public List<ConversionTask> getSuccessConversionTasksForDeletion() {
        return conversionTaskRepository.findTasksForDeletionNative(deletionTimeMinutes);
    }
}
