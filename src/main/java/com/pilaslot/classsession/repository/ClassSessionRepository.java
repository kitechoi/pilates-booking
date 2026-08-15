package com.pilaslot.classsession.repository;

import com.pilaslot.classsession.domain.ClassSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClassSessionRepository extends JpaRepository<ClassSession, Long> {
}
