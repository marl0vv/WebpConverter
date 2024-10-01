package com.glamik.webpconverter.repository;

import com.glamik.webpconverter.model.ConversionTask;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConversionTaskRepository extends JpaRepository<ConversionTask, Long> {
}
