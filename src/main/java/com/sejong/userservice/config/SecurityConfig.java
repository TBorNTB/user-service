package com.sejong.userservice.config;

import com.sejong.userservice.common.security.jwt.JWTUtil;
import com.sejong.userservice.common.security.jwt.JwtExceptionFilter;
import com.sejong.userservice.common.security.jwt.JwtFilter;
import com.sejong.userservice.common.security.oauth.CustomOAuth2UserService;
import com.sejong.userservice.common.security.oauth.CustomSuccessHandler;
import com.sejong.userservice.domain.token.TokenBlacklistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final JWTUtil jwtUtil;
    private final CustomSuccessHandler customSuccessHandler;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final TokenBlacklistRepository tokenBlacklistRepository;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        //csrf disable
        http
                .csrf((auth) -> auth.disable());

        //From 로그인 방식 disable
        http
                .formLogin((auth) -> auth.disable());

        //http basic 인증 방식 disable
        http
                .httpBasic((auth) -> auth.disable());

        //세션 설정 > session을 stateless로 설정
        http
                .sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // OAuth2 로그인 설정
        http
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfoEndpointConfig -> userInfoEndpointConfig
                                .userService(customOAuth2UserService))
                        .successHandler(customSuccessHandler));

        //경로별 인가 작업
        http
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers("/", "/users","/users/role/**", "/users/login", "/users/health", "/token/reissue", "/webjars/swagger-ui/**",
                                "/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui/index.html#/",
                                "/internal/**", "/oauth2/**", "/login/oauth2/**"
                        )
                        .permitAll()
//                        .requestMatchers("/admin").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .addFilterBefore(new JwtExceptionFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new JwtFilter(jwtUtil, tokenBlacklistRepository), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
