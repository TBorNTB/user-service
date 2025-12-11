package com.sejong.userservice.support.common.security.oauth;

import static com.sejong.userservice.support.common.exception.ExceptionType.NOT_FOUND_USER;

import com.sejong.userservice.domain.role.domain.UserRole;
import com.sejong.userservice.domain.user.domain.User;
import com.sejong.userservice.domain.user.dto.request.UserUpdateRequest;
import com.sejong.userservice.domain.user.repository.UserRepository;
import com.sejong.userservice.support.common.exception.BaseException;
import com.sejong.userservice.support.common.security.oauth.dto.CustomOAuth2User;
import com.sejong.userservice.support.common.security.oauth.dto.GithubResponse;
import com.sejong.userservice.support.common.security.oauth.dto.OAuth2Response;
import com.sejong.userservice.support.common.security.oauth.dto.UserDTO;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);
        System.out.println(oAuth2User);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2Response oAuth2Response = null;
        if (registrationId.equals("github")) {

            oAuth2Response = new GithubResponse(oAuth2User.getAttributes());
        } else {

            return null;
        }
        String username = oAuth2Response.getProvider() + " " + oAuth2Response.getProviderId();
        User existData = userRepository.findByUsername(username).orElseThrow(() -> new BaseException(NOT_FOUND_USER));

        if (existData == null) {

            User user = User.builder()
                    .username(username)
                    .nickname(oAuth2Response.getNickname())
                    .email(oAuth2Response.getEmail())
                    .encryptPassword(bCryptPasswordEncoder.encode(UUID.randomUUID().toString())) // OAuth2 사용자용 더미 비밀번호
                    .role(UserRole.GUEST)
                    .profileImageUrl(oAuth2Response.getAvatarUrl())
                    .build();

            userRepository.save(user);

            UserDTO userDTO = new UserDTO();
            userDTO.setUsername(username);
            userDTO.setName(oAuth2Response.getName());
            userDTO.setRole("ROLE_USER");

            return new CustomOAuth2User(userDTO);
        } else {
            // 기존 사용자 정보 업데이트
            UserUpdateRequest updateRequest = new UserUpdateRequest();
            updateRequest.setEmail(oAuth2Response.getEmail());
            updateRequest.setRealName(oAuth2Response.getName());
            
            existData.updateProfile(updateRequest);
            userRepository.save(existData);

            UserDTO userDTO = new UserDTO();
            userDTO.setUsername(existData.getUsername());
            userDTO.setName(oAuth2Response.getName());
            userDTO.setRole(existData.getRole().toString());

            return new CustomOAuth2User(userDTO);
        }
    }
}
