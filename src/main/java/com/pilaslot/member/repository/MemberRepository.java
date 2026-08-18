package com.pilaslot.member.repository;

import com.pilaslot.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByMemberNumber(String memberNumber);
}
