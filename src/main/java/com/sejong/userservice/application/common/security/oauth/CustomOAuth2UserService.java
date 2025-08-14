package com.sejong.userservice.application.common.security.oauth;

import com.sejong.userservice.application.common.security.oauth.dto.CustomOAuth2User;
import com.sejong.userservice.application.common.security.oauth.dto.GithubResponse;
import com.sejong.userservice.application.common.security.oauth.dto.OAuth2Response;
import com.sejong.userservice.application.common.security.oauth.dto.UserDTO;
import com.sejong.userservice.core.user.UserRole;
import com.sejong.userservice.infrastructure.user.JpaUserRepository;
import com.sejong.userservice.infrastructure.user.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final JpaUserRepository jpaUserRepository;

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
        UserEntity existData = jpaUserRepository.findByRealName(username);

        if (existData == null) {

            UserEntity userEntity = UserEntity.builder()
                    .username(username)
                    .nickname(oAuth2Response.getNickname())
                    .email(oAuth2Response.getEmail())
                    .role(UserRole.OUTSIDER)
                    .profileImageUrl(oAuth2Response.getAvatarUrl())
                    .build();

            jpaUserRepository.save(userEntity);

            UserDTO userDTO = new UserDTO();
            userDTO.setUsername(username);
            userDTO.setName(oAuth2Response.getName());
            userDTO.setRole("ROLE_USER");

            return new CustomOAuth2User(userDTO);
        } else {

            existData.setEmail(oAuth2Response.getEmail());
            existData.setRealName(oAuth2Response.getName());

            jpaUserRepository.save(existData);

            UserDTO userDTO = new UserDTO();
            userDTO.setUsername(existData.getUsername());
            userDTO.setName(oAuth2Response.getName());
            userDTO.setRole(existData.getRole().toString());

            return new CustomOAuth2User(userDTO);
        }
    }
}
