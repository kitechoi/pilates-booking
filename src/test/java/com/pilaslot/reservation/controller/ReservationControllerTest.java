package com.pilaslot.reservation.controller;

import com.pilaslot.global.config.TimeConfig;
import com.pilaslot.global.exception.BusinessException;
import com.pilaslot.global.exception.ErrorCode;
import com.pilaslot.global.exception.GlobalExceptionHandler;
import com.pilaslot.global.security.CustomAuthenticationEntryPoint;
import com.pilaslot.global.security.JwtAuthenticationFilter;
import com.pilaslot.global.security.JwtTokenProvider;
import com.pilaslot.global.security.SecurityConfig;
import com.pilaslot.reservation.domain.ReservationStatus;
import com.pilaslot.reservation.dto.response.ReservationCreateResponse;
import com.pilaslot.reservation.service.ReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest(ReservationController.class)
@Import({
        GlobalExceptionHandler.class,
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        CustomAuthenticationEntryPoint.class,
        TimeConfig.class
})
class ReservationControllerTest {

    private static final LocalDateTime RESERVED_AT = LocalDateTime.of(2026, 8, 19, 13, 0, 1);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationService reservationService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUpAuthentication() {
        given(jwtTokenProvider.getMemberId("valid-token")).willReturn(42L);
    }

    @Test
    void returnsUnauthorizedWithoutToken() throws Exception {
        mockMvc.perform(post("/api/v1/class-sessions/10/reservations"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("인증이 필요합니다."))
                .andExpect(jsonPath("$.path")
                        .value("/api/v1/class-sessions/10/reservations"));
    }

    @Test
    void createsReservationUsingAuthenticatedMemberId() throws Exception {
        given(reservationService.reserve(42L, 10L)).willReturn(new ReservationCreateResponse(
                55L,
                10L,
                ReservationStatus.RESERVED,
                RESERVED_AT
        ));

        mockMvc.perform(post("/api/v1/class-sessions/10/reservations")
                        .header("Authorization", "Bearer valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"memberId\":999}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(55))
                .andExpect(jsonPath("$.classSessionId").value(10))
                .andExpect(jsonPath("$.status").value("RESERVED"))
                .andExpect(jsonPath("$.reservedAt").value("2026-08-19T13:00:01"));
        verify(reservationService).reserve(42L, 10L);
    }

    @Test
    void returnsInvalidRequestForMalformedClassSessionId() throws Exception {
        mockMvc.perform(post("/api/v1/class-sessions/not-a-number/reservations")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.path")
                        .value("/api/v1/class-sessions/not-a-number/reservations"));
    }

    @Test
    void returnsExistingErrorResponseForBusinessException() throws Exception {
        given(reservationService.reserve(42L, 999L))
                .willThrow(new BusinessException(ErrorCode.CLASS_SESSION_NOT_FOUND));

        mockMvc.perform(post("/api/v1/class-sessions/999/reservations")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CLASS_SESSION_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("수업을 찾을 수 없습니다."))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path")
                        .value("/api/v1/class-sessions/999/reservations"))
                .andExpect(jsonPath("$.errors").isEmpty());
    }

    @Test
    void returnsUnauthorizedWhenCancellingWithoutToken() throws Exception {
        mockMvc.perform(delete("/api/v1/reservations/55"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("인증이 필요합니다."))
                .andExpect(jsonPath("$.path").value("/api/v1/reservations/55"));
    }

    @Test
    void cancelsReservationUsingAuthenticatedMemberId() throws Exception {
        mockMvc.perform(delete("/api/v1/reservations/55")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(reservationService).cancel(42L, 55L);
    }

    @Test
    void returnsInvalidRequestForMalformedReservationId() throws Exception {
        mockMvc.perform(delete("/api/v1/reservations/not-a-number")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.path")
                        .value("/api/v1/reservations/not-a-number"));
    }

    @Test
    void returnsExistingErrorResponseWhenCancellationFails() throws Exception {
        willThrow(new BusinessException(ErrorCode.RESERVATION_NOT_FOUND))
                .given(reservationService)
                .cancel(42L, 999L);

        mockMvc.perform(delete("/api/v1/reservations/999")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESERVATION_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("예약을 찾을 수 없습니다."))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/api/v1/reservations/999"))
                .andExpect(jsonPath("$.errors").isEmpty());
    }
}
