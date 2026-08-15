package com.pilaslot.persistence;

import com.pilaslot.classsession.domain.ClassSession;
import com.pilaslot.classsession.domain.ClassSessionStatus;
import com.pilaslot.classsession.domain.ClassType;
import com.pilaslot.classsession.repository.ClassSessionRepository;
import com.pilaslot.instructor.domain.Instructor;
import com.pilaslot.instructor.repository.InstructorRepository;
import com.pilaslot.member.domain.Member;
import com.pilaslot.member.repository.MemberRepository;
import com.pilaslot.reservation.domain.Reservation;
import com.pilaslot.reservation.domain.ReservationStatus;
import com.pilaslot.reservation.repository.ReservationRepository;
import com.pilaslot.support.PostgreSqlTestContainerConfiguration;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
@ActiveProfiles("test")
@SpringBootTest
@Import(PostgreSqlTestContainerConfiguration.class)
class JpaRepositoryIntegrationTest {

    private static final LocalDateTime CLASS_START_AT = LocalDateTime.of(2026, 8, 22, 13, 0);

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private InstructorRepository instructorRepository;

    @Autowired
    private ClassSessionRepository classSessionRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void savesAndLoadsAllEntities() {
        TestFixture fixture = persistFixture("happy-path");
        LocalDateTime reservedAt = CLASS_START_AT.minusDays(1);

        Reservation saved = reservationRepository.saveAndFlush(new Reservation(
                fixture.member(),
                fixture.classSession(),
                ReservationStatus.RESERVED,
                reservedAt,
                null
        ));

        entityManager.clear();

        Reservation found = reservationRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getStatus()).isEqualTo(ReservationStatus.RESERVED);
        assertThat(found.getReservedAt()).isEqualTo(reservedAt);
        assertThat(found.getCancelledAt()).isNull();
        assertThat(found.getMember().getMemberNumber()).isEqualTo("member-happy-path");
        assertThat(found.getClassSession().getClassType()).isEqualTo(ClassType.REFORMER);
        assertThat(found.getClassSession().getInstructor().getName()).isEqualTo("Instructor happy-path");
        assertThat(found.getClassSession().getReservedCount()).isZero();
        assertThat(found.getCreatedAt()).isNotNull();
        assertThat(found.getUpdatedAt()).isNotNull();
    }

    @Test
    void rejectsSecondActiveReservationForSameMemberAndClassSession() {
        TestFixture fixture = persistFixture("duplicate");
        LocalDateTime reservedAt = CLASS_START_AT.minusDays(1);

        reservationRepository.saveAndFlush(new Reservation(
                fixture.member(),
                fixture.classSession(),
                ReservationStatus.RESERVED,
                reservedAt,
                null
        ));

        assertThatThrownBy(() -> reservationRepository.saveAndFlush(new Reservation(
                fixture.member(),
                fixture.classSession(),
                ReservationStatus.RESERVED,
                reservedAt.plusMinutes(1),
                null
        ))).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void allowsNewActiveReservationAfterCancelledHistory() {
        TestFixture fixture = persistFixture("rebook");
        LocalDateTime firstReservedAt = CLASS_START_AT.minusDays(2);

        Reservation cancelledHistory = reservationRepository.saveAndFlush(new Reservation(
                fixture.member(),
                fixture.classSession(),
                ReservationStatus.CANCELLED,
                firstReservedAt,
                firstReservedAt.plusHours(1)
        ));

        Reservation newActiveReservation = reservationRepository.saveAndFlush(new Reservation(
                fixture.member(),
                fixture.classSession(),
                ReservationStatus.RESERVED,
                CLASS_START_AT.minusDays(1),
                null
        ));

        assertThat(cancelledHistory.getId()).isNotEqualTo(newActiveReservation.getId());
        assertThat(reservationRepository.findAll()).hasSize(2);
    }

    private TestFixture persistFixture(String suffix) {
        Member member = memberRepository.save(new Member(
                "member-" + suffix,
                "encoded-password",
                "Member " + suffix,
                "010-0000-0000"
        ));
        Instructor instructor = instructorRepository.save(new Instructor(
                "Instructor " + suffix,
                null
        ));
        ClassSession classSession = classSessionRepository.save(new ClassSession(
                instructor,
                ClassType.REFORMER,
                CLASS_START_AT,
                50,
                CLASS_START_AT.minusDays(7),
                4,
                ClassSessionStatus.SCHEDULED
        ));
        return new TestFixture(member, classSession);
    }

    private record TestFixture(Member member, ClassSession classSession) {
    }
}
