package com.pilaslot.member.config;

import com.pilaslot.member.domain.Member;
import com.pilaslot.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LocalMemberSeederTest {

    @Mock
    private MemberRepository memberRepository;

    private PasswordEncoder passwordEncoder;
    private LocalMemberSeeder localMemberSeeder;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        localMemberSeeder = new LocalMemberSeeder(memberRepository, passwordEncoder);
    }

    @Test
    void storesLocalMemberPasswordAsBcryptHash() {
        given(memberRepository.findByMemberNumber("1234")).willReturn(Optional.empty());

        localMemberSeeder.run(new DefaultApplicationArguments(new String[0]));

        ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
        verify(memberRepository).save(memberCaptor.capture());
        Member savedMember = memberCaptor.getValue();
        assertThat(savedMember.getPassword()).isNotEqualTo("1234");
        assertThat(passwordEncoder.matches("1234", savedMember.getPassword())).isTrue();
    }

    @Test
    void doesNotCreateDuplicateLocalMember() {
        Member existingMember = new Member(
                "1234",
                passwordEncoder.encode("1234"),
                "로컬 회원",
                "010-0000-0000"
        );
        given(memberRepository.findByMemberNumber("1234")).willReturn(Optional.of(existingMember));

        localMemberSeeder.run(new DefaultApplicationArguments(new String[0]));

        verify(memberRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }
}
