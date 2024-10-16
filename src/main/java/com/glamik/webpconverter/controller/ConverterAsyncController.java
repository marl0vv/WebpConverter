package com.glamik.webpconverter.controller;

import com.glamik.webpconverter.annotation.WithRateLimitProtection;
import com.glamik.webpconverter.command.SaveConversionTaskCommand;
import com.glamik.webpconverter.controller.dto.ConversionTaskStatusDto;
import com.glamik.webpconverter.enums.ConversionTaskStatus;
import com.glamik.webpconverter.exception.ConversionStatusIsErrorException;
import com.glamik.webpconverter.model.ConversionTask;
import com.glamik.webpconverter.service.ConversionTaskService;
import com.glamik.webpconverter.service.FileService;
import com.glamik.webpconverter.controller.mapper.ConversionTaskStatusMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.PathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MimeType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.UUID;

/**
 * Controller that handles HTTP requests related to image conversion to WebP format.
 * <p>
 * <strong>Endpoints:</strong>
 * </p>
 * <table border="1">
 *   <thead>
 *      <tr>
 *          <th width="15%"> HTTP Method </th>
 *          <th width="25%"> URL </th>
 *          <th width="20%"> Return Value </th>
 *          <th width="40%"> Description </th>
 *      </tr>
 *   </thead>
 *   <tbody>
 *     <tr>
 *       <td> POST </td>
 *       <td> /convert-to-webp/async </td>
 *       <td> UUID </td>
 *       <td> Request takes  an image to conversion and returns and ID of an conversion task </td>
 *     </tr>
 *     <tr>
 *       <td> GET </td>
 *       <td> /convert-to-webp/async/{taskId}/status </td>
 *       <td> TaskStatus, ErrorMessage </td>
 *       <td> Returns status of conversion: SUCCESS, ERROR, PENDING OR DELETED. If status is ERROR then also returns message containing information about error </td>
 *    </tr>
 *    <tr>
 *       <td> GET </td>
 *       <td> /convert-to-webp/async/{taskId} </td>
 *       <td> Webp File </td>
 *       <td> Returns converted image. If conversion is not finished or finished with error, then return Internal Server Error (500) status </td>
 *    </tr>
 *   </tbody>
 *  </table>
 *
 * <p>
 * <strong>Usage Examples:</strong>
 * </p>
 *
 * <ul>
 *   <li><strong>Start Conversion:</strong> Send a POST request to <code>/convert-to-webp/async</code> with a multipart image file. Receive a UUID to track the task.</li>
 *   <li><strong>Check Status:</strong> Send a GET request to <code>/convert-to-webp/async/{taskId}/status</code> using the UUID to check if the conversion is complete.</li>
 *   <li><strong>Download Converted Image:</strong> Once the status is SUCCESS, send a GET request to <code>/convert-to-webp/async/{taskId}</code> to download the WebP image.</li>
 * </ul>
 */
@RestController
@RequiredArgsConstructor
public class ConverterAsyncController {

    private final SaveConversionTaskCommand saveConversionTaskCommand;
    private final ConversionTaskService conversionTaskService;
    private final FileService fileService;
    private final ConversionTaskStatusMapper conversionTaskStatusMapper;

    /**
     * Initiates an asynchronous image conversion to WebP format
     *
     * @param imageFile the image file to be converted
     * @return UUID representing the unique ID of the conversion task
     */
    @PostMapping("/convert-to-webp/async")
    @WithRateLimitProtection
    public UUID convertImageAsync(@RequestParam("image") MultipartFile imageFile) {
        ConversionTask savedTask = saveConversionTaskCommand.execute(imageFile);
        return savedTask.getId();
    }

    /**
     * Retrieves the status of a specific conversion task.
     *
     * @param taskId the UUID of the conversion task
     * @return ConversionTaskStatusDTO containing the status and an error message
     */
    @GetMapping("/convert-to-webp/async/{taskId}/status")
    public ConversionTaskStatusDto getTaskStatus(@PathVariable UUID taskId) {
        ConversionTask conversionTask = conversionTaskService.getConversionTask(taskId);
        return conversionTaskStatusMapper.mapToStatusDto(conversionTask);
    }

    /**
     * Retrieve the converted
     *
     * @param taskId the UUID of the conversion task
     * @return ResponseEntity containing the WebP file if conversion is successful
     * @throws ConversionStatusIsErrorException if the conversion is not successful
     */
    @GetMapping("/convert-to-webp/async/{taskId}")
    public ResponseEntity<PathResource> getConvertedImage(@PathVariable UUID taskId) {
        ConversionTask conversionTask = conversionTaskService.getConversionTask(taskId);

        if (conversionTask.getStatus() != ConversionTaskStatus.SUCCESS) {
            throw new ConversionStatusIsErrorException("Error during image conversion: " + conversionTask.getErrorMessage());
        }

        File outputFile = fileService.getOutputFile(conversionTask.getConvertedName());
        PathResource resource = new PathResource(outputFile.toPath());

        String outputFileName = conversionTask.getOriginalName() + ".webp";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + outputFileName + "\"")
                .contentType(MediaType.asMediaType(MimeType.valueOf("image/webp")))
                .body(resource);
    }

}
