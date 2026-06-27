package jnu.econovation.ecoknockbecentral.common.security.helper;

import jnu.econovation.ecoknockbecentral.common.exception.server.InternalServerException;
import jnu.econovation.ecoknockbecentral.common.security.dto.EcoKnockUserDetails;
import jnu.econovation.ecoknockbecentral.common.security.exception.UnauthorizedException;
import jnu.econovation.ecoknockbecentral.common.security.util.JwtUtil;
import jnu.econovation.ecoknockbecentral.member.service.MemberService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class JwtAuthHelper {

    private final JwtUtil jwtUtil;
    private final MemberService memberService;

    public JwtAuthHelper(JwtUtil jwtUtil, MemberService memberService) {
        this.jwtUtil = jwtUtil;
        this.memberService = memberService;
    }

    public Authentication authenticate(String token) {
        if (token == null || token.isBlank()) {
            throw new UnauthorizedException();
        }

        if (!jwtUtil.validateAccessToken(token)) {
            throw new UnauthorizedException();
        }

        Long memberId = jwtUtil.extractId(token);
        if (memberId == null) {
            throw new InternalServerException(
                    new IllegalStateException("토큰이 유효하지만 id 추출 실패")
            );
        }

        var memberInfo = memberService.get(memberId);
        if (memberInfo == null) {
            throw new InternalServerException(
                    new IllegalStateException("토큰이 유효하지만 DB에 해당 회원 정보가 없음")
            );
        }

        EcoKnockUserDetails userDetails = new EcoKnockUserDetails(memberInfo);

        return new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
    }
}
