package com.glamik.webpconverter.command;

import com.glamik.webpconverter.model.ConversionTask;
import com.glamik.webpconverter.service.ConversionTaskService;
import com.glamik.webpconverter.service.FileService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@RequiredArgsConstructor
@Getter
public class SaveConversionTaskCommand implements Command<MultipartFile, ConversionTask> {

    private final ConversionTaskService conversionTaskService;
    private final FileService fileService;

    private File savedFile;
    private ConversionTask savedTask;

    @Override
    public ConversionTask execute(MultipartFile imageFile) {
        try {
            savedFile = fileService.saveInputFile(imageFile);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save input file", e);
        }
        savedTask = conversionTaskService.saveConversionTask(savedFile.getName());
        return savedTask;
    }
}
