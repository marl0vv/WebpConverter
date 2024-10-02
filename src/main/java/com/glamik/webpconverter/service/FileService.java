package com.glamik.webpconverter.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

import static com.glamik.webpconverter.util.FileUtils.getFileExtension;

@Service
public class FileService {

    private final File baseDir;
    public static final String BASE_DIR = "webpconverter-";
    public static final String IN_DIR = "in";
    public static final String OUT_DIR = "out";

    public FileService() throws IOException {
        this.baseDir = createBaseDirectory();
        createDirectories();
    }

    private File createBaseDirectory() throws IOException {
        File tempDir = File.createTempFile(BASE_DIR, "");
        if (!tempDir.delete() || !tempDir.mkdirs()) {
            throw new IOException("Failed to create base directory: " + tempDir.getAbsolutePath());
        }
        return tempDir;
    }

    public File getInDir() {
        return new File(baseDir, IN_DIR);
    }

    public File getOutDir() {
        return new File(baseDir, OUT_DIR);
    }

    private void createDirectories() throws IOException {
        if (!getInDir().exists() && !getInDir().mkdirs()) {
            throw new IOException("Failed to create input directory: " + getInDir().getAbsolutePath());
        }
        if (!getOutDir().exists() && !getOutDir().mkdirs()) {
            throw new IOException("Failed to create output directory: " + getOutDir().getAbsolutePath());
        }
    }

    public File saveInputFile(MultipartFile imageFile) throws IOException {
        String originalFilename = Objects.requireNonNull(imageFile.getOriginalFilename(), "File must have a name");
        String fileExtension = getFileExtension(originalFilename);
        String newFileName = "input-" + UUID.randomUUID() + fileExtension;
        File inputFile = new File(getInDir(), newFileName);
        imageFile.transferTo(inputFile);
        return inputFile;
    }

}
