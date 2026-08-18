package com.pilaslot.classsession.service;

import com.pilaslot.classsession.domain.ClassSession;
import com.pilaslot.classsession.domain.ClassSessionStatus;
import com.pilaslot.classsession.domain.ClassType;
import com.pilaslot.classsession.domain.ReservationAvailability;
import com.pilaslot.classsession.dto.response.ClassSessionResponse;
import com.pilaslot.classsession.dto.response.WeeklyClassSessionResponse;
import com.pilaslot.classsession.repository.ClassSessionRepository;
import com.pilaslot.instructor.domain.Instructor;
import com.pilaslot.instructor.repository.InstructorRepository;
import com.pilaslot.support.PostgreSqlTestContainerConfiguration;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@ActiveProfiles("test")
@SpringBootTest
@Import({
        PostgreSqlTestContainerConfiguration.class,
        ClassSessionQueryIntegrationTest.FixedClockConfiguration.class
})
class ClassSessionQueryIntegrationTest {

    private static final LocalDate WEEK_START = LocalDate.of(2026, 8, 17);
    private static final LocalDateTime RESERVATION_OPEN_AT =
            LocalDateTime.of(2026, 8, 10, 9, 0);

    @Autowired
    private InstructorRepository instructorRepository;

    @Autowired
    private ClassSessionRepository classSessionRepository;

    @Autowired
    private ClassSessionQueryService classSessionQueryService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager entityManager;

    @Test
    void returnsOnlyWeeklyClassSessionsInStartOrderWithCalculatedValues() {
        Instructor instructor = instructorRepository.save(new Instructor(
                "김필라",
                "https://example.com/instructors/kim.jpg"
        ));
        saveClassSession(instructor, WEEK_START.minusDays(1).atTime(23, 59), ClassType.RANDOM);
        ClassSession later = saveClassSession(
                instructor,
                WEEK_START.plusDays(3).atTime(14, 0),
                ClassType.CHAIR_BARREL
        );
        ClassSession earlier = saveClassSession(
                instructor,
                WEEK_START.atStartOfDay(),
                ClassType.REFORMER
        );
        saveClassSession(instructor, WEEK_START.plusWeeks(1).atStartOfDay(), ClassType.ANIMAL_FLOW);
        classSessionRepository.flush();
        jdbcTemplate.update("UPDATE class_session SET reserved_count = 1 WHERE id = ?", earlier.getId());
        entityManager.clear();

        WeeklyClassSessionResponse result = classSessionQueryService.getWeeklyClassSessions(WEEK_START);

        assertThat(result.weekStart()).isEqualTo(WEEK_START);
        assertThat(result.sessions()).extracting(ClassSessionResponse::classSessionId)
                .containsExactly(earlier.getId(), later.getId());

        ClassSessionResponse first = result.sessions().get(0);
        assertThat(first.classType()).isEqualTo(ClassType.REFORMER);
        assertThat(first.instructor().instructorId()).isEqualTo(instructor.getId());
        assertThat(first.instructor().name()).isEqualTo("김필라");
        assertThat(first.instructor().profileImageUrl())
                .isEqualTo("https://example.com/instructors/kim.jpg");
        assertThat(first.endAt()).isEqualTo(first.startAt().plusMinutes(50));
        assertThat(first.reservedCount()).isEqualTo(1);
        assertThat(first.remainingCount()).isEqualTo(3);
        assertThat(first.reservationAvailability()).isEqualTo(ReservationAvailability.AVAILABLE);
    }

    @Test
    void returnsClassSessionDetailFromPostgreSqlWithInstructorAndCalculatedValues() {
        Instructor instructor = instructorRepository.save(new Instructor(
                "김필라",
                "https://example.com/instructors/kim.jpg"
        ));
        LocalDateTime startAt = WEEK_START.plusDays(3).atTime(14, 0);
        ClassSession classSession = saveClassSession(instructor, startAt, ClassType.REFORMER);
        classSessionRepository.flush();
        jdbcTemplate.update(
                "UPDATE class_session SET reserved_count = 1 WHERE id = ?",
                classSession.getId()
        );
        entityManager.clear();

        ClassSessionResponse result = classSessionQueryService.getClassSession(classSession.getId());

        assertThat(result.classSessionId()).isEqualTo(classSession.getId());
        assertThat(result.classType()).isEqualTo(ClassType.REFORMER);
        assertThat(result.instructor().instructorId()).isEqualTo(instructor.getId());
        assertThat(result.instructor().name()).isEqualTo("김필라");
        assertThat(result.instructor().profileImageUrl())
                .isEqualTo("https://example.com/instructors/kim.jpg");
        assertThat(result.startAt()).isEqualTo(startAt);
        assertThat(result.durationMinutes()).isEqualTo(50);
        assertThat(result.endAt()).isEqualTo(startAt.plusMinutes(50));
        assertThat(result.reservationOpenAt()).isEqualTo(RESERVATION_OPEN_AT);
        assertThat(result.capacity()).isEqualTo(4);
        assertThat(result.reservedCount()).isEqualTo(1);
        assertThat(result.remainingCount()).isEqualTo(3);
        assertThat(result.status()).isEqualTo(ClassSessionStatus.SCHEDULED);
        assertThat(result.reservationAvailability()).isEqualTo(ReservationAvailability.AVAILABLE);
    }

    private ClassSession saveClassSession(
            Instructor instructor,
            LocalDateTime startAt,
            ClassType classType
    ) {
        return classSessionRepository.save(new ClassSession(
                instructor,
                classType,
                startAt,
                50,
                RESERVATION_OPEN_AT,
                4,
                ClassSessionStatus.SCHEDULED
        ));
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class FixedClockConfiguration {

        @Bean
        @Primary
        Clock fixedClock() {
            return Clock.fixed(Instant.parse("2026-08-16T00:00:00Z"), ZoneOffset.UTC);
        }
    }
}
