package com.pilaslot.classsession.domain;

import com.pilaslot.instructor.domain.Instructor;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ReservationAvailabilityTest {

    private static final LocalDateTime START_AT = LocalDateTime.of(2026, 8, 20, 14, 0);
    private static final LocalDateTime RESERVATION_OPEN_AT = LocalDateTime.of(2026, 8, 13, 9, 0);
    private static final LocalDateTime RESERVATION_DEADLINE = START_AT.minusHours(2);

    @Test
    void returnsBeforeOpenBeforeReservationOpenAt() {
        ReservationAvailability result = calculate(RESERVATION_OPEN_AT.minusNanos(1), 0, 4);

        assertThat(result).isEqualTo(ReservationAvailability.BEFORE_OPEN);
    }

    @Test
    void returnsAvailableAtReservationOpenAt() {
        ReservationAvailability result = calculate(RESERVATION_OPEN_AT, 0, 4);

        assertThat(result).isEqualTo(ReservationAvailability.AVAILABLE);
    }

    @Test
    void returnsFullWhenCapacityIsFilled() {
        ReservationAvailability result = calculate(START_AT.minusHours(3), 4, 4);

        assertThat(result).isEqualTo(ReservationAvailability.FULL);
    }

    @Test
    void beforeOpenTakesPriorityOverFull() {
        ReservationAvailability result = calculate(RESERVATION_OPEN_AT.minusNanos(1), 4, 4);

        assertThat(result).isEqualTo(ReservationAvailability.BEFORE_OPEN);
    }

    @Test
    void returnsAvailableAtReservationDeadline() {
        ReservationAvailability result = calculate(RESERVATION_DEADLINE, 0, 4);

        assertThat(result).isEqualTo(ReservationAvailability.AVAILABLE);
    }

    @Test
    void returnsClosedAfterReservationDeadline() {
        ReservationAvailability result = calculate(RESERVATION_DEADLINE.plusNanos(1), 0, 4);

        assertThat(result).isEqualTo(ReservationAvailability.CLOSED);
    }

    @Test
    void closedTakesPriorityOverFull() {
        ReservationAvailability result = calculate(RESERVATION_DEADLINE.plusNanos(1), 4, 4);

        assertThat(result).isEqualTo(ReservationAvailability.CLOSED);
    }

    @Test
    void returnsCancelledForCancelledClassSession() {
        ReservationAvailability result = calculateCancelled(START_AT.minusHours(3), 0, 4);

        assertThat(result).isEqualTo(ReservationAvailability.CANCELLED);
    }

    @Test
    void cancelledTakesPriorityOverOtherStates() {
        ReservationAvailability result = calculateCancelled(
                RESERVATION_OPEN_AT.minusDays(1),
                4,
                4
        );

        assertThat(result).isEqualTo(ReservationAvailability.CANCELLED);
    }

    private ReservationAvailability calculate(LocalDateTime now, int reservedCount, int capacity) {
        return ReservationAvailability.calculate(
                classSession(ClassSessionStatus.SCHEDULED, reservedCount, capacity),
                now
        );
    }

    private ReservationAvailability calculateCancelled(
            LocalDateTime now,
            int reservedCount,
            int capacity
    ) {
        return ReservationAvailability.calculate(
                classSession(ClassSessionStatus.CANCELLED, reservedCount, capacity),
                now
        );
    }

    private ClassSession classSession(
            ClassSessionStatus status,
            int reservedCount,
            int capacity
    ) {
        ClassSession classSession = new ClassSession(
                new Instructor("김필라", null),
                ClassType.REFORMER,
                START_AT,
                50,
                RESERVATION_OPEN_AT,
                capacity,
                status
        );
        ReflectionTestUtils.setField(classSession, "reservedCount", reservedCount);
        return classSession;
    }
}
