package com.pilaslot.reservation.dto.response;

import com.pilaslot.reservation.domain.Reservation;
import com.pilaslot.reservation.domain.ReservationStatus;

import java.time.LocalDateTime;

public record ReservationCreateResponse(
        Long id,
        Long classSessionId,
        ReservationStatus status,
        LocalDateTime reservedAt
) {

    public static ReservationCreateResponse from(Reservation reservation) {
        return new ReservationCreateResponse(
                reservation.getId(),
                reservation.getClassSession().getId(),
                reservation.getStatus(),
                reservation.getReservedAt()
        );
    }
}
