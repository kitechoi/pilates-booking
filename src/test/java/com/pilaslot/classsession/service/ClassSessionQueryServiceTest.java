package com.pilaslot.classsession.service;

import com.pilaslot.classsession.domain.ClassSession;
import com.pilaslot.classsession.domain.ClassSessionStatus;
import com.pilaslot.classsession.domain.ClassType;
import com.pilaslot.classsession.domain.ReservationState;
import com.pilaslot.classsession.dto.response.ClassSessionResponse;
import com.pilaslot.classsession.dto.response.WeeklyClassSessionResponse;
import com.pilaslot.classsession.repository.ClassSessionRepository;
import com.pilaslot.global.exception.BusinessException;
import com.pilaslot.global.exception.ErrorCode;
import com.pilaslot.instructor.domain.Instructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ClassSessionQueryServiceTest {

    private static final LocalDate MONDAY = LocalDate.of(2026, 8, 17);

    @Mock
    private ClassSessionRepository classSessionRepository;

    private ClassSessionQueryService classSessionQueryService;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-08-17T00:00:00Z"), ZoneOffset.UTC);
        classSessionQueryService = new ClassSessionQueryService(classSessionRepository, clock);
    }

    @Test
    void acceptsMondayAsWeekStart() {
        LocalDateTime rangeStart = MONDAY.atStartOfDay();
        LocalDateTime rangeEnd = MONDAY.plusWeeks(1).atStartOfDay();
        given(classSessionRepository.findAllWithInstructorByStartAtRange(rangeStart, rangeEnd))
                .willReturn(List.of());

        assertThatCode(() -> classSessionQueryService.getWeeklyClassSessions(MONDAY))
                .doesNotThrowAnyException();

        verify(classSessionRepository).findAllWithInstructorByStartAtRange(rangeStart, rangeEnd);
    }

    @Test
    void rejectsNonMondayWeekStart() {
        assertThatThrownBy(() -> classSessionQueryService.getWeeklyClassSessions(MONDAY.plusDays(1)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_WEEK_START);
    }

    @Test
    void usesInjectedClockWhenCalculatingReservationState() {
        ZoneId zoneId = ZoneId.of("Asia/Seoul");
        LocalDate weekStart = LocalDate.of(2100, 1, 4);
        LocalDateTime now = weekStart.atTime(9, 0);
        Clock fixedClock = Clock.fixed(now.atZone(zoneId).toInstant(), zoneId);
        ClassSessionQueryService service = new ClassSessionQueryService(
                classSessionRepository,
                fixedClock
        );
        ClassSession classSession = new ClassSession(
                new Instructor("김필라", null),
                ClassType.REFORMER,
                weekStart.plusDays(1).atTime(14, 0),
                50,
                now,
                4,
                ClassSessionStatus.SCHEDULED
        );
        LocalDateTime rangeStart = weekStart.atStartOfDay();
        LocalDateTime rangeEnd = weekStart.plusWeeks(1).atStartOfDay();
        given(classSessionRepository.findAllWithInstructorByStartAtRange(rangeStart, rangeEnd))
                .willReturn(List.of(classSession));

        WeeklyClassSessionResponse result = service.getWeeklyClassSessions(weekStart);

        assertThat(result.sessions()).singleElement()
                .extracting(ClassSessionResponse::reservationState)
                .isEqualTo(ReservationState.OPEN);
    }
}
