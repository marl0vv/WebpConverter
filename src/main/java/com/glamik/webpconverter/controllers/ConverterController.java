package com.glamik.webpconverter.controllers;

import com.glamik.webpconverter.service.ConverterService;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.PathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@RestController
@RequiredArgsConstructor
public class ConverterController {

    private final ConverterService converterService;

    @PostMapping("/convert-to-webp")
    public ResponseEntity<PathResource> convertImage(@RequestParam("image") MultipartFile imageFile) {
        File tempInputFile = null;
        File webpFile = null;

        try {
            String originalFilename = imageFile.getOriginalFilename();
            String fileExtension = getFileExtension(originalFilename);

            tempInputFile = File.createTempFile("input-", fileExtension);
            imageFile.transferTo(tempInputFile);

            webpFile = converterService.convertToWebp(tempInputFile);
            PathResource resource = new PathResource(String.valueOf(webpFile));

            return ResponseEntity.ok().body(resource);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
        finally {
            deleteIfExists(webpFile);
            deleteIfExists(tempInputFile);
        }
    }

    private String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        return (lastDot == -1) ? ".tmp" : filename.substring(lastDot);
    }

    private void deleteIfExists(File file) {
        if (file != null && Files.exists(file.toPath())) {
            file.deleteOnExit();
        }
    }
}