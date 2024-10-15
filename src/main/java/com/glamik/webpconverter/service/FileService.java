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

/**
 * Service responsible for managing input and output images within the filesystem.
 * <p>
 * This service handles the creation of necessary directories, saving uploaded input files,
 * saving output files, and retrieving files based on their filenames.
 * It ensures that the application's file storage structure is properly initialized
 * and maintained throughout the application's lifecycle.
 * </p>
 * <p>
 * The directory structure is organized under a base directory specified by the
 * {@code base.directory} property. Within this base directory, two subdirectories
 * are created:
 * <ul>
 *     <li><strong>in</strong>: Stores the original input image files uploaded by users.</li>
 *     <li><strong>out</strong>: Stores the converted output image files.</li>
 * </ul>
 * </p>
 *
 * <p>
 * This service is a Spring-managed singleton bean, ensuring that the directories are
 * initialized once at application startup and remain consistent throughout the application's runtime.
 * </p>
 *
 * @see DirectoryCreationException
 * @see FileSaveException
 */
@Service
public class FileService {

    private final File baseDir;
    private final String programDir;

    public static final String IN_DIR = "in";
    public static final String OUT_DIR = "out";

    /**
     * Constructs a {@code FileService} instance, initializing the base directory and its subdirectories.
     * <p>
     * The constructor performs the following actions:
     *     <ol>
     *         <li>Assigns the base directory path from the {@code base.directory} property</li>
     *         <li>Creates the base directory if it does not exist</li>
     *         <li>Creates the "in" and "out" subdirectories within the base directory.</li>
     *     </ol>
     *     These steps ensure that the necessary directory structures is in place before any file operation occur.
     * </p>
     *
     * @param programDir path to a directory that contains in/out directories for images
     * @throws DirectoryCreationException if the base or subdirectories cannot be created
     */
    public FileService(@Value("${base.directory}") String programDir) {
        this.programDir = programDir;
        this.baseDir = createBaseDirectory();
        createDirectories();
    }

    /**
     * Creates base directory for storing images if it does not already exist.
     *
     * @return the {@link File} object representing the base directory
     * @throws DirectoryCreationException if the base directory cannot be created
     */
    private File createBaseDirectory() {
        File baseDirectory = new File(programDir);

        if (!baseDirectory.exists() && !baseDirectory.mkdirs()) {
            throw new DirectoryCreationException("Unable to create base directory: " + baseDirectory.getAbsolutePath());
        }

        return baseDirectory;
    }

    /**
     * Retrieves the {@code in} subdirectory within the base directory.
     *
     * @return the {@link File} object representing the input directory
     */
    private File getInDir() {
        return new File(baseDir, IN_DIR);
    }

    /**
     * Retrieves the {@code out} subdirectory within the base directory.
     *
     * @return the {@link File} object representing the output directory
     */
    private File getOutDir() {
        return new File(baseDir, OUT_DIR);
    }

    /**
     * Creates "in" and "out" subdirectories within the base directory if they do not exist.
     *
     * @throws DirectoryCreationException if either the "in" or "out" directory cannot be created
     */
    private void createDirectories() {
        if (!getInDir().exists() && !getInDir().mkdirs()) {
            throw new DirectoryCreationException("Failed to create directory for input images: " + getInDir().getAbsolutePath());
        }
        if (!getOutDir().exists() && !getOutDir().mkdirs()) {
            throw new DirectoryCreationException("Failed to create directory for output images: " + getOutDir().getAbsolutePath());
        }
    }

    /**
     * Saves an uploaded input image file to the input directory with a unique filename.
     *
     * @param imageFile     the {@link MultipartFile} representing the uploaded image
     * @param fileExtension the file extension of the image (e.g., ".jpg", ".png")
     * @return the {@link File} object representing the saved input file
     * @throws FileSaveException if the file cannot be saved to the filesystem
     */
    public File saveInputFile(MultipartFile imageFile, String fileExtension) {
        String newFileName = "input-" + UUID.randomUUID() + fileExtension;
        File inputFile = new File(getInDir(), newFileName);

        try {
            imageFile.transferTo(inputFile);
        } catch (IOException e) {
            throw new FileSaveException("Unable to save input file: " + inputFile.getAbsolutePath());
        }

        return inputFile;
    }

    /**
     * Saves a converted output image file to the output directory with a unique filename.
     *
     * @param imageFile the {@link File} representing the image to be saved as output
     * @return the {@link File} object representing the saved output file
     * @throws FileSaveException if the file cannot be saved to the filesystem
     */
    public File saveOutputFile(File imageFile) {
        String newFileName = "output-" + UUID.randomUUID() + ".webp";
        File outputFile = new File(getOutDir(), newFileName);

        try {
            Files.copy(imageFile.toPath(), outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new FileSaveException("Unable to save output file: " + outputFile.getAbsolutePath());
        }

        return outputFile;
    }

    /**
     * Retrieves an input file from the input directory based on its filesystem.
     *
     * @param filesystemName the name of the file as stored in the filesystem
     * @return the {@link File} object representing the requested input file
     */
    public File getInputFile(String filesystemName) {
        return new File(getInDir(), filesystemName);
    }

    /**
     * Retrieves an output file from the output directory based on its converted name.
     *
     * @param convertedName the name of the converted file as stored in the filesystem
     * @return the {@link File} object representing the requested output file
     */
    public File getOutputFile(String convertedName) {
        return new File(getOutDir(), convertedName);
    }
}
