package jnu.econovation.ecoknockbecentral.common.security.dto;

import jnu.econovation.ecoknockbecentral.member.dto.MemberInfoDTO;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public record EcoKnockUserDetails(
        MemberInfoDTO memberInfo
) implements UserDetails {
    private static final String ROLE_PREFIX = "ROLE_";

    @Override
    public @NonNull String getUsername() {
        return memberInfo.getName();
    }

    @Override
    public String getPassword() {
        return "sso";
    }

    @Override
    public @NonNull Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(ROLE_PREFIX + memberInfo.getRole()));
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}