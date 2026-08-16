package com.pilaslot.classsession.service;

import com.pilaslot.classsession.dto.response.ClassSessionResponse;
import com.pilaslot.classsession.dto.response.WeeklyClassSessionResponse;
import com.pilaslot.classsession.repository.ClassSessionRepository;
import com.pilaslot.global.exception.BusinessException;
import com.pilaslot.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClassSessionQueryService {

    private final ClassSessionRepository classSessionRepository;
    private final Clock clock;

    @Transactional(readOnly = true)
    public WeeklyClassSessionResponse getWeeklyClassSessions(LocalDate weekStart) {
        validateWeekStart(weekStart);

        LocalDateTime rangeStart = weekStart.atStartOfDay();
        LocalDateTime rangeEnd = weekStart.plusWeeks(1).atStartOfDay();
        LocalDateTime now = LocalDateTime.now(clock);

        List<ClassSessionResponse> sessions = classSessionRepository
                .findAllWithInstructorByStartAtRange(rangeStart, rangeEnd)
                .stream()
                .map(classSession -> ClassSessionResponse.from(classSession, now))
                .toList();

        return new WeeklyClassSessionResponse(weekStart, sessions);
    }

    private void validateWeekStart(LocalDate weekStart) {
        if (weekStart == null || weekStart.getDayOfWeek() != DayOfWeek.MONDAY) {
            throw new BusinessException(ErrorCode.INVALID_WEEK_START);
        }
    }
}
