package com.pilaslot.reservation.dto.response;

import java.time.LocalDate;
import java.util.List;

public record MyReservationListResponse(
        LocalDate weekStart,
        List<MyReservationResponse> reservations
) {
}
