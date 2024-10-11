package com.glamik.webpconverter.exception;

import com.glamik.webpconverter.exception.dto.ErrorDto;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.FileNotFoundException;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class ExceptionApiHandler {

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(code = HttpStatus.NOT_FOUND)
    public ErrorDto notFoundException(NoSuchElementException e) {
        return ErrorDto.of((e.getMessage()));
    }

    @ExceptionHandler(FileNotFoundException.class)
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    public ErrorDto fileNotFoundException(FileNotFoundException e) {
        return ErrorDto.of((e.getMessage()));
    }

    @ExceptionHandler(DirectoryCreationException.class)
    @ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorDto directoryCreationException(DirectoryCreationException e) {
        return ErrorDto.of(e.getMessage());
    }

    @ExceptionHandler(ConversionStatusIsErrorException.class)
    @ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorDto conversionStatusIsErrorException(ConversionStatusIsErrorException e) {
        return ErrorDto.of(e.getMessage());
    }

}
