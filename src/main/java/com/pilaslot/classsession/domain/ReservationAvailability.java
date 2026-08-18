package com.pilaslot.classsession.domain;

import java.time.LocalDateTime;

public enum ReservationAvailability {
    BEFORE_OPEN,
    AVAILABLE,
    FULL,
    EXPIRED,
    CANCELLED;

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
        if (now.isAfter(startAt.minusHours(2))) {
            return EXPIRED;
        }
        if (reservedCount >= capacity) {
            return FULL;
        }
        return AVAILABLE;
    }
}
