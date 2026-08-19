package com.pilaslot.reservation.repository;

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
import com.pilaslot.support.PostgreSqlTestContainerConfiguration;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@ActiveProfiles("test")
@SpringBootTest
@Import(PostgreSqlTestContainerConfiguration.class)
class ReservationRepositoryIntegrationTest {

    private static final LocalDateTime WEEK_START = LocalDateTime.of(2026, 8, 17, 0, 0);
    private static final LocalDateTime WEEK_END = LocalDateTime.of(2026, 8, 24, 0, 0);

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private InstructorRepository instructorRepository;

    @Autowired
    private ClassSessionRepository classSessionRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager entityManager;

    @Test
    void countsOnlyMembersActiveReservationsInsideClassSessionWeek() {
        Member targetMember = saveMember("target");
        Member otherMember = saveMember("other");
        Instructor instructor = instructorRepository.save(new Instructor("김필라", null));

        saveReservation(targetMember, saveClassSession(instructor, WEEK_START));
        saveReservation(
                targetMember,
                saveClassSession(instructor, WEEK_END.minusSeconds(1))
        );
        saveReservation(targetMember, saveClassSession(instructor, WEEK_END));
        Reservation cancelledReservation = saveReservation(
                targetMember,
                saveClassSession(instructor, WEEK_START.plusDays(2))
        );
        saveReservation(otherMember, saveClassSession(instructor, WEEK_START.plusDays(3)));
        reservationRepository.flush();
        jdbcTemplate.update(
                "UPDATE reservation SET status = ?, cancelled_at = ? WHERE id = ?",
                ReservationStatus.CANCELLED.name(),
                WEEK_START.plusDays(2).plusHours(1),
                cancelledReservation.getId()
        );
        entityManager.clear();

        long count = reservationRepository.countByMemberAndStatusInClassSessionWeek(
                targetMember.getId(),
                ReservationStatus.RESERVED,
                WEEK_START,
                WEEK_END
        );

        assertThat(count).isEqualTo(2);
    }

    @Test
    void findsReservationOnlyForItsOwner() {
        Member owner = saveMember("lookup-owner");
        Member otherMember = saveMember("lookup-other");
        Instructor instructor = instructorRepository.save(new Instructor("이필라", null));
        Reservation reservation = saveReservation(
                owner,
                saveClassSession(instructor, WEEK_START.plusDays(1))
        );
        reservationRepository.flush();
        entityManager.clear();

        assertThat(reservationRepository.findByIdAndMemberId(
                reservation.getId(),
                owner.getId()
        )).isPresent();
        assertThat(reservationRepository.findByIdAndMemberId(
                reservation.getId(),
                otherMember.getId()
        )).isEmpty();
    }

    @Test
    void countsOnlyMembersCancelledReservationsInsideClassSessionWeek() {
        Member targetMember = saveMember("cancel-target");
        Member otherMember = saveMember("cancel-other");
        Instructor instructor = instructorRepository.save(new Instructor("박필라", null));

        saveCancelledReservation(targetMember, saveClassSession(instructor, WEEK_START));
        saveCancelledReservation(
                targetMember,
                saveClassSession(instructor, WEEK_END.minusSeconds(1))
        );
        saveCancelledReservation(targetMember, saveClassSession(instructor, WEEK_END));
        saveReservation(targetMember, saveClassSession(instructor, WEEK_START.plusDays(2)));
        saveCancelledReservation(
                otherMember,
                saveClassSession(instructor, WEEK_START.plusDays(3))
        );
        reservationRepository.flush();
        entityManager.clear();

        long count = reservationRepository.countByMemberAndStatusInClassSessionWeek(
                targetMember.getId(),
                ReservationStatus.CANCELLED,
                WEEK_START,
                WEEK_END
        );

        assertThat(count).isEqualTo(2);
    }

    private Member saveMember(String suffix) {
        return memberRepository.save(new Member(
                "weekly-" + suffix,
                "encoded-password",
                "Member " + suffix,
                "010-0000-0000"
        ));
    }

    private ClassSession saveClassSession(Instructor instructor, LocalDateTime startAt) {
        return classSessionRepository.save(new ClassSession(
                instructor,
                ClassType.REFORMER,
                startAt,
                50,
                startAt.minusDays(7),
                20,
                ClassSessionStatus.SCHEDULED
        ));
    }

    private Reservation saveReservation(Member member, ClassSession classSession) {
        return reservationRepository.save(Reservation.reserve(
                member,
                classSession,
                classSession.getStartAt().minusDays(1)
        ));
    }

    private Reservation saveCancelledReservation(Member member, ClassSession classSession) {
        Reservation reservation = Reservation.reserve(
                member,
                classSession,
                classSession.getStartAt().minusDays(1)
        );
        reservation.cancel(classSession.getStartAt().minusHours(10));
        return reservationRepository.save(reservation);
    }
}
