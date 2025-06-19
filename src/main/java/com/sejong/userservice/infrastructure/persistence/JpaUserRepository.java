package com.sejong.userservice.infrastructure.persistence;

import com.sejong.userservice.domain.model.User;
import com.sejong.userservice.domain.repository.UserRepository;
import java.util.Optional;
import javax.swing.text.html.Option;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class JpaUserRepository implements UserRepository{

    private final SpringDataJpaUserRepository springDataJpaUserRepository;

    public JpaUserRepository(SpringDataJpaUserRepository springDataJpaUserRepository) {
        this.springDataJpaUserRepository = springDataJpaUserRepository;
    }

    @Override
    public User save(User user) {
        return springDataJpaUserRepository.save(UserEntity.from(user)).toDomain();
    }

    @Override
    public boolean existsByUsername(String loginId) {
        return springDataJpaUserRepository.existsByUsername(loginId);
    }

    @Override
    public User findByUsername(String username) {
        Optional<UserEntity> userEntityOptional = springDataJpaUserRepository.findByUsername(username);

        return userEntityOptional.map(UserEntity::toDomain) // UserEntity가 존재하면 toDomain() 호출
                .orElse(null); // UserEntity가 없으면 null 반환
    }

    @Override
    public List<User> findAllUsers() {
        return springDataJpaUserRepository.findAll().stream().map(UserEntity::toDomain).toList();
    }

    @Override
    @Transactional
    public void deleteByUsername(String username) {
        springDataJpaUserRepository.deleteByUsername(username);
    }
}
