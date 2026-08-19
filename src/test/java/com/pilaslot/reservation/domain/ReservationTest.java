package com.pilaslot.reservation.domain;

import com.pilaslot.classsession.domain.ClassSession;
import com.pilaslot.classsession.domain.ClassSessionStatus;
import com.pilaslot.classsession.domain.ClassType;
import com.pilaslot.instructor.domain.Instructor;
import com.pilaslot.member.domain.Member;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ReservationTest {

    @Test
    void reserveCreatesValidInitialState() {
        Member member = new Member("member-001", "encoded-password", "Member", "010-0000-0000");
        Instructor instructor = new Instructor("Instructor", null);
        ClassSession classSession = new ClassSession(
                instructor,
                ClassType.REFORMER,
                LocalDateTime.of(2026, 8, 22, 13, 0),
                50,
                LocalDateTime.of(2026, 8, 15, 13, 0),
                4,
                ClassSessionStatus.SCHEDULED
        );
        LocalDateTime reservedAt = LocalDateTime.of(2026, 8, 16, 10, 0);

        Reservation reservation = Reservation.reserve(member, classSession, reservedAt);

        assertThat(reservation.getMember()).isSameAs(member);
        assertThat(reservation.getClassSession()).isSameAs(classSession);
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.RESERVED);
        assertThat(reservation.getReservedAt()).isEqualTo(reservedAt);
        assertThat(reservation.getCancelledAt()).isNull();
    }

    @Test
    void cancelChangesOnlyCancellationState() {
        Member member = new Member("member-001", "encoded-password", "Member", "010-0000-0000");
        ClassSession classSession = new ClassSession(
                new Instructor("Instructor", null),
                ClassType.REFORMER,
                LocalDateTime.of(2026, 8, 22, 13, 0),
                50,
                LocalDateTime.of(2026, 8, 15, 13, 0),
                4,
                ClassSessionStatus.SCHEDULED
        );
        LocalDateTime reservedAt = LocalDateTime.of(2026, 8, 16, 10, 0);
        LocalDateTime cancelledAt = LocalDateTime.of(2026, 8, 19, 13, 0);
        Reservation reservation = Reservation.reserve(member, classSession, reservedAt);

        reservation.cancel(cancelledAt);

        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
        assertThat(reservation.getCancelledAt()).isEqualTo(cancelledAt);
        assertThat(reservation.getReservedAt()).isEqualTo(reservedAt);
        assertThat(reservation.getMember()).isSameAs(member);
        assertThat(reservation.getClassSession()).isSameAs(classSession);
    }
}
