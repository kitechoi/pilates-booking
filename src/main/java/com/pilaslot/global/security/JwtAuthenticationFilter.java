package com.pilaslot.global.security;

import com.pilaslot.global.exception.ErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String AUTH_ERROR_CODE_ATTRIBUTE = "authErrorCode";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (!StringUtils.hasText(authorization)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = extractBearerToken(authorization);
            Long memberId = jwtTokenProvider.getMemberId(token);
            setAuthentication(request, memberId);
        } catch (JwtAuthenticationException exception) {
            SecurityContextHolder.clearContext();
            request.setAttribute(AUTH_ERROR_CODE_ATTRIBUTE, exception.getErrorCode());
        }

        filterChain.doFilter(request, response);
    }

    private String extractBearerToken(String authorization) {
        if (!authorization.startsWith(BEARER_PREFIX)) {
            throw invalidToken();
        }
        String token = authorization.substring(BEARER_PREFIX.length());
        if (!StringUtils.hasText(token)) {
            throw invalidToken();
        }
        return token;
    }

    private void setAuthentication(HttpServletRequest request, Long memberId) {
        AuthenticatedMember principal = new AuthenticatedMember(memberId);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(principal, null, List.of());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private JwtAuthenticationException invalidToken() {
        return new JwtAuthenticationException(
                ErrorCode.INVALID_TOKEN,
                new IllegalArgumentException("Invalid authorization scheme")
        );
    }
}
