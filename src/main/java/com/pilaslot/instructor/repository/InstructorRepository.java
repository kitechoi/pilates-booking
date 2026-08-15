package com.pilaslot.instructor.repository;

import com.pilaslot.instructor.domain.Instructor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstructorRepository extends JpaRepository<Instructor, Long> {
}
