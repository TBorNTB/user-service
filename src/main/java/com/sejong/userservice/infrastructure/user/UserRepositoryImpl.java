package com.sejong.userservice.infrastructure.user;

import static com.sejong.userservice.common.exception.ExceptionType.NOT_FOUND_USER;

import com.sejong.userservice.common.exception.BaseException;
import com.sejong.userservice.core.user.User;
import com.sejong.userservice.core.user.UserRepository;
import com.sejong.userservice.domain.rolechange.domain.UserRole;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final JpaUserRepository jpaUserRepository;

    @Override
    public User save(User user) {
        // todo: exception 처리
        UserEntity savedUserEntity = jpaUserRepository.save(UserEntity.from(user));
        savedUserEntity.updateUsername();
        return savedUserEntity.toDomain();
    }

    @Override
    public boolean existsByNickname(String nickname) {
        return jpaUserRepository.existsByNickname(nickname);
    }

    @Override
    public User findByUsername(String username) {
        Optional<UserEntity> userEntityOptional = jpaUserRepository.findByUsername(username);

        return userEntityOptional.map(UserEntity::toDomain)
                .orElseThrow(() -> new BaseException(NOT_FOUND_USER));
    }

    @Override
    public List<User> findAllUsers() {
        return jpaUserRepository.findAll().stream().map(UserEntity::toDomain).toList();
    }

    @Override
    public Page<User> findAllUsers(Pageable pageable) {
        Page<UserEntity> pageUserEntities = jpaUserRepository.findAll(pageable);
        List<User> users = pageUserEntities.stream()
                .map(UserEntity::toDomain)
                .toList();

        return new PageImpl<>(
                users,
                pageable,
                pageUserEntities.getTotalElements()
        );
    }

    @Override
    @Transactional
    public void deleteByUsername(String username) {
        jpaUserRepository.deleteByUsername(username);
    }

    @Override
    public List<User> findAllByUsernameIn(List<String> usernames) {
        return usernames.stream().map((username) -> jpaUserRepository.findByUsername(username)
                        .orElseThrow(() -> new BaseException(NOT_FOUND_USER)))
                .map(UserEntity::toDomain)
                .toList();
    }

    @Override
    public User findByEmail(String email) {
        UserEntity userEntity = jpaUserRepository.findByEmail(email).orElseThrow(() -> new BaseException(NOT_FOUND_USER));
        return userEntity.toDomain();
    }

    @Override
    public boolean existsByUsernames(String username, List<String> collaboratorUserNames) {
        if (!jpaUserRepository.existsByUsername(username)) {
            return false;
        }
        for (String collaborator : collaboratorUserNames) {
            if (!jpaUserRepository.existsByUsername(collaborator)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public List<User> findByUsernameIn(List<String> usernames) {
        List<UserEntity> userEntityList = jpaUserRepository.findByUsernameIn(usernames);

        return userEntityList.stream()
                .map(UserEntity::toDomain)
                .toList();
    }

    @Override
    public boolean existsByUsername(String username) {
        if (!jpaUserRepository.existsByUsername(username)) {
            return false;
        }
        return true;
    }

    @Override
    public void updateUserRole(Long id, UserRole userRole) {
        UserEntity userEntity = jpaUserRepository.findById(id)
                .orElseThrow(() -> new BaseException(NOT_FOUND_USER));

        userEntity.updateUserRole(userRole);
    }

    @Override
    public User getUserInfo(String username) {
        UserEntity userEntity = jpaUserRepository.findByUsername(username)
                .orElseThrow(() -> new BaseException(NOT_FOUND_USER));

        return userEntity.toDomain();
    }

    @Override
    public Long findUsersCount() {
       Long count =  jpaUserRepository.findUserCount();

       return count;
    }
}
