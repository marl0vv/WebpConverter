package com.glamik.webpconverter.command;

import com.glamik.webpconverter.enums.ConversionTaskStatus;
import com.glamik.webpconverter.enums.ErrorMessage;
import com.glamik.webpconverter.model.ConversionTask;
import com.glamik.webpconverter.repository.ConversionTaskRepository;
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

    public ConversionTaskStatus execute(ConversionTask task) {
        try {
            File filesystemFile = fileService.getInputFile(task.getFilesystemName());
            File outputFile = converterService.convertToWebp(filesystemFile);

            outputFile = fileService.saveOutputFile(outputFile);
            Files.delete(filesystemFile.toPath());

            task.setStatus(ConversionTaskStatus.SUCCESS);
            task.setConvertedName(outputFile.getName());
            conversionTaskRepository.save(task);
        } catch (IOException e) {
            log.error("IO error occurred while processing file", e);
            task.setStatus(ConversionTaskStatus.ERROR);
            task.setErrorMessage(ErrorMessage.INPUT_FILE_IS_NULL_OR_CORRUPTED);
            conversionTaskRepository.save(task);
        } catch (IllegalArgumentException e) {
            log.error("Invalid argument", e);
            task.setStatus(ConversionTaskStatus.ERROR);
            task.setErrorMessage(ErrorMessage.INPUT_FILE_IS_NOT_AN_IMAGE);
            conversionTaskRepository.save(task);
        }
        return task.getStatus();
    }
}
