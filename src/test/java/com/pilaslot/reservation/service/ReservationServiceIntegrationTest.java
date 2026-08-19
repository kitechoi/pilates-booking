package com.pilaslot.reservation.service;

import com.pilaslot.classsession.domain.ClassSession;
import com.pilaslot.classsession.domain.ClassSessionStatus;
import com.pilaslot.classsession.domain.ClassType;
import com.pilaslot.classsession.repository.ClassSessionRepository;
import com.pilaslot.global.exception.BusinessException;
import com.pilaslot.global.exception.ErrorCode;
import com.pilaslot.instructor.domain.Instructor;
import com.pilaslot.instructor.repository.InstructorRepository;
import com.pilaslot.member.domain.Member;
import com.pilaslot.member.repository.MemberRepository;
import com.pilaslot.reservation.domain.Reservation;
import com.pilaslot.reservation.domain.ReservationStatus;
import com.pilaslot.reservation.dto.response.ReservationCreateResponse;
import com.pilaslot.reservation.repository.ReservationRepository;
import com.pilaslot.support.PostgreSqlTestContainerConfiguration;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
@ActiveProfiles("test")
@SpringBootTest
@Import({
        PostgreSqlTestContainerConfiguration.class,
        ReservationServiceIntegrationTest.FixedClockConfiguration.class
})
class ReservationServiceIntegrationTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 8, 19, 13, 0, 1);

    @Autowired
    private ReservationService reservationService;

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
    void persistsReservationAndReservedCountInOneTransaction() {
        Fixture fixture = persistFixture("success");
        assertThat(fixture.classSession().getReservedCount()).isZero();

        ReservationCreateResponse response = reservationService.reserve(
                fixture.member().getId(),
                fixture.classSession().getId()
        );
        entityManager.flush();
        entityManager.clear();

        Reservation reservation = reservationRepository.findById(response.id()).orElseThrow();
        ClassSession classSession = classSessionRepository.findById(
                fixture.classSession().getId()
        ).orElseThrow();
        assertThat(reservation.getMember().getId()).isEqualTo(fixture.member().getId());
        assertThat(reservation.getClassSession().getId()).isEqualTo(classSession.getId());
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.RESERVED);
        assertThat(reservation.getReservedAt()).isEqualTo(NOW);
        assertThat(reservation.getCancelledAt()).isNull();
        assertThat(classSession.getReservedCount()).isEqualTo(1);
    }

    @Test
    void rejectsDuplicateBeforePartialUniqueIndexIsViolated() {
        Fixture fixture = persistFixture("duplicate");
        reservationService.reserve(fixture.member().getId(), fixture.classSession().getId());
        entityManager.flush();

        assertThatThrownBy(() -> reservationService.reserve(
                fixture.member().getId(),
                fixture.classSession().getId()
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DUPLICATE_RESERVATION);
    }

    private Fixture persistFixture(String suffix) {
        Member member = memberRepository.save(new Member(
                "reservation-" + suffix,
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
                LocalDateTime.of(2026, 8, 21, 15, 0),
                50,
                LocalDateTime.of(2026, 8, 12, 9, 0),
                4,
                ClassSessionStatus.SCHEDULED
        ));
        entityManager.flush();
        return new Fixture(member, classSession);
    }

    private record Fixture(Member member, ClassSession classSession) {
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class FixedClockConfiguration {

        @Bean
        @Primary
        Clock fixedClock() {
            ZoneId zoneId = ZoneId.of("Asia/Seoul");
            Instant instant = NOW.atZone(zoneId).toInstant();
            return Clock.fixed(instant, zoneId);
        }
    }
}
