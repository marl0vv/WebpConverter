package com.glamik.webpconverter.scheduler;

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
import java.util.UUID;

/**
 * Scheduler responsible for deleting outdated or unnecessary converted image files.
 *
 * <p>
 * This scheduler periodically performs the following tasks:
 *     <ol>
 *         <li>Retrieves a list of successfully converted tasks that are eligible for deletion.</li>
 *         <li>Deletes the corresponding image files from the filesystem.</li>
 *         <li>Updates the status of each conversion task to {@code DELETED} in the repository</li>
 *     </ol>
 * </p>
 *
 * <p>
 *      <strong>Configuration properties:</strong>
 *      <ul>
 *          <li><b>deletion.time.millis</b>: The interval in milliseconds between each execution of the scheduler.</li>
 *      </ul>
 * </p>
 *
 * <p>
 *     <strong>Conditional Activation:</strong>
 *     This scheduler is activated only if the property {@code deletion.time.millis} is defined in the application configuration.
 * </p>
 *
 * @see ConversionTaskService
 * @see FileService
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "deletion.time.millis")
@Slf4j
public class ConversionTaskDeletionScheduler {

    private final ConversionTaskService conversionTaskService;
    private final FileService fileService;

    /**
     * Executed the scheduled task to delete old converted images and update their statuses.
     *
     * <p>
     * The deletion process involves:
     *     <ol>
     *          <li>Fetching all successfully converted tasks that are marked for deletion.</li>
     *          <li>For each task, retrieving the corresponding output file using {@link FileService#getOutputFile(String)}.</li>
     *          <li>Attempting to delete the file from the filesystem.</li>
     *          <li>If the deletion is successful, updating the task's status to {@code DELETED} using {@link ConversionTaskService#setConversionDeletedStatus(UUID)}.</li>
     *          <li>Logging appropriate messages based on the outcome of each deletion attempt.</li>
     *     </ol>
     * </p>
     */
    @Scheduled(initialDelayString = "${deletion.time.millis}", fixedDelayString = "${deletion.time.millis}")
    public void deleteOldConvertedImages() {

        List<ConversionTask> pendingTasks = conversionTaskService.getSuccessConversionTasksForDeletion();
        for (ConversionTask task : pendingTasks) {
            File convertedFile = fileService.getOutputFile(task.getConvertedName());
            try {
                Files.delete(convertedFile.toPath());
                conversionTaskService.setConversionDeletedStatus(task.getId());
            } catch (NoSuchFileException e) {
                log.warn("File not found, might have already been deleted: {}", convertedFile.getAbsolutePath(), e);
            } catch (IOException e) {
                log.error("Failed to delete file: {}", convertedFile.getAbsolutePath(), e);
            }
        }
    }

}
