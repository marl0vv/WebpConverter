package com.glamik.webpconverter.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileService {

    private final File baseDir;
    private final String programDir;

    public static final String IN_DIR = "in";
    public static final String OUT_DIR = "out";

    public FileService(@Value("${base.directory}") String programDir) throws IOException {
        this.programDir = programDir;
        this.baseDir = createBaseDirectory();
        createDirectories();
    }

    private File createBaseDirectory() throws IOException {
        File baseDirectory = new File(programDir);

        if (!baseDirectory.exists() && !baseDirectory.mkdirs()) {
            throw new IOException("Unable to create directory: " + baseDirectory.getAbsolutePath());
        }

        return baseDirectory;
    }

    private File getInDir() {
        return new File(baseDir, IN_DIR);
    }

    private File getOutDir() {
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

    public File saveInputFile(MultipartFile imageFile, String fileExtension) throws IOException {
        String newFileName = "input-" + UUID.randomUUID() + fileExtension;
        File inputFile = new File(getInDir(), newFileName);

        imageFile.transferTo(inputFile);
        return inputFile;
    }

    public File saveOutputFile(File imageFile) throws IOException {
        String newFileName = "output-" + UUID.randomUUID() + ".webp";
        File outputFile = new File(getOutDir(), newFileName);

        Files.copy(imageFile.toPath(), outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        return outputFile;
    }

    public File getInputFile(String filesystemName) {
        return new File(getInDir(), filesystemName);
    }


}
