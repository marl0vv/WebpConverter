package com.glamik.webpconverter.service;

import com.glamik.webpconverter.enums.ConversionTaskStatus;
import com.glamik.webpconverter.enums.ErrorMessage;
import com.glamik.webpconverter.model.ConversionTask;
import com.glamik.webpconverter.repository.ConversionTaskRepository;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * Service responsible for managing conversion tasks within the WebP converter application.
 * <p>
 * This service handles the creation, status updates, retrieval, and deletion of conversion tasks.
 * It interacts with he {@link ConversionTaskRepository} to perform CRUD operations on conversion tasks.
 * </p>
 * <p>
 * Configuration properties:
 * <ul>
 *     <li><b>deletion.time.minutes</b> - Time in minutes after which successful conversion tasks are eligible for deletion. Default is 1 minute</li>
 * </ul>
 * </p>
 *
 * @see ConversionTask
 * @see ConversionTaskRepository
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ConversionTaskService {

    private static final String COULD_NOT_FIND_ID_EXCEPTION_MESSAGE = "Couldn't find conversion task with id: ";

    private final ConversionTaskRepository conversionTaskRepository;

    @Value("${deletion.time.minutes:1}")
    private int deletionTimeMinutes;

    /**
     * Retrieves a conversion task by its unique identifier. Used only within this class
     *
     * @param id the unique identifier of the conversion task
     * @return {@link ConversionTask} entity with the specified ID
     * @throws NoSuchElementException if no conversion task with the given {@code id} is found
     */
    private ConversionTask getConversionTaskById(UUID id) {
        return conversionTaskRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("ConversionTask with ID {} not found", id);
                    return new NoSuchElementException(COULD_NOT_FIND_ID_EXCEPTION_MESSAGE + id);
                });
    }

    /**
     * Saves a new conversion task with the given original and filesystem file names.
     * <p>
     * The newly created task is initialized with a status of {@link ConversionTaskStatus#PENDING} and the current date.
     * </p>
     *
     * @param originalFileName the original name of the file to be converted
     * @param filesystemName   the name of the file in the filesystem after conversion. Expected format: "input-UUID.extension"
     * @return the saved {@link ConversionTask} entity
     * @throws NullPointerException if either {@code originalFileName} or {@code filesystemName} is null
     */
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

    /**
     * Updates the status of a conversion task to {@link ConversionTaskStatus#SUCCESS} and sets the converted file name.
     * <p>
     * Records the processing date as the current time.
     * </p>
     *
     * @param id            the unique identifier of the conversion task
     * @param convertedName the name of the converted file
     * @throws NoSuchElementException if no conversion task with the given {@code id} is found
     * @throws NullPointerException   if either {@code id} or {@code convertedName} is null
     */
    @Transactional
    public void setConversionSuccessStatus(@NonNull UUID id, @NonNull String convertedName) {
        ConversionTask task = getConversionTaskById(id);

        task.setStatus(ConversionTaskStatus.SUCCESS);
        task.setConvertedName(convertedName);
        task.setTaskProcessingDate(LocalDateTime.now());
    }

    /**
     * Updates the status of a conversion task to {@link ConversionTaskStatus#ERROR} and sets the error message.
     *
     * @param id           the unique identifier of the conversion task
     * @param errorMessage message that clarifies reasons behind error
     * @throws NoSuchElementException if no conversion task with the given {@code id} is found
     * @throws NullPointerException   if either {@code id} or {@code errorMessage} is null
     */
    @Transactional
    public void setConversionErrorStatus(@NonNull UUID id, @NonNull ErrorMessage errorMessage) {
        ConversionTask task = getConversionTaskById(id);

        task.setStatus(ConversionTaskStatus.ERROR);
        task.setErrorMessage(errorMessage);
    }

    /**
     * Updates the status of a conversion task to {@link ConversionTaskStatus#DELETED}
     *
     * @param id the unique identifier of the conversion task
     * @throws NoSuchElementException if no conversion task with the given {@code id} is found
     * @throws NullPointerException   if  {@code id} is null
     */
    @Transactional
    public void setConversionDeletedStatus(@NonNull UUID id) {
        ConversionTask conversionTask = getConversionTaskById(id);

        conversionTask.setStatus(ConversionTaskStatus.DELETED);
    }

    /**
     * Retrieves a conversion task by its unique identifier.
     *
     * @param id the unique identifier of the conversion task
     * @return {@link ConversionTask} entity with the specified ID
     * @throws NoSuchElementException if no conversion task with the given {@code id} is found
     * @throws NullPointerException   if  {@code id} is null
     */
    @Transactional(readOnly = true)
    public ConversionTask getConversionTask(@NonNull UUID id) {
        return getConversionTaskById(id);
    }

    /**
     * Retrieves all conversion tasks that currently have {@link ConversionTaskStatus#PENDING} status
     *
     * @return {@link List} of {@link ConversionTask} entities ordered by their creation date
     */
    @Transactional(readOnly = true)
    public List<ConversionTask> getPendingConversionTasks() {
        return conversionTaskRepository.findByStatusOrderByTaskCreationDate(ConversionTaskStatus.PENDING);
    }

    /**
     * Retrieve all conversion task that are intended for deletion
     *
     * @return {@link List} of {@link ConversionTask} entities that has {@link ConversionTaskStatus#SUCCESS} and are older than deletionTimeMinutes
     */
    @Transactional(readOnly = true)
    public List<ConversionTask> getSuccessConversionTasksForDeletion() {
        return conversionTaskRepository.findTasksForDeletionNative(deletionTimeMinutes);
    }
}
