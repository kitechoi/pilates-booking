package com.pilaslot.classsession.domain;

import java.time.LocalDateTime;

public enum ReservationState {
    BEFORE_OPEN,
    OPEN,
    FULL,
    CLOSED,
    CANCELLED;

    public static ReservationState calculate(
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
        if (now.isAfter(startAt.minusHours(2))) {
            return CLOSED;
        }
        if (reservedCount >= capacity) {
            return FULL;
        }
        return OPEN;
    }
}
