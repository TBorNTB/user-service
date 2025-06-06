package com.sejong.userservice.infrastructure.persistence;

import com.sejong.userservice.domain.model.User;
import com.sejong.userservice.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class JpaUserRepository implements UserRepository {
    private final SpringDataJpaUserRepository userRepository;
    private final ModelMapper modelMapper;
//  TODO 정적팩터리 메서드나 직접 정의한 Converter를 사용할수 도 있음 논의 필요

    @Override
    public List<User> findAll() {
        return userRepository.findAll()
                .stream()
                .map(entity->{
                    return modelMapper.map(entity, User.class);
                }).toList();
    }
}
