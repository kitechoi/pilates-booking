package com.pilaslot.reservation.domain;

import com.pilaslot.classsession.domain.ClassSession;
import com.pilaslot.global.common.BaseTimeEntity;
import com.pilaslot.member.domain.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "reservation")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reservation extends BaseTimeEntity {

    private static final int CANCELLATION_DEADLINE_HOURS = 8;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "class_session_id", nullable = false)
    private ClassSession classSession;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;

    @Column(name = "reserved_at", nullable = false)
    private LocalDateTime reservedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    private Reservation(
            Member member,
            ClassSession classSession,
            ReservationStatus status,
            LocalDateTime reservedAt,
            LocalDateTime cancelledAt
    ) {
        this.member = member;
        this.classSession = classSession;
        this.status = status;
        this.reservedAt = reservedAt;
        this.cancelledAt = cancelledAt;
    }

    public static Reservation reserve(
            Member member,
            ClassSession classSession,
            LocalDateTime reservedAt
    ) {
        return new Reservation(
                member,
                classSession,
                ReservationStatus.RESERVED,
                reservedAt,
                null
        );
    }

    public void cancel(LocalDateTime cancelledAt) {
        this.status = ReservationStatus.CANCELLED;
        this.cancelledAt = cancelledAt;
    }

    public LocalDateTime getCancellationDeadline() {
        return classSession.getStartAt().minusHours(CANCELLATION_DEADLINE_HOURS);
    }

    public boolean isCancellableAt(LocalDateTime now) {
        return status == ReservationStatus.RESERVED
                && !now.isAfter(getCancellationDeadline());
    }
}
