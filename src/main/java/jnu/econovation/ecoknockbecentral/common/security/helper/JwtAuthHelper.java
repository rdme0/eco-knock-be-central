package jnu.econovation.ecoknockbecentral.common.security.helper;

import jnu.econovation.ecoknockbecentral.common.security.dto.EcoKnockUserDetails;
import jnu.econovation.ecoknockbecentral.common.security.exception.UnauthorizedException;
import jnu.econovation.ecoknockbecentral.common.security.util.JwtUtil;
import jnu.econovation.ecoknockbecentral.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtAuthHelper {
    private final JwtUtil jwtUtil;
    private final MemberService memberService;

    public Authentication authenticate(String token) {
        if (token == null || token.isBlank()) {
            throw new UnauthorizedException();
        }

        if (!jwtUtil.validateAccessToken(token)) {
            throw new UnauthorizedException();
        }

        Long memberId = jwtUtil.extractId(token);
        if (memberId == null) {
            throw new UnauthorizedException();
        }

        var memberInfo = memberService.get(memberId);
        if (memberInfo == null) {
            throw new UnauthorizedException();
        }

        EcoKnockUserDetails userDetails = new EcoKnockUserDetails(memberInfo);

        return new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
    }
}
