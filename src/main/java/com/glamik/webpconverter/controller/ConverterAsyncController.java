package com.glamik.webpconverter.controller;

import com.glamik.webpconverter.command.SaveConversionTaskCommand;
import com.glamik.webpconverter.dto.response.ConversionTaskStatusResponse;
import com.glamik.webpconverter.enums.ConversionTaskStatus;
import com.glamik.webpconverter.enums.ErrorMessage;
import com.glamik.webpconverter.model.ConversionTask;

import com.glamik.webpconverter.service.ConversionTaskService;
import com.glamik.webpconverter.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.PathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MimeType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ConverterAsyncController {

    private final SaveConversionTaskCommand saveConversionTaskCommand;
    private final ConversionTaskService conversionTaskService;
    private final FileService fileService;

    @PostMapping("/convert-to-webp/async")
    public ResponseEntity<UUID> convertImageAsync(@RequestParam("image") MultipartFile imageFile) {
        ConversionTask savedTask = saveConversionTaskCommand.execute(imageFile);
        return ResponseEntity.ok()
                .body(savedTask.getId());
    }

    @GetMapping("/convert-to-webp/async/{taskId}/status")
    public ResponseEntity<ConversionTaskStatusResponse> getTaskStatus(@PathVariable UUID taskId) {
        ConversionTask conversionTask = conversionTaskService.getConversionTask(taskId);
        ConversionTaskStatus status = conversionTask.getStatus();

        ConversionTaskStatusResponse response = ConversionTaskStatusResponse.builder()
                .status(status)
                .errorMessage(status == ConversionTaskStatus.ERROR ? conversionTask.getErrorMessage() : null)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/convert-to-webp/async/{taskId}")
    public ResponseEntity<PathResource> getConvertedImage(@PathVariable UUID taskId) {
        ConversionTask conversionTask = conversionTaskService.getConversionTask(taskId);

        if (conversionTask.getStatus() != ConversionTaskStatus.SUCCESS) {
            return ResponseEntity.notFound().build();
        }

        File outputFile = fileService.getOutputFile(conversionTask.getConvertedName());
        PathResource resource = new PathResource(outputFile.toPath());

        String outputFileName = conversionTask.getOriginalName() + ".webp";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + outputFileName + "\"")
                .contentType(MediaType.asMediaType(MimeType.valueOf("image/webp")))
                .body(resource);
    }

}
