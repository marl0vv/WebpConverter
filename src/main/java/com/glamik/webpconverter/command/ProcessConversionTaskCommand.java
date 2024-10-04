package com.glamik.webpconverter.command;

import com.glamik.webpconverter.enums.ConversionTaskStatus;
import com.glamik.webpconverter.enums.ErrorMessage;
import com.glamik.webpconverter.model.ConversionTask;
import com.glamik.webpconverter.repository.ConversionTaskRepository;
import com.glamik.webpconverter.service.ConversionTaskService;
import com.glamik.webpconverter.service.ConverterService;
import com.glamik.webpconverter.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@RequiredArgsConstructor
@Component
@Slf4j
public class ProcessConversionTaskCommand implements Command<ConversionTask, ConversionTaskStatus> {

    private final ConversionTaskRepository conversionTaskRepository;
    private final FileService fileService;
    private final ConverterService converterService;
    private final ConversionTaskService conversionTaskService;

    public ConversionTaskStatus execute(ConversionTask task) {
        try {
            File filesystemFile = fileService.getInputFile(task.getFilesystemName());
            File outputFile = converterService.convertToWebp(filesystemFile);

            outputFile = fileService.saveOutputFile(outputFile);
            Files.delete(filesystemFile.toPath());
            
            conversionTaskService.setConversionSuccessStatus(task, ConversionTaskStatus.SUCCESS, outputFile.getName());
        } catch (IOException e) {
            log.error("IO error occurred while processing file", e);
            conversionTaskService.setConversionErrorStatus(task, ConversionTaskStatus.ERROR, ErrorMessage.INPUT_FILE_IS_NULL_OR_CORRUPTED);
        } catch (IllegalArgumentException e) {
            log.error("Invalid argument", e);
            conversionTaskService.setConversionErrorStatus(task, ConversionTaskStatus.ERROR, ErrorMessage.INPUT_FILE_IS_NOT_AN_IMAGE);
        }
        return task.getStatus();
    }
}
