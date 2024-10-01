package com.glamik.webpconverter.controller;

import com.glamik.webpconverter.model.ConversionTask;
import com.glamik.webpconverter.service.ConversionTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import static com.glamik.webpconverter.util.FileUtils.getFileExtension;

@RestController
@RequiredArgsConstructor
public class ConverterAsyncController {

    private final ConversionTaskService conversionTaskService;

    @PostMapping("/convert-to-webp/async")
    public ResponseEntity<UUID> convertImageAsync(@RequestParam("image") MultipartFile imageFile) {
        File tempInputFile = null;
        String originalFilename;
        try {
            originalFilename = imageFile.getOriginalFilename();
            tempInputFile = File.createTempFile("input-", getFileExtension(originalFilename));
            imageFile.transferTo(tempInputFile);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }

        ConversionTask conversionTask =  conversionTaskService.saveConversionTask(originalFilename);
        return ResponseEntity.ok()
                .body(conversionTask.getId());
    }

}
