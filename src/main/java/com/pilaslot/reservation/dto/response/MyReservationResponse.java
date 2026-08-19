package com.pilaslot.reservation.dto.response;

import com.pilaslot.classsession.domain.ClassSession;
import com.pilaslot.classsession.domain.ClassType;
import com.pilaslot.instructor.domain.Instructor;
import com.pilaslot.reservation.domain.Reservation;
import com.pilaslot.reservation.domain.ReservationStatus;

import java.time.LocalDateTime;

public record MyReservationResponse(
        Long id,
        ReservationStatus status,
        LocalDateTime reservedAt,
        LocalDateTime cancelledAt,
        boolean cancellable,
        LocalDateTime cancellationDeadline,
        ClassSessionResponse classSession
) {

    public static MyReservationResponse from(Reservation reservation, LocalDateTime now) {
        return new MyReservationResponse(
                reservation.getId(),
                reservation.getStatus(),
                reservation.getReservedAt(),
                reservation.getCancelledAt(),
                reservation.isCancellableAt(now),
                reservation.getCancellationDeadline(),
                ClassSessionResponse.from(reservation.getClassSession())
        );
    }

    public record ClassSessionResponse(
            Long classSessionId,
            ClassType classType,
            LocalDateTime startAt,
            LocalDateTime endAt,
            InstructorResponse instructor
    ) {

        private static ClassSessionResponse from(ClassSession classSession) {
            return new ClassSessionResponse(
                    classSession.getId(),
                    classSession.getClassType(),
                    classSession.getStartAt(),
                    classSession.getStartAt().plusMinutes(classSession.getDurationMinutes()),
                    InstructorResponse.from(classSession.getInstructor())
            );
        }
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
