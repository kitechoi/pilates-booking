package com.pilaslot.classsession.domain;

import java.time.LocalDateTime;

public enum ReservationAvailability {
    BEFORE_OPEN,
    AVAILABLE,
    FULL,
    CLOSED,
    CANCELLED;

    public static ReservationAvailability calculate(
            ClassSession classSession,
            LocalDateTime now
    ) {
        if (classSession.getStatus() == ClassSessionStatus.CANCELLED) {
            return CANCELLED;
        }
        if (now.isBefore(classSession.getReservationOpenAt())) {
            return BEFORE_OPEN;
        }
        if (now.isAfter(classSession.getReservationDeadline())) {
            return CLOSED;
        }
        if (classSession.getReservedCount() >= classSession.getCapacity()) {
            return FULL;
        }
        return AVAILABLE;
    }
}
