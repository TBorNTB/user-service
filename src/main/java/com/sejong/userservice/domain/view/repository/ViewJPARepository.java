package com.sejong.userservice.domain.view.repository;

import com.sejong.userservice.domain.view.domain.View;
import com.sejong.userservice.support.common.constants.PostType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ViewJPARepository extends JpaRepository<View, Long> {
    Optional<View> findByPostTypeAndPostId(PostType postType, Long postId);
}
