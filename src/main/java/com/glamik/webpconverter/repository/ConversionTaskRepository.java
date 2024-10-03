package com.glamik.webpconverter.repository;

import com.glamik.webpconverter.enums.ConversionTaskStatus;
import com.glamik.webpconverter.model.ConversionTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConversionTaskRepository extends JpaRepository<ConversionTask, Long> {

    List<ConversionTask> findByStatus(ConversionTaskStatus status);

}
