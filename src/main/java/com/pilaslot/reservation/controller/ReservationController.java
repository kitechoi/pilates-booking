package com.pilaslot.reservation.controller;

import com.pilaslot.global.security.AuthenticatedMember;
import com.pilaslot.reservation.domain.ReservationStatus;
import com.pilaslot.reservation.dto.request.ReservationCreateRequest;
import com.pilaslot.reservation.dto.response.MyReservationListResponse;
import com.pilaslot.reservation.dto.response.ReservationCreateResponse;
import com.pilaslot.reservation.service.ReservationQueryService;
import com.pilaslot.reservation.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reservations")
public class ReservationController {

    private final ReservationService reservationService;
    private final ReservationQueryService reservationQueryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationCreateResponse reserve(
            @Valid @RequestBody ReservationCreateRequest request,
            @AuthenticationPrincipal AuthenticatedMember authenticatedMember
    ) {
        return reservationService.reserve(
                authenticatedMember.memberId(),
                request.classSessionId()
        );
    }

    @GetMapping
    public MyReservationListResponse getMyReservations(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate weekStart,
            @RequestParam(required = false)
            ReservationStatus status,
            @AuthenticationPrincipal AuthenticatedMember authenticatedMember
    ) {
        return reservationQueryService.getMyReservations(
                authenticatedMember.memberId(),
                weekStart,
                status
        );
    }

    @DeleteMapping("/{reservationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(
            @PathVariable Long reservationId,
            @AuthenticationPrincipal AuthenticatedMember authenticatedMember
    ) {
        reservationService.cancel(authenticatedMember.memberId(), reservationId);
    }
}
