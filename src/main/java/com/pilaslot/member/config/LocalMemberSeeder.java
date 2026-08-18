package com.pilaslot.member.config;

import com.pilaslot.member.domain.Member;
import com.pilaslot.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("local")
@RequiredArgsConstructor
public class LocalMemberSeeder implements ApplicationRunner {

    private static final String MEMBER_NUMBER = "1234";
    private static final String RAW_PASSWORD = "1234";

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        if (memberRepository.findByMemberNumber(MEMBER_NUMBER).isPresent()) {
            return;
        }
        memberRepository.save(new Member(
                MEMBER_NUMBER,
                passwordEncoder.encode(RAW_PASSWORD),
                "로컬 회원",
                "010-0000-0000"
        ));
    }
}
