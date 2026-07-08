package com.group1.auth_service.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.group1.auth_service.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.server.ResponseStatusException;

import java.time.format.DateTimeParseException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        FieldError fieldError = ex.getBindingResult().getFieldError();
        String message = fieldError == null ? "Invalid request payload" : fieldError.getDefaultMessage();
        return ResponseEntity.badRequest().body(new ApiResponse<>(false, message, null));
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<Void>> handleBindException(BindException ex) {
        FieldError fieldError = ex.getBindingResult().getFieldError();
        String message = "Invalid request payload";

        if (fieldError != null) {
            message = fieldError.getDefaultMessage();
            if ("dateOfBirth".equals(fieldError.getField()) && "typeMismatch".equals(fieldError.getCode())) {
                message = "Invalid date format for dateOfBirth. Use yyyy-MM-dd";
            }
        }

        return ResponseEntity.badRequest().body(new ApiResponse<>(false, message, null));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnreadableMessage(HttpMessageNotReadableException ex) {
        String message = "Invalid request payload";

        Throwable cause = ex.getMostSpecificCause();
        if (cause instanceof InvalidFormatException invalidFormat && !invalidFormat.getPath().isEmpty()) {
            String fieldName = invalidFormat.getPath().get(0).getFieldName();
            if ("dateOfBirth".equals(fieldName)) {
                message = "Invalid date format for dateOfBirth. Use yyyy-MM-dd";
            }
        } else if (cause instanceof DateTimeParseException) {
            message = "Invalid date format for dateOfBirth. Use yyyy-MM-dd";
        } else if (ex.getMessage() != null && ex.getMessage().contains("dateOfBirth")) {
            message = "Invalid date format for dateOfBirth. Use yyyy-MM-dd";
        }

        return ResponseEntity.badRequest().body(new ApiResponse<>(false, message, null));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex) {
        return ResponseEntity.badRequest().body(new ApiResponse<>(
                false,
                "Avatar image must be 5 MB or smaller",
                null
        ));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResponse<Void>> handleStatusException(ResponseStatusException ex) {
        return ResponseEntity.status(ex.getStatusCode()).body(new ApiResponse<>(false, ex.getReason(), null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnknownException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Internal server error", null));
    }
}
