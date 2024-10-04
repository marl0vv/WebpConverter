package com.glamik.webpconverter.repository;

import com.glamik.webpconverter.enums.ConversionTaskStatus;
import com.glamik.webpconverter.model.ConversionTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ConversionTaskRepository extends JpaRepository<ConversionTask, Long> {

    List<ConversionTask> findByStatusOrderByTaskCreationDate(ConversionTaskStatus status);

    ConversionTask getById(UUID id);

}
