package com.pilaslot.reservation.repository;

import com.pilaslot.reservation.domain.Reservation;
import com.pilaslot.reservation.domain.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    Optional<Reservation> findByIdAndMemberId(Long reservationId, Long memberId);

    boolean existsByMemberIdAndClassSessionIdAndStatus(
            Long memberId,
            Long classSessionId,
            ReservationStatus status
    );

    @Query("""
            SELECT COUNT(reservation)
            FROM Reservation reservation
            WHERE reservation.member.id = :memberId
              AND reservation.status = :status
              AND reservation.classSession.startAt >= :weekStart
              AND reservation.classSession.startAt < :weekEnd
            """)
    long countByMemberAndStatusInClassSessionWeek(
            @Param("memberId") Long memberId,
            @Param("status") ReservationStatus status,
            @Param("weekStart") LocalDateTime weekStart,
            @Param("weekEnd") LocalDateTime weekEnd
    );
}
