package jnu.econovation.ecoknockbecentral.common.security.util;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jnu.econovation.ecoknockbecentral.auth.config.AuthPolicyConfig;
import jnu.econovation.ecoknockbecentral.member.dto.MemberInfoDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtil{
    private static final String TOKEN_TYPE_CLAIM = "type";
    private static final String ACCESS_TOKEN_TYPE = "ACCESS";
    private static final String REFRESH_TOKEN_TYPE = "REFRESH";

    @Value("${security.jwt.secret-key}")
    private String secretKey;

    private final AuthPolicyConfig authPolicyConfig;

    private SecretKey key;
    private JwtParser jwtParser;

    public JwtUtil(AuthPolicyConfig authPolicyConfig) {
        this.authPolicyConfig = authPolicyConfig;
    }

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
        this.jwtParser = Jwts.parser().verifyWith(key).build();
    }

    public Long extractId(String token) {
        try {
            String subject = jwtParser.parseSignedClaims(token).getPayload().getSubject();
            return subject != null ? Long.parseLong(subject) : null;
        } catch (JwtException | NumberFormatException e) {
            return null;
        }
    }

    public String extractTokenId(String token) {
        try {
            return jwtParser.parseSignedClaims(token).getPayload().getId();
        } catch (JwtException e) {
            return null;
        }
    }

    public String generateAccessToken(MemberInfoDTO memberInfo) {
        return generateAccessToken(memberInfo, authPolicyConfig.getAccessTokenTTL());
    }

    public String generateAccessToken(MemberInfoDTO memberInfo, Duration ttl) {
        long now = System.currentTimeMillis();

        return Jwts.builder()
                .subject(String.valueOf(memberInfo.getId()))
                .claim("id", memberInfo.getId())
                .claim(TOKEN_TYPE_CLAIM, ACCESS_TOKEN_TYPE)
                .issuedAt(new Date(now))
                .expiration(new Date(now + ttl.toMillis()))
                .signWith(key)
                .compact();
    }

    public String generateRefreshToken(MemberInfoDTO memberInfo) {
        return generateRefreshToken(memberInfo, authPolicyConfig.getRefreshTokenTTL());
    }

    public String generateRefreshToken(MemberInfoDTO memberInfo, Duration ttl) {
        long now = System.currentTimeMillis();

        return Jwts.builder()
                .subject(String.valueOf(memberInfo.getId()))
                .id(UUID.randomUUID().toString())
                .claim("id", memberInfo.getId())
                .claim(TOKEN_TYPE_CLAIM, REFRESH_TOKEN_TYPE)
                .issuedAt(new Date(now))
                .expiration(new Date(now + ttl.toMillis()))
                .signWith(key)
                .compact();
    }

    public boolean validateToken(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }

        try {
            jwtParser.parseSignedClaims(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    public boolean validateAccessToken(String token) {
        return validateTokenType(token, ACCESS_TOKEN_TYPE);
    }

    public boolean validateRefreshToken(String token) {
        return validateTokenType(token, REFRESH_TOKEN_TYPE);
    }

    private boolean validateTokenType(String token, String tokenType) {
        if (!validateToken(token)) {
            return false;
        }

        try {
            String actualType = jwtParser.parseSignedClaims(token)
                    .getPayload()
                    .get(TOKEN_TYPE_CLAIM, String.class);
            return tokenType.equals(actualType);
        } catch (JwtException e) {
            return false;
        }
    }
}
