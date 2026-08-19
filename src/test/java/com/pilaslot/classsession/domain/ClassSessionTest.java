package com.pilaslot.classsession.domain;

import com.pilaslot.instructor.domain.Instructor;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ClassSessionTest {

    private static final LocalDateTime START_AT = LocalDateTime.of(2026, 8, 20, 19, 0);

    @Test
    void calculatesReservationDeadlineTwoHoursBeforeStart() {
        ClassSession classSession = classSession();

        assertThat(classSession.getReservationDeadline())
                .isEqualTo(LocalDateTime.of(2026, 8, 20, 17, 0));
    }

    @Test
    void calculatesEndAtFromDuration() {
        ClassSession classSession = classSession();

        assertThat(classSession.getEndAt())
                .isEqualTo(LocalDateTime.of(2026, 8, 20, 19, 50));
    }

    @Test
    void calculatesRemainingCount() {
        ClassSession classSession = classSession();
        classSession.increaseReservedCount();

        assertThat(classSession.getRemainingCount()).isEqualTo(3);
    }

    @Test
    void exposesNegativeRemainingCountWithoutClamping() {
        ClassSession classSession = classSession();
        for (int i = 0; i < 5; i++) {
            classSession.increaseReservedCount();
        }

        assertThat(classSession.getRemainingCount()).isEqualTo(-1);
    }

    private ClassSession classSession() {
        return new ClassSession(
                new Instructor("김필라", null),
                ClassType.REFORMER,
                START_AT,
                50,
                START_AT.minusDays(7),
                4,
                ClassSessionStatus.SCHEDULED
        );
    }
}
