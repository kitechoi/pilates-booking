package com.pilaslot.auth.dto.response;

import com.pilaslot.member.domain.Member;

public record LoginResponse(
        String accessToken,
        String tokenType,
        MemberResponse member
) {

    private static final String BEARER_TOKEN_TYPE = "Bearer";

    public static LoginResponse of(String accessToken, Member member) {
        return new LoginResponse(
                accessToken,
                BEARER_TOKEN_TYPE,
                new MemberResponse(member.getId(), member.getMemberNumber(), member.getName())
        );
    }

    public record MemberResponse(
            Long id,
            String memberNumber,
            String name
    ) {
    }
}
