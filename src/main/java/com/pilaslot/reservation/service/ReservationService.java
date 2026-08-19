package com.pilaslot.reservation.service;

import com.pilaslot.classsession.domain.ClassSession;
import com.pilaslot.classsession.domain.ClassSessionStatus;
import com.pilaslot.classsession.repository.ClassSessionRepository;
import com.pilaslot.global.exception.BusinessException;
import com.pilaslot.global.exception.ErrorCode;
import com.pilaslot.member.domain.Member;
import com.pilaslot.member.repository.MemberRepository;
import com.pilaslot.reservation.domain.Reservation;
import com.pilaslot.reservation.domain.ReservationStatus;
import com.pilaslot.reservation.dto.response.ReservationCreateResponse;
import com.pilaslot.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private static final int RESERVATION_DEADLINE_HOURS = 2;
    private static final int CANCELLATION_DEADLINE_HOURS = 8;
    private static final long WEEKLY_RESERVATION_LIMIT = 14;
    private static final long WEEKLY_CANCELLATION_LIMIT = 7;

    private final ClassSessionRepository classSessionRepository;
    private final MemberRepository memberRepository;
    private final ReservationRepository reservationRepository;
    private final Clock clock;

    @Transactional
    public ReservationCreateResponse reserve(Long memberId, Long classSessionId) {
        ClassSession classSession = classSessionRepository.findById(classSessionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CLASS_SESSION_NOT_FOUND));
        validateClassSessionStatus(classSession);

        LocalDateTime now = LocalDateTime.now(clock);
        validateReservationTime(classSession, now);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));
        validateDuplicate(memberId, classSessionId);
        validateWeeklyLimit(memberId, classSession.getStartAt());
        validateCapacity(classSession);

        Reservation reservation = Reservation.reserve(member, classSession, now);
        Reservation savedReservation = reservationRepository.save(reservation);
        classSession.increaseReservedCount();

        return ReservationCreateResponse.from(savedReservation);
    }

    @Transactional
    public void cancel(Long memberId, Long reservationId) {
        Reservation reservation = reservationRepository
                .findByIdAndMemberId(reservationId, memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));
        validateReservationStatus(reservation);

        LocalDateTime now = LocalDateTime.now(clock);
        validateCancellationTime(reservation.getClassSession(), now);
        validateWeeklyCancellationLimit(memberId, reservation.getClassSession().getStartAt());

        reservation.cancel(now);
        reservation.getClassSession().decreaseReservedCount();
    }

    private void validateClassSessionStatus(ClassSession classSession) {
        if (classSession.getStatus() != ClassSessionStatus.SCHEDULED) {
            throw new BusinessException(ErrorCode.CLASS_SESSION_CANCELLED);
        }
    }

    private void validateReservationTime(ClassSession classSession, LocalDateTime now) {
        if (now.isBefore(classSession.getReservationOpenAt())) {
            throw new BusinessException(ErrorCode.RESERVATION_NOT_OPEN);
        }
        LocalDateTime deadline = classSession.getStartAt()
                .minusHours(RESERVATION_DEADLINE_HOURS);
        if (now.isAfter(deadline)) {
            throw new BusinessException(ErrorCode.RESERVATION_CLOSED);
        }
    }

    private void validateDuplicate(Long memberId, Long classSessionId) {
        boolean duplicate = reservationRepository.existsByMemberIdAndClassSessionIdAndStatus(
                memberId,
                classSessionId,
                ReservationStatus.RESERVED
        );
        if (duplicate) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESERVATION);
        }
    }

    private void validateWeeklyLimit(Long memberId, LocalDateTime classStartAt) {
        LocalDate weekStartDate = classStartAt.toLocalDate()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDateTime weekStart = weekStartDate.atStartOfDay();
        LocalDateTime weekEnd = weekStart.plusWeeks(1);
        long activeReservationCount = reservationRepository.countByMemberAndStatusInClassSessionWeek(
                memberId,
                ReservationStatus.RESERVED,
                weekStart,
                weekEnd
        );
        if (activeReservationCount >= WEEKLY_RESERVATION_LIMIT) {
            throw new BusinessException(ErrorCode.WEEKLY_RESERVATION_LIMIT_EXCEEDED);
        }
    }

    private void validateCapacity(ClassSession classSession) {
        if (classSession.getReservedCount() >= classSession.getCapacity()) {
            throw new BusinessException(ErrorCode.CLASS_SESSION_FULL);
        }
    }

    private void validateReservationStatus(Reservation reservation) {
        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new BusinessException(ErrorCode.RESERVATION_ALREADY_CANCELLED);
        }
    }

    private void validateCancellationTime(ClassSession classSession, LocalDateTime now) {
        LocalDateTime deadline = classSession.getStartAt()
                .minusHours(CANCELLATION_DEADLINE_HOURS);
        if (now.isAfter(deadline)) {
            throw new BusinessException(ErrorCode.CANCELLATION_CLOSED);
        }
    }

    private void validateWeeklyCancellationLimit(Long memberId, LocalDateTime classStartAt) {
        LocalDate weekStartDate = classStartAt.toLocalDate()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDateTime weekStart = weekStartDate.atStartOfDay();
        LocalDateTime weekEnd = weekStart.plusWeeks(1);
        long cancellationCount = reservationRepository.countByMemberAndStatusInClassSessionWeek(
                memberId,
                ReservationStatus.CANCELLED,
                weekStart,
                weekEnd
        );
        if (cancellationCount >= WEEKLY_CANCELLATION_LIMIT) {
            throw new BusinessException(ErrorCode.WEEKLY_CANCELLATION_LIMIT_EXCEEDED);
        }
    }
}
