package com.glamik.webpconverter.controller;

import com.glamik.webpconverter.enums.ConversionTaskStatus;
import com.glamik.webpconverter.model.ConversionTask;
import com.glamik.webpconverter.repository.ConversionTaskRepository;
import com.glamik.webpconverter.service.ConverterService;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.PathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ConverterController {

    private final ConverterService converterService;
    private final ConversionTaskRepository conversionTaskRepository;

    @PostMapping("/convert-to-webp")
    public ResponseEntity<PathResource> convertImage(@RequestParam("image") MultipartFile imageFile) {
        File tempInputFile = null;
        File webpFile = null;

        try {
            String originalFilename = imageFile.getOriginalFilename();
            String fileExtension = getFileExtension(originalFilename);
            String outputFileName = getFileNameWithoutExtension(originalFilename) + ".webp";

            tempInputFile = File.createTempFile("input-", fileExtension);
            imageFile.transferTo(tempInputFile);

            webpFile = converterService.convertToWebp(tempInputFile);
            PathResource resource = new PathResource(String.valueOf(webpFile));

            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + outputFileName + "\"");
            responseHeaders.add(HttpHeaders.CONTENT_TYPE, "image/webp");

            return ResponseEntity.ok()
                    .headers(responseHeaders)
                    .body(resource);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } finally {
            deleteIfExists(webpFile);
            deleteIfExists(tempInputFile);
        }
    }

    private String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        return (lastDot == -1) ? ".tmp" : filename.substring(lastDot);
    }

    private String getFileNameWithoutExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        return (lastDot == -1) ? filename : filename.substring(0, lastDot);
    }

    private void deleteIfExists(File file) {
        if (file != null && Files.exists(file.toPath())) {
            file.deleteOnExit();
        }
    }
}