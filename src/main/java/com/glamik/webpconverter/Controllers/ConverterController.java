package com.glamik.webpconverter.Controllers;

import com.glamik.webpconverter.Service.ConverterService;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
public class ConverterController {

    private final ConverterService converterService;

    public ConverterController(ConverterService converterService) {
        this.converterService = converterService;
    }

    @PostMapping("/convert-to-webp")
    public ResponseEntity<ByteArrayResource> convertImage(@RequestParam("image") MultipartFile imageFile) throws IOException {
        try {
            byte[] webpBytes = converterService.convertToWebp(imageFile.getInputStream());
            ByteArrayResource byteArrayResource = new ByteArrayResource(webpBytes);
            return ResponseEntity.ok().body(byteArrayResource);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
