package com.pilaslot.classsession.controller;

import com.pilaslot.classsession.dto.response.ClassSessionResponse;
import com.pilaslot.classsession.dto.response.WeeklyClassSessionResponse;
import com.pilaslot.classsession.service.ClassSessionQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/class-sessions")
public class ClassSessionController {

    private final ClassSessionQueryService classSessionQueryService;

    @GetMapping
    public WeeklyClassSessionResponse getWeeklyClassSessions(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate weekStart
    ) {
        return classSessionQueryService.getWeeklyClassSessions(weekStart);
    }

    @GetMapping("/{classSessionId}")
    public ClassSessionResponse getClassSession(@PathVariable Long classSessionId) {
        return classSessionQueryService.getClassSession(classSessionId);
    }
}
