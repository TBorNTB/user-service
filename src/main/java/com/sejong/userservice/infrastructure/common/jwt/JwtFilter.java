package com.sejong.userservice.infrastructure.common.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    private static final String NICKNAME_HEADER = "X-User-Nickname";
    private static final String ROLES_HEADER = "X-User-Roles";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String username = request.getHeader(NICKNAME_HEADER);
        String roles = request.getHeader(ROLES_HEADER);

        if (username == null || username.isEmpty()) {
            log.info("헤더에 username이 포함되어 있지 않아요! 헤더 이름: {}", NICKNAME_HEADER);
            filterChain.doFilter(request, response);
            return;
        }

        if (roles == null || roles.isEmpty()) {
            log.error("헤더에 roles가 포함되어 있지 않아요! 헤더 이름: {}", ROLES_HEADER);
            filterChain.doFilter(request, response);
            return;
        }

        Collection<? extends GrantedAuthority> authorities = Arrays.stream(roles.split(","))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication authentication =
                new TestingAuthenticationToken(username, null, authorities);
        context.setAuthentication(authentication);

        SecurityContextHolder.setContext(context);
        log.info("헤더 인증 필터가 실행되었습니다. 사용자: {}, 권한: {}인 사용자 SecurityContext를 생성했습니다.", username, roles);

        filterChain.doFilter(request, response);
    }
}