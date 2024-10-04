package com.glamik.webpconverter.dto.response;

import com.glamik.webpconverter.enums.ConversionTaskStatus;
import com.glamik.webpconverter.enums.ErrorMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Builder
@Getter
public class ConversionTaskStatusResponse {

    private ConversionTaskStatus status;
    private ErrorMessage errorMessage;

}
