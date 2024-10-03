package com.glamik.webpconverter.command;

import com.glamik.webpconverter.enums.ConversionTaskStatus;
import com.glamik.webpconverter.model.ConversionTask;
import com.glamik.webpconverter.repository.ConversionTaskRepository;
import com.glamik.webpconverter.service.ConverterService;
import com.glamik.webpconverter.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;

@RequiredArgsConstructor
@Component
public class ProcessConversionTaskCommand implements Command<ConversionTask, ConversionTaskStatus> {

    private final ConversionTaskRepository conversionTaskRepository;
    private final FileService fileService;
    private final ConverterService converterService;

    public ConversionTaskStatus execute(ConversionTask task) {
        try {
            File filesystemFile = fileService.getInputFile(task.getFilesystemName());
            File outputFile = converterService.convertToWebp(filesystemFile);

            outputFile = fileService.saveOutputFile(outputFile);
            Files.delete(filesystemFile.toPath());

            task.setStatus(ConversionTaskStatus.SUCCESS);
            task.setConvertedName(outputFile.getName());
            conversionTaskRepository.save(task);
        } catch (Exception e) {
            task.setStatus(ConversionTaskStatus.ERROR);
            conversionTaskRepository.save(task);
        }
        return task.getStatus();
    }
}
