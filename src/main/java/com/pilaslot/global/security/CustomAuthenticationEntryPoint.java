package com.pilaslot.global.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pilaslot.global.exception.ErrorCode;
import com.pilaslot.global.exception.ErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;
    private final Clock clock;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException, ServletException {
        ErrorCode errorCode = resolveErrorCode(request);
        ErrorResponse errorResponse = ErrorResponse.from(
                errorCode,
                LocalDateTime.now(clock),
                request.getRequestURI()
        );

        response.setStatus(errorCode.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(response.getWriter(), errorResponse);
    }

    private ErrorCode resolveErrorCode(HttpServletRequest request) {
        Object errorCode = request.getAttribute(JwtAuthenticationFilter.AUTH_ERROR_CODE_ATTRIBUTE);
        return errorCode instanceof ErrorCode authErrorCode
                ? authErrorCode
                : ErrorCode.UNAUTHORIZED;
    }
}
