package com.sejong.userservice.application.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    public SecurityConfig() {
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

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

        //경로별 인가 작업
        http
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers("/login", "/", "/users", "/users/login", "/users/health", "/token/reissue", "/webjars/swagger-ui/**",
                                "/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui/index.html#/",
                                "/internal/**"
                        )
                        .permitAll()
//                        .requestMatchers("/admin").hasRole("ADMIN")
                        .anyRequest().authenticated());

//        http
//                .addFilterBefore(new JwtFilter("X-User-ID", "X-User-Roles"), UsernamePasswordAuthenticationFilter.class);

        //세션 설정 > session을 stateless로 설정
        http
                .sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }
}
