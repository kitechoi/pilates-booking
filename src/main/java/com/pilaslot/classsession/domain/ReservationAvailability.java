package com.pilaslot.classsession.domain;

import java.time.LocalDateTime;

public enum ReservationAvailability {
    BEFORE_OPEN,
    AVAILABLE,
    FULL,
    CLOSED,
    CANCELLED;

    private static final long RESERVATION_DEADLINE_HOURS = 2;

    public static ReservationAvailability calculate(
            ClassSessionStatus status,
            LocalDateTime reservationOpenAt,
            LocalDateTime startAt,
            int reservedCount,
            int capacity,
            LocalDateTime now
    ) {
        if (status == ClassSessionStatus.CANCELLED) {
            return CANCELLED;
        }
        if (now.isBefore(reservationOpenAt)) {
            return BEFORE_OPEN;
        }
        if (now.isAfter(startAt.minusHours(RESERVATION_DEADLINE_HOURS))) {
            return CLOSED;
        }
        if (reservedCount >= capacity) {
            return FULL;
        }
        return AVAILABLE;
    }
}
