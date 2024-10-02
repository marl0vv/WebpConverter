package com.glamik.webpconverter.command;

import com.glamik.webpconverter.service.FileService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@RequiredArgsConstructor
@Getter
public class SaveInputFileCommand implements Command {

    private final FileService fileService;
    private final MultipartFile imageFile;

    private File savedFile;

    @Override
    public void execute() {
        try {
            savedFile = fileService.saveInputFile(imageFile);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save input file", e);
        }
    }

}
