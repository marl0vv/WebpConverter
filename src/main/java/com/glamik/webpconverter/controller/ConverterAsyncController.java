package com.glamik.webpconverter.controller;

import com.glamik.webpconverter.model.ConversionTask;
import com.glamik.webpconverter.service.ConversionTaskService;
import com.glamik.webpconverter.service.FileService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;


@RestController
@RequiredArgsConstructor
public class ConverterAsyncController {

    private final ConversionTaskService conversionTaskService;
    private final FileService fileService;

    @PostMapping("/convert-to-webp/async")
    public ResponseEntity<UUID> convertImageAsync(@RequestParam("image") MultipartFile imageFile) {
        try {
            File inputFile = fileService.saveInputFile(imageFile);
            ConversionTask conversionTask =  conversionTaskService.saveConversionTask(inputFile.getName());
            return ResponseEntity.ok()
                    .body(conversionTask.getId());
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
