package com.ct240.backend.exception;

import com.ct240.backend.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Object>> handleAppException(AppException ex) {

        ApiResponse<Object> response = new ApiResponse<>();
        response.setCode(ex.getErrorCode().getCode());
        response.setMessage(ex.getErrorCode().getMessage());

        return ResponseEntity
                .status(ex.getErrorCode().getStatusCode())
                .body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleException(Exception ex) {

        ex.printStackTrace();   // in lỗi thật


        ApiResponse<Object> response = new ApiResponse<>();
        response.setCode(1001);
        response.setMessage("Internal Server Error");

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationException(
            MethodArgumentNotValidException ex) {

        String message = ex.getBindingResult()
                .getFieldError()
                .getDefaultMessage();

        ApiResponse<Object> response = new ApiResponse<>();
        response.setCode(1002);
        response.setMessage(message);

        return ResponseEntity.badRequest().body(response);
    }
}