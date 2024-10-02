package com.glamik.webpconverter.command;

import com.glamik.webpconverter.model.ConversionTask;
import com.glamik.webpconverter.service.ConversionTaskService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class SaveConversionTaskCommand implements Command {

    private final ConversionTaskService conversionTaskService;
    private final String fileName;

    private ConversionTask savedTask;

    @Override
    public void execute() {
        savedTask = conversionTaskService.saveConversionTask(fileName);
    }
}
