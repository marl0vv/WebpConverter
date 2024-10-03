package com.glamik.webpconverter.service;

import com.glamik.webpconverter.enums.ConversionTaskStatus;
import com.glamik.webpconverter.model.ConversionTask;
import com.glamik.webpconverter.repository.ConversionTaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConversionTaskService {

    private final ConversionTaskRepository conversionTaskRepository;

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

}
