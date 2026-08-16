package com.pilaslot.classsession.dto.response;

import java.time.LocalDate;
import java.util.List;

public record WeeklyClassSessionResponse(
        LocalDate weekStart,
        List<ClassSessionResponse> sessions
) {
}
