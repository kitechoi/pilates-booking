package com.pilaslot.classsession.controller;

import com.pilaslot.classsession.domain.ClassSessionStatus;
import com.pilaslot.classsession.domain.ClassType;
import com.pilaslot.classsession.domain.ReservationAvailability;
import com.pilaslot.classsession.dto.response.ClassSessionResponse;
import com.pilaslot.classsession.dto.response.WeeklyClassSessionResponse;
import com.pilaslot.classsession.service.ClassSessionQueryService;
import com.pilaslot.global.config.TimeConfig;
import com.pilaslot.global.exception.BusinessException;
import com.pilaslot.global.exception.ErrorCode;
import com.pilaslot.global.exception.GlobalExceptionHandler;
import com.pilaslot.global.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClassSessionController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class, TimeConfig.class})
class ClassSessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClassSessionQueryService classSessionQueryService;

    @Test
    void returnsWeeklyClassSessions() throws Exception {
        LocalDate weekStart = LocalDate.of(2026, 8, 17);
        LocalDateTime startAt = LocalDateTime.of(2026, 8, 20, 14, 0);
        ClassSessionResponse response = createClassSessionResponse(startAt);
        given(classSessionQueryService.getWeeklyClassSessions(weekStart))
                .willReturn(new WeeklyClassSessionResponse(weekStart, List.of(response)));

        mockMvc.perform(get("/api/v1/class-sessions")
                        .param("weekStart", weekStart.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.weekStart").value("2026-08-17"))
                .andExpect(jsonPath("$.sessions").isArray())
                .andExpect(jsonPath("$.sessions[0].classSessionId").value(1))
                .andExpect(jsonPath("$.sessions[0].classType").value("REFORMER"))
                .andExpect(jsonPath("$.sessions[0].instructor.instructorId").value(10))
                .andExpect(jsonPath("$.sessions[0].instructor.name").value("김필라"))
                .andExpect(jsonPath("$.sessions[0].startAt").value("2026-08-20T14:00:00"))
                .andExpect(jsonPath("$.sessions[0].endAt").value("2026-08-20T14:50:00"))
                .andExpect(jsonPath("$.sessions[0].remainingCount").value(3))
                .andExpect(jsonPath("$.sessions[0].reservationAvailability").value("AVAILABLE"))
                .andExpect(jsonPath("$.sessions[0].reservationState").doesNotExist());
    }

    @Test
    void returnsClassSessionDetailWithoutAuthentication() throws Exception {
        LocalDateTime startAt = LocalDateTime.of(2026, 8, 20, 14, 0);
        given(classSessionQueryService.getClassSession(1L))
                .willReturn(createClassSessionResponse(startAt));

        mockMvc.perform(get("/api/v1/class-sessions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.classSessionId").value(1))
                .andExpect(jsonPath("$.classType").value("REFORMER"))
                .andExpect(jsonPath("$.instructor.instructorId").value(10))
                .andExpect(jsonPath("$.instructor.name").value("김필라"))
                .andExpect(jsonPath("$.instructor.profileImageUrl")
                        .value("https://example.com/instructors/kim.jpg"))
                .andExpect(jsonPath("$.startAt").value("2026-08-20T14:00:00"))
                .andExpect(jsonPath("$.durationMinutes").value(50))
                .andExpect(jsonPath("$.endAt").value("2026-08-20T14:50:00"))
                .andExpect(jsonPath("$.reservationOpenAt").value("2026-08-13T09:00:00"))
                .andExpect(jsonPath("$.capacity").value(4))
                .andExpect(jsonPath("$.reservedCount").value(1))
                .andExpect(jsonPath("$.remainingCount").value(3))
                .andExpect(jsonPath("$.status").value("SCHEDULED"))
                .andExpect(jsonPath("$.reservationAvailability").value("AVAILABLE"))
                .andExpect(jsonPath("$.reservationState").doesNotExist())
                .andExpect(jsonPath("$.reservationId").doesNotExist())
                .andExpect(jsonPath("$.reservationStatus").doesNotExist())
                .andExpect(jsonPath("$.myReservationStatus").doesNotExist())
                .andExpect(jsonPath("$.isReservedByMe").doesNotExist())
                .andExpect(jsonPath("$.memberId").doesNotExist())
                .andExpect(jsonPath("$.memberName").doesNotExist());
    }

    @Test
    void returnsClassSessionNotFoundForUnknownId() throws Exception {
        given(classSessionQueryService.getClassSession(999999L))
                .willThrow(new BusinessException(ErrorCode.CLASS_SESSION_NOT_FOUND));

        mockMvc.perform(get("/api/v1/class-sessions/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CLASS_SESSION_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("수업을 찾을 수 없습니다."))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/api/v1/class-sessions/999999"))
                .andExpect(jsonPath("$.errors").isEmpty());
    }

    @Test
    void returnsInvalidRequestForMalformedClassSessionId() throws Exception {
        mockMvc.perform(get("/api/v1/class-sessions/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").value("요청 값이 올바르지 않습니다."))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/api/v1/class-sessions/abc"))
                .andExpect(jsonPath("$.errors[0].field").value("classSessionId"));
    }

    @Test
    void returnsInvalidWeekStartBusinessError() throws Exception {
        LocalDate weekStart = LocalDate.of(2026, 8, 18);
        given(classSessionQueryService.getWeeklyClassSessions(weekStart))
                .willThrow(new BusinessException(ErrorCode.INVALID_WEEK_START));

        mockMvc.perform(get("/api/v1/class-sessions")
                        .param("weekStart", weekStart.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_WEEK_START"))
                .andExpect(jsonPath("$.message").value("weekStart는 월요일이어야 합니다."))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/api/v1/class-sessions"))
                .andExpect(jsonPath("$.errors").isEmpty());
    }

    @Test
    void returnsValidationErrorForMalformedWeekStart() throws Exception {
        mockMvc.perform(get("/api/v1/class-sessions")
                        .param("weekStart", "2026-08-xx"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").value("요청 값이 올바르지 않습니다."))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/api/v1/class-sessions"))
                .andExpect(jsonPath("$.errors[0].field").value("weekStart"));
    }

    @Test
    void returnsValidationErrorWhenWeekStartIsMissing() throws Exception {
        mockMvc.perform(get("/api/v1/class-sessions"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/api/v1/class-sessions"))
                .andExpect(jsonPath("$.errors[0].field").value("weekStart"));
    }

    @Test
    void returnsCommonErrorResponseForUnexpectedException() throws Exception {
        LocalDate weekStart = LocalDate.of(2026, 8, 17);
        given(classSessionQueryService.getWeeklyClassSessions(weekStart))
                .willThrow(new RuntimeException("sensitive internal detail"));

        mockMvc.perform(get("/api/v1/class-sessions")
                        .param("weekStart", weekStart.toString()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.message").value("서버 내부 오류가 발생했습니다."))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/api/v1/class-sessions"))
                .andExpect(jsonPath("$.errors").isEmpty())
                .andExpect(content().string(not(containsString("sensitive internal detail"))));
    }

    @Test
    void returnsNotFoundForUnknownEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/class-sessions/1/unknown"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("요청한 리소스를 찾을 수 없습니다."))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/api/v1/class-sessions/1/unknown"))
                .andExpect(jsonPath("$.errors").isEmpty());
    }

    @Test
    @WithMockUser
    void returnsMethodNotAllowedForUnsupportedMethod() throws Exception {
        mockMvc.perform(post("/api/v1/class-sessions").with(csrf()))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.code").value("METHOD_NOT_ALLOWED"))
                .andExpect(jsonPath("$.message").value("지원하지 않는 HTTP 메서드입니다."))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/api/v1/class-sessions"))
                .andExpect(jsonPath("$.errors").isEmpty());
    }

    private ClassSessionResponse createClassSessionResponse(LocalDateTime startAt) {
        return new ClassSessionResponse(
                1L,
                ClassType.REFORMER,
                new ClassSessionResponse.InstructorResponse(
                        10L,
                        "김필라",
                        "https://example.com/instructors/kim.jpg"
                ),
                startAt,
                50,
                startAt.plusMinutes(50),
                LocalDateTime.of(2026, 8, 13, 9, 0),
                4,
                1,
                3,
                ClassSessionStatus.SCHEDULED,
                ReservationAvailability.AVAILABLE
        );
    }
}
