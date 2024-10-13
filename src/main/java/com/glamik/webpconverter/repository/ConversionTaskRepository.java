package com.glamik.webpconverter.repository;

import com.glamik.webpconverter.enums.ConversionTaskStatus;
import com.glamik.webpconverter.model.ConversionTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ConversionTaskRepository extends JpaRepository<ConversionTask, UUID> {

    List<ConversionTask> findByStatusOrderByTaskCreationDate(ConversionTaskStatus status);

    @Query(
            value = "SELECT * FROM conversion_task ct WHERE ct.task_processing_date < (NOW() - (:deletionTimeMinutes * INTERVAL '1 minute'))",
            nativeQuery = true
    )
    List<ConversionTask> findTasksForDeletionNative(@Param("deletionTimeMinutes") int deletionTimeMinutes);

}
