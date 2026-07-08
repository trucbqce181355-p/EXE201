package com.group1.customer_service.exception;

import com.group1.customer_service.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult()
                .getAllErrors()
                .get(0)
                .getDefaultMessage();

        return ResponseEntity.badRequest().body(new ApiResponse<>(
                false,
                errorMessage,
                null
        ));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResponse<?>> handleResponseStatusException(ResponseStatusException ex) {
        return ResponseEntity.status(ex.getStatusCode()).body(new ApiResponse<>(
                false,
                ex.getReason(),
                null
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleUnknownException(Exception ex) {
        System.err.println("[GlobalExceptionHandler] Caught unknown exception:");
        ex.printStackTrace();
        return ResponseEntity.internalServerError().body(new ApiResponse<>(
                false,
                "Internal server error: " + ex.getMessage(),
                null
        ));
    }

}
