package com.glamik.webpconverter.service;

import com.glamik.webpconverter.exception.DirectoryCreationException;
import com.glamik.webpconverter.exception.FileSaveException;
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

    public FileService(@Value("${base.directory}") String programDir) {
        this.programDir = programDir;
        this.baseDir = createBaseDirectory();
        createDirectories();
    }

    private File createBaseDirectory() {
        File baseDirectory = new File(programDir);

        if (!baseDirectory.exists() && !baseDirectory.mkdirs()) {
            throw new DirectoryCreationException("Unable to create base directory: " + baseDirectory.getAbsolutePath());
        }

        return baseDirectory;
    }

    private File getInDir() {
        return new File(baseDir, IN_DIR);
    }

    private File getOutDir() {
        return new File(baseDir, OUT_DIR);
    }

    private void createDirectories() {
        if (!getInDir().exists() && !getInDir().mkdirs()) {
            throw new DirectoryCreationException("Failed to create directory for input images: " + getInDir().getAbsolutePath());
        }
        if (!getOutDir().exists() && !getOutDir().mkdirs()) {
            throw new DirectoryCreationException("Failed to create directory for output images: " + getOutDir().getAbsolutePath());
        }
    }

    public File saveInputFile(MultipartFile imageFile, String fileExtension) {
        String newFileName = "input-" + UUID.randomUUID() + fileExtension;
        File inputFile = new File(getInDir(), newFileName);

        try {
            imageFile.transferTo(inputFile);
        } catch (IOException e) {
            throw new FileSaveException("Unable to write input file: " + inputFile.getAbsolutePath());
        }

        return inputFile;
    }

    public File saveOutputFile(File imageFile) {
        String newFileName = "output-" + UUID.randomUUID() + ".webp";
        File outputFile = new File(getOutDir(), newFileName);

        try {
            Files.copy(imageFile.toPath(), outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new FileSaveException("Unable to write output file: " + outputFile.getAbsolutePath());
        }

        return outputFile;
    }

    public File getInputFile(String filesystemName) {
        return new File(getInDir(), filesystemName);
    }

    public File getOutputFile(String convertedName) {
        return new File(getOutDir(), convertedName);
    }
}
