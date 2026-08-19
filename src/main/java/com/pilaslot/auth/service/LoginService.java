package com.pilaslot.auth.service;

import com.pilaslot.auth.dto.request.LoginRequest;
import com.pilaslot.auth.dto.response.LoginResponse;
import com.pilaslot.global.exception.BusinessException;
import com.pilaslot.global.exception.ErrorCode;
import com.pilaslot.global.security.JwtTokenProvider;
import com.pilaslot.member.domain.Member;
import com.pilaslot.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LoginService {

    private static final String DUMMY_PASSWORD_HASH =
            "$2y$10$lBUjcpBVUpsslW8msLuDM.VWgCJyZs8loEqHye/vwXxmAZJzReaMG";

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        Member member = memberRepository.findByMemberNumber(request.memberNumber())
                .orElse(null);
        String encodedPassword = member != null
                ? member.getPassword()
                : DUMMY_PASSWORD_HASH;
        boolean passwordMatches = passwordEncoder.matches(
                request.password(),
                encodedPassword
        );

        if (member == null || !passwordMatches) {
            throw invalidCredentials();
        }

        String accessToken = jwtTokenProvider.createAccessToken(member.getId());
        return LoginResponse.of(accessToken, member);
    }

    private static BusinessException invalidCredentials() {
        return new BusinessException(ErrorCode.INVALID_LOGIN_CREDENTIALS);
    }
}
