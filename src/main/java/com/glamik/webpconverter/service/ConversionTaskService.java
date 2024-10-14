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
import java.util.UUID;

/**
 * Service that handles what is written or changed about conversion tasks in a repository.
 */
@Service
@RequiredArgsConstructor
public class ConversionTaskService {

    private static final String COULD_NOT_FIND_ID_EXCEPTION_MESSAGE = "Couldn't find conversion task with id: ";
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

        return conversionTaskRepository.save(conversionTask);
    }

    @Transactional
    public void setConversionSuccessStatus(@NonNull UUID id, @NonNull String convertedName) {
        ConversionTask task = conversionTaskRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(COULD_NOT_FIND_ID_EXCEPTION_MESSAGE + id));

        task.setStatus(ConversionTaskStatus.SUCCESS);
        task.setConvertedName(convertedName);
        task.setTaskProcessingDate(LocalDateTime.now());
    }

    @Transactional
    public void setConversionErrorStatus(@NonNull UUID id, ErrorMessage errorMessage) {
        ConversionTask task = conversionTaskRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(COULD_NOT_FIND_ID_EXCEPTION_MESSAGE + id));

        task.setStatus(ConversionTaskStatus.ERROR);
        task.setErrorMessage(errorMessage);
    }

    @Transactional
    public void setConversionDeletedStatus(@NonNull UUID id) {
        ConversionTask conversionTask = conversionTaskRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(COULD_NOT_FIND_ID_EXCEPTION_MESSAGE + id));

        conversionTask.setStatus(ConversionTaskStatus.DELETED);
    }

    @Transactional
    public ConversionTask getConversionTask(@NonNull UUID id) {
        return conversionTaskRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(COULD_NOT_FIND_ID_EXCEPTION_MESSAGE + id));
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
