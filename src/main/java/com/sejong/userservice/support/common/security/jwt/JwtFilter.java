package com.sejong.userservice.support.common.security.jwt;

import com.sejong.userservice.domain.token.TokenBlacklistRepository;
import com.sejong.userservice.support.common.security.UserContext;

import com.sejong.userservice.support.common.security.oauth.dto.CustomOAuth2User;
import com.sejong.userservice.support.common.security.oauth.dto.UserDTO;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.core.Authentication;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String USER_ROLE_HEADER = "X-User-Role";
    private final JWTUtil jwtUtil;
    private final TokenBlacklistRepository tokenBlacklistRepository;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // OAuth2 관련 경로는 이 필터를 건너뜀
        return path.startsWith("/oauth2/")
               || path.startsWith("/login/oauth2/")
               || path.startsWith("/token/reissue");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // 이미 인증이 되어 있으면 건너뜀

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        // API-Gateway에서 검증된 경우
        String username = request.getHeader(USER_ID_HEADER);
        String role = request.getHeader(USER_ROLE_HEADER);

        log.info("JwtFilter - Headers: X-User-Id={}, X-User-Role={}", username, role);

        if (username != null && role != null) {
            UserContext userContext = UserContext.of(username, role);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userContext,
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + role))
                    );

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.debug("User context set: userId={}, userRole={}", username, role);

            filterChain.doFilter(request, response);
            return;
        }

        // 주의: 권한이 필요한 컨트롤러 메서드의 경우 PreAuthorize로 꼭 권한을 명시할것.
        // userDTO를 생성하여 값 set
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername(username);
        userDTO.setRole(role);

        // UserDetails에 회원 정보 객체 담기
        CustomOAuth2User customOAuth2User = new CustomOAuth2User(userDTO);

        // 스프링 시큐리티 인증 토큰 생성
        Authentication authToken = new UsernamePasswordAuthenticationToken(customOAuth2User, null, customOAuth2User.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        log.info("JWT 토큰 인증 필터가 실행되었습니다. 사용자: {}, 권한: {}인 사용자 SecurityContext를 생성했습니다.", username, role);

        filterChain.doFilter(request, response);
    }
}