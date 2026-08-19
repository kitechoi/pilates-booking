package com.pilaslot.classsession.dto.response;

import com.pilaslot.classsession.domain.ClassSession;
import com.pilaslot.classsession.domain.ClassSessionStatus;
import com.pilaslot.classsession.domain.ClassType;
import com.pilaslot.classsession.domain.ReservationAvailability;
import com.pilaslot.instructor.domain.Instructor;

import java.time.LocalDateTime;

public record ClassSessionResponse(
        Long classSessionId,
        ClassType classType,
        InstructorResponse instructor,
        LocalDateTime startAt,
        Integer durationMinutes,
        LocalDateTime endAt,
        LocalDateTime reservationOpenAt,
        Integer capacity,
        Integer reservedCount,
        Integer remainingCount,
        ClassSessionStatus status,
        ReservationAvailability reservationAvailability
) {

    public static ClassSessionResponse from(ClassSession classSession, LocalDateTime now) {
        return new ClassSessionResponse(
                classSession.getId(),
                classSession.getClassType(),
                InstructorResponse.from(classSession.getInstructor()),
                classSession.getStartAt(),
                classSession.getDurationMinutes(),
                classSession.getEndAt(),
                classSession.getReservationOpenAt(),
                classSession.getCapacity(),
                classSession.getReservedCount(),
                classSession.getRemainingCount(),
                classSession.getStatus(),
                ReservationAvailability.calculate(classSession, now)
        );
    }

    public record InstructorResponse(
            Long instructorId,
            String name,
            String profileImageUrl
    ) {

        private static InstructorResponse from(Instructor instructor) {
            return new InstructorResponse(
                    instructor.getId(),
                    instructor.getName(),
                    instructor.getProfileImageUrl()
            );
        }
    }
}
