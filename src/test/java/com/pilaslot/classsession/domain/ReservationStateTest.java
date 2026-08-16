package com.pilaslot.classsession.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ReservationStateTest {

    private static final LocalDateTime START_AT = LocalDateTime.of(2026, 8, 20, 14, 0);
    private static final LocalDateTime RESERVATION_OPEN_AT = LocalDateTime.of(2026, 8, 13, 9, 0);

    @Test
    void returnsBeforeOpenBeforeReservationOpenAt() {
        ReservationState result = calculate(RESERVATION_OPEN_AT.minusNanos(1), 0, 4);

        assertThat(result).isEqualTo(ReservationState.BEFORE_OPEN);
    }

    @Test
    void returnsOpenAtReservationOpenAt() {
        ReservationState result = calculate(RESERVATION_OPEN_AT, 0, 4);

        assertThat(result).isEqualTo(ReservationState.OPEN);
    }

    @Test
    void returnsFullWhenCapacityIsFilled() {
        ReservationState result = calculate(START_AT.minusHours(3), 4, 4);

        assertThat(result).isEqualTo(ReservationState.FULL);
    }

    @Test
    void beforeOpenTakesPriorityOverFull() {
        ReservationState result = calculate(RESERVATION_OPEN_AT.minusNanos(1), 4, 4);

        assertThat(result).isEqualTo(ReservationState.BEFORE_OPEN);
    }

    @Test
    void returnsOpenAtReservationDeadline() {
        ReservationState result = calculate(START_AT.minusHours(2), 0, 4);

        assertThat(result).isEqualTo(ReservationState.OPEN);
    }

    @Test
    void returnsClosedAfterReservationDeadline() {
        ReservationState result = calculate(START_AT.minusHours(2).plusNanos(1), 0, 4);

        assertThat(result).isEqualTo(ReservationState.CLOSED);
    }

    @Test
    void closedTakesPriorityOverFull() {
        ReservationState result = calculate(START_AT.minusHours(2).plusNanos(1), 4, 4);

        assertThat(result).isEqualTo(ReservationState.CLOSED);
    }

    @Test
    void returnsCancelledForCancelledClassSession() {
        ReservationState result = ReservationState.calculate(
                ClassSessionStatus.CANCELLED,
                RESERVATION_OPEN_AT,
                START_AT,
                0,
                4,
                START_AT.minusHours(3)
        );

        assertThat(result).isEqualTo(ReservationState.CANCELLED);
    }

    @Test
    void cancelledTakesPriorityOverOtherStates() {
        ReservationState result = ReservationState.calculate(
                ClassSessionStatus.CANCELLED,
                RESERVATION_OPEN_AT,
                START_AT,
                4,
                4,
                RESERVATION_OPEN_AT.minusDays(1)
        );

        assertThat(result).isEqualTo(ReservationState.CANCELLED);
    }

    private ReservationState calculate(LocalDateTime now, int reservedCount, int capacity) {
        return ReservationState.calculate(
                ClassSessionStatus.SCHEDULED,
                RESERVATION_OPEN_AT,
                START_AT,
                reservedCount,
                capacity,
                now
        );
    }
}
