package com.glamik.webpconverter.controllers;

import com.glamik.webpconverter.service.ConverterService;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.PathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequiredArgsConstructor
public class ConverterController {

    private final ConverterService converterService;

    @PostMapping("/convert-to-webp")
    public ResponseEntity<PathResource> convertImage(@RequestParam("image") MultipartFile imageFile) {
        Path tempDir;
        try {
            tempDir = Files.createTempDirectory("webp-converter-");
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }

        Path tempInputPath = null;
        Path webpPath = null;

        try {
            String originalFilename = imageFile.getOriginalFilename();
            String fileExtension = getFileExtension(originalFilename);

            tempInputPath = Files.createTempFile(tempDir, "input-", fileExtension);
            imageFile.transferTo(tempInputPath.toFile());

            webpPath = converterService.convertToWebp(tempInputPath);
            PathResource resource = new PathResource(webpPath);

            return ResponseEntity.ok().body(resource);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        } finally {
            deleteIfExists(webpPath);
            deleteIfExists(tempInputPath);
        }
    }

    private String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        return (lastDot == -1) ? ".tmp" : filename.substring(lastDot);
    }

    private void deleteIfExists(Path path) {
        if (path != null && Files.exists(path)) {
                path.toFile().deleteOnExit();
        }
    }
}