package com.pilaslot.classsession.repository;

import com.pilaslot.classsession.domain.ClassSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ClassSessionRepository extends JpaRepository<ClassSession, Long> {

    @Query("""
            SELECT classSession
            FROM ClassSession classSession
            JOIN FETCH classSession.instructor
            WHERE classSession.startAt >= :startAt
              AND classSession.startAt < :endAt
            ORDER BY classSession.startAt ASC
            """)
    List<ClassSession> findAllWithInstructorByStartAtRange(
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt
    );
}
