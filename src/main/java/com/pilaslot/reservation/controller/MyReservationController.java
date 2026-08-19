package com.pilaslot.reservation.controller;

import com.pilaslot.global.security.AuthenticatedMember;
import com.pilaslot.reservation.dto.response.MyReservationListResponse;
import com.pilaslot.reservation.service.ReservationQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members/me/reservations")
public class MyReservationController {

    private final ReservationQueryService reservationQueryService;

    @GetMapping
    public MyReservationListResponse getMyReservations(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate weekStart,
            @RequestParam(required = false)
            String status,
            @AuthenticationPrincipal AuthenticatedMember authenticatedMember
    ) {
        return reservationQueryService.getMyReservations(
                authenticatedMember.memberId(),
                weekStart,
                status
        );
    }
}
