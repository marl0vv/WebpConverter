package com.glamik.webpconverter.controller;

import com.glamik.webpconverter.command.SaveConversionTaskCommand;
import com.glamik.webpconverter.dto.response.ConversionTaskStatusResponse;
import com.glamik.webpconverter.enums.ConversionTaskStatus;
import com.glamik.webpconverter.enums.ErrorMessage;
import com.glamik.webpconverter.model.ConversionTask;

import com.glamik.webpconverter.repository.ConversionTaskRepository;
import com.glamik.webpconverter.service.ConversionTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.PathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ConverterAsyncController {

    private final SaveConversionTaskCommand saveConversionTaskCommand;
    private final ConversionTaskService conversionTaskService;

    @PostMapping("/convert-to-webp/async")
    public ResponseEntity<UUID> convertImageAsync(@RequestParam("image") MultipartFile imageFile) {
        ConversionTask savedTask = saveConversionTaskCommand.execute(imageFile);
        return ResponseEntity.ok()
                .body(savedTask.getId());
    }

    @GetMapping("/convert-to-webp/async/{taskId}/status")
    public ResponseEntity<ConversionTaskStatusResponse> getTaskStatus(@PathVariable("taskId") UUID taskId) {
        ConversionTask conversionTask = conversionTaskService.getConversionTask(taskId);
        ConversionTaskStatus status = conversionTask.getStatus();

        if (status == ConversionTaskStatus.ERROR) {
            ErrorMessage errorMessage = conversionTask.getErrorMessage();
            ConversionTaskStatusResponse response = new ConversionTaskStatusResponse(status, errorMessage);
            return ResponseEntity.ok(response);
        } else {
            ConversionTaskStatusResponse response = ConversionTaskStatusResponse.builder()
                    .status(status)
                    .build();
            return ResponseEntity.ok(response);
        }
    }

    
}
