package com.sejong.userservice.application.service;

import com.sejong.userservice.api.controller.dto.CustomUserDetails;
import com.sejong.userservice.domain.model.User;
import com.sejong.userservice.domain.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);

        if (user != null) {
            return new CustomUserDetails(user);
        }

        throw new UsernameNotFoundException("엥 그런 유저 네임은 없어요!!!");
    }

}
