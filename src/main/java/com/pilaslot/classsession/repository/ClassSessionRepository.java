package com.pilaslot.classsession.repository;

import com.pilaslot.classsession.domain.ClassSession;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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

    @Query("""
            SELECT classSession
            FROM ClassSession classSession
            JOIN FETCH classSession.instructor
            WHERE classSession.id = :classSessionId
            """)
    Optional<ClassSession> findWithInstructorById(
            @Param("classSessionId") Long classSessionId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000"))
    @Query("""
            SELECT classSession
            FROM ClassSession classSession
            WHERE classSession.id = :classSessionId
            """)
    Optional<ClassSession> findByIdForUpdate(
            @Param("classSessionId") Long classSessionId
    );
}
