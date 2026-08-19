package com.pilaslot.reservation.controller;

import com.pilaslot.global.security.AuthenticatedMember;
import com.pilaslot.reservation.dto.response.ReservationCreateResponse;
import com.pilaslot.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/class-sessions/{classSessionId}/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationCreateResponse reserve(
            @PathVariable Long classSessionId,
            @AuthenticationPrincipal AuthenticatedMember authenticatedMember
    ) {
        return reservationService.reserve(authenticatedMember.memberId(), classSessionId);
    }
}
