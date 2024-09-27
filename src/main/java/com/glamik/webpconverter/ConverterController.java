package com.glamik.webpconverter;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@RestController
public class ConverterController {
    @PostMapping("/convert-to-webp")
    public ResponseEntity<ByteArrayResource> convertImage(@RequestParam("image") MultipartFile imageFile) throws IOException {

        Resource stubImageResource = new ClassPathResource("static/stub-image.png");
        byte[] imageBytes;
        try (InputStream stubImageStream = stubImageResource.getInputStream()) {
            imageBytes = stubImageStream.readAllBytes();
        } catch (IOException e) {
            throw new IOException("Stub image not found", e);
        }

        ByteArrayResource byteArrayResource = new ByteArrayResource(imageBytes);

        return ResponseEntity.ok().body(byteArrayResource);

    }
}
