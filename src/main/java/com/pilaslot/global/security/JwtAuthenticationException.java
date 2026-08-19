package com.pilaslot.global.security;

import com.pilaslot.global.exception.ErrorCode;
import lombok.Getter;

@Getter
public class JwtAuthenticationException extends RuntimeException {

    private final ErrorCode errorCode;

    public JwtAuthenticationException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }
}
