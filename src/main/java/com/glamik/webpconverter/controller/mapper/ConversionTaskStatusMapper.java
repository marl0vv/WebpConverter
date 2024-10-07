package com.glamik.webpconverter.controller.mapper;

import com.glamik.webpconverter.controller.dto.ConversionTaskStatusDto;
import com.glamik.webpconverter.enums.ConversionTaskStatus;
import com.glamik.webpconverter.model.ConversionTask;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", imports = {ConversionTaskStatus.class})
public interface ConversionTaskStatusMapper {
    @Mapping(target = "errorMessage", expression = "java(conversionTask.getStatus() == ConversionTaskStatus.ERROR ? conversionTask.getErrorMessage() : null)")
    public ConversionTaskStatusDto mapToStatusDto(ConversionTask conversionTask);
}
