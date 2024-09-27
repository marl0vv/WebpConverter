package com.glamik.webpconverter.Service;

import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Service
public class ConverterService {

    public byte[] convertToWebp(InputStream inputStream) throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            BufferedImage image = ImageIO.read(inputStream);
            ImageIO.write(image, "webp", outputStream);
            return outputStream.toByteArray();
        }
    }
}
