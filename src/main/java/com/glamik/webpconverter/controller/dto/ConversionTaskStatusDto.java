package com.glamik.webpconverter.controller.dto;

import com.glamik.webpconverter.enums.ConversionTaskStatus;
import com.glamik.webpconverter.enums.ErrorMessage;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class ConversionTaskStatusDto {

    private ConversionTaskStatus status;
    private ErrorMessage errorMessage;

}
