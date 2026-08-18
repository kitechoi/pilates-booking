package com.pilaslot.auth.service;

import com.pilaslot.auth.dto.request.LoginRequest;
import com.pilaslot.auth.dto.response.LoginResponse;
import com.pilaslot.global.exception.BusinessException;
import com.pilaslot.global.exception.ErrorCode;
import com.pilaslot.global.security.JwtTokenProvider;
import com.pilaslot.member.domain.Member;
import com.pilaslot.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    private static final String MEMBER_NUMBER = "1234";
    private static final String RAW_PASSWORD = "1234";

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    private PasswordEncoder passwordEncoder;
    private LoginService loginService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        loginService = new LoginService(memberRepository, passwordEncoder, jwtTokenProvider);
    }

    @Test
    void logsInWithMemberNumberAndPassword() {
        Member member = memberWithEncodedPassword();
        given(memberRepository.findByMemberNumber(MEMBER_NUMBER)).willReturn(Optional.of(member));
        given(jwtTokenProvider.createAccessToken(1L)).willReturn("access-token");

        LoginResponse response = loginService.login(new LoginRequest(MEMBER_NUMBER, RAW_PASSWORD));

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.member()).isEqualTo(new LoginResponse.MemberResponse(
                1L,
                MEMBER_NUMBER,
                "홍길동"
        ));
        verify(jwtTokenProvider).createAccessToken(1L);
    }

    @Test
    void hidesWhetherMemberNumberDoesNotExist() {
        given(memberRepository.findByMemberNumber(MEMBER_NUMBER)).willReturn(Optional.empty());

        assertInvalidCredentials(() -> loginService.login(
                new LoginRequest(MEMBER_NUMBER, RAW_PASSWORD)
        ));
    }

    @Test
    void hidesWhetherPasswordDoesNotMatch() {
        given(memberRepository.findByMemberNumber(MEMBER_NUMBER))
                .willReturn(Optional.of(memberWithEncodedPassword()));

        assertInvalidCredentials(() -> loginService.login(
                new LoginRequest(MEMBER_NUMBER, "wrong-password")
        ));
    }

    private Member memberWithEncodedPassword() {
        Member member = new Member(
                MEMBER_NUMBER,
                passwordEncoder.encode(RAW_PASSWORD),
                "홍길동",
                "010-1234-5678"
        );
        ReflectionTestUtils.setField(member, "id", 1L);
        return member;
    }

    private void assertInvalidCredentials(Runnable action) {
        assertThatThrownBy(action::run)
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_LOGIN_CREDENTIALS);
    }
}
