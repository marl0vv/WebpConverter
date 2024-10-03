package com.glamik.webpconverter.command;

import com.glamik.webpconverter.model.ConversionTask;
import com.glamik.webpconverter.service.ConversionTaskService;
import com.glamik.webpconverter.service.FileService;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Objects;

import static com.glamik.webpconverter.util.FileUtils.getFileExtension;
import static com.glamik.webpconverter.util.FileUtils.getFileNameWithoutExtension;

@RequiredArgsConstructor
@Component
public class SaveConversionTaskCommand implements Command<MultipartFile, ConversionTask> {

    private final ConversionTaskService conversionTaskService;
    private final FileService fileService;

    @Override
    @SneakyThrows
    public ConversionTask execute(MultipartFile imageFile) {
        String originalFilename = Objects.requireNonNull(imageFile.getOriginalFilename(), "File must have a name");

        File savedFile = fileService.saveInputFile(imageFile, getFileExtension(originalFilename));
        return conversionTaskService.saveConversionTask(getFileNameWithoutExtension(originalFilename), savedFile.getName());
    }

}
