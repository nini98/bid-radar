package com.bidradar.common.exception;

import com.bidradar.common.response.Response;
import com.bidradar.common.response.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Response<Void>> handleApiException(ApiException e) {
        ResultCode resultCode = e.getResultCode();
        log.warn("ApiException: {}", e.getMessage());
        return ResponseEntity
                .status(Integer.parseInt(resultCode.getCode()))
                .body(Response.fail(resultCode));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Response<String>> handleValidationException(MethodArgumentNotValidException e) {
        String detail = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("ValidationException: {}", detail);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Response.fail(ResultCode.VALIDATION_ERROR, detail));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response<Void>> handleException(Exception e) {
        log.error("Unhandled exception", e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Response.fail(ResultCode.INTERNAL_ERROR));
    }
}
