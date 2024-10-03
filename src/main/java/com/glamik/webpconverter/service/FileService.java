package com.glamik.webpconverter.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

import static com.glamik.webpconverter.util.FileUtils.getFileExtension;

@Service
public class FileService {


    private final String programDir;
    private final File baseDir;

    public static final String BASE_DIR = "WebpConverterImages";
    public static final String IN_DIR = "in";
    public static final String OUT_DIR = "out";

    public FileService(@Value("${base.directory}") String programDir) throws IOException {
        this.programDir = programDir;
        this.baseDir = createBaseDirectory();
        createDirectories();
    }

    private File createBaseDirectory() throws IOException {
        File currentDirectory = new File(programDir);

        File parentDir = currentDirectory.getParentFile();
        if (parentDir == null) {
            throw new IOException("Cannot determine the parent directory of the current directory: " + programDir);
        }

        File baseDirectory = new File(parentDir, BASE_DIR);

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

}
