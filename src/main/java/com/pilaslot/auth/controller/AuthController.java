package com.pilaslot.auth.controller;

import com.pilaslot.auth.dto.request.LoginRequest;
import com.pilaslot.auth.dto.response.LoginResponse;
import com.pilaslot.auth.service.LoginService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final LoginService loginService;

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return loginService.login(request);
    }
}
