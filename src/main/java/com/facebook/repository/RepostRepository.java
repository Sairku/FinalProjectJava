package com.facebook.repository;

import com.facebook.model.Repost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RepostRepository extends JpaRepository<Repost, Long> {
    Optional<Repost> findByUserIdAndPostId(Long userId, Long postId);
    Optional<List<Repost>> findAllByUserId(Long userId);
}
