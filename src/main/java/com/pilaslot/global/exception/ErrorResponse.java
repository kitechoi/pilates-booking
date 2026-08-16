package com.pilaslot.global.exception;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponse(
        String code,
        String message,
        LocalDateTime timestamp,
        String path,
        List<FieldError> errors
) {

    public static ErrorResponse from(
            ErrorCode errorCode,
            LocalDateTime timestamp,
            String path
    ) {
        return new ErrorResponse(
                errorCode.name(),
                errorCode.getMessage(),
                timestamp,
                path,
                List.of()
        );
    }

    public static ErrorResponse of(
            ErrorCode errorCode,
            LocalDateTime timestamp,
            String path,
            List<FieldError> errors
    ) {
        return new ErrorResponse(
                errorCode.name(),
                errorCode.getMessage(),
                timestamp,
                path,
                errors
        );
    }

    public record FieldError(String field, String message) {
    }
}
