package com.pilaslot.classsession.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ReservationAvailabilityTest {

    private static final LocalDateTime START_AT = LocalDateTime.of(2026, 8, 20, 14, 0);
    private static final LocalDateTime RESERVATION_OPEN_AT = LocalDateTime.of(2026, 8, 13, 9, 0);

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
        ReservationAvailability result = calculate(START_AT.minusHours(2), 0, 4);

        assertThat(result).isEqualTo(ReservationAvailability.AVAILABLE);
    }

    @Test
    void returnsExpiredAfterReservationDeadline() {
        ReservationAvailability result = calculate(START_AT.minusHours(2).plusNanos(1), 0, 4);

        assertThat(result).isEqualTo(ReservationAvailability.EXPIRED);
    }

    @Test
    void expiredTakesPriorityOverFull() {
        ReservationAvailability result = calculate(START_AT.minusHours(2).plusNanos(1), 4, 4);

        assertThat(result).isEqualTo(ReservationAvailability.EXPIRED);
    }

    @Test
    void returnsCancelledForCancelledClassSession() {
        ReservationAvailability result = ReservationAvailability.calculate(
                ClassSessionStatus.CANCELLED,
                RESERVATION_OPEN_AT,
                START_AT,
                0,
                4,
                START_AT.minusHours(3)
        );

        assertThat(result).isEqualTo(ReservationAvailability.CANCELLED);
    }

    @Test
    void cancelledTakesPriorityOverOtherStates() {
        ReservationAvailability result = ReservationAvailability.calculate(
                ClassSessionStatus.CANCELLED,
                RESERVATION_OPEN_AT,
                START_AT,
                4,
                4,
                RESERVATION_OPEN_AT.minusDays(1)
        );

        assertThat(result).isEqualTo(ReservationAvailability.CANCELLED);
    }

    private ReservationAvailability calculate(LocalDateTime now, int reservedCount, int capacity) {
        return ReservationAvailability.calculate(
                ClassSessionStatus.SCHEDULED,
                RESERVATION_OPEN_AT,
                START_AT,
                reservedCount,
                capacity,
                now
        );
    }
}
