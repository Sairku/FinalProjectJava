package com.facebook.repository;

import com.facebook.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {
    Optional<List<Post>> findAllByUserId(Long userId);

    @Query(value = """
    SELECT COUNT(*) FROM (
        (SELECT p.id
         FROM posts p
         WHERE p.user_id = :userId)
        UNION ALL
        (SELECT p.id
         FROM reposts r
         JOIN posts p ON r.post_id = p.id
         WHERE r.user_id = :userId)
    ) AS combined_posts
    """, nativeQuery = true)
    long countCombinedPosts(@Param("userId") Long userId);

    @Query(value = """
    (SELECT p.*
     FROM posts p
     WHERE p.user_id = :userId)
    UNION ALL
    (SELECT p.*
     FROM reposts r
     JOIN posts p ON r.post_id = p.id
     WHERE r.user_id = :userId)
    ORDER BY created_at DESC
    LIMIT :limit OFFSET :offset
    """, nativeQuery = true)
    List<Post> getCombinedPosts(
            @Param("userId") Long userId,
            @Param("limit") int limit,
            @Param("offset") int offset
    );

    @Query(value = """
    (SELECT p.*
     FROM posts p
     WHERE p.user_id = :userId)
    UNION ALL
    (SELECT p.*
     FROM posts p
     WHERE p.user_id IN :friendsIds)
    ORDER BY created_at DESC
    LIMIT :limit OFFSET :offset
    """, nativeQuery = true)
    List<Post> getUserAndFriendsPosts(
            @Param("userId") Long userId,
            @Param("friendsIds") List<Long> friendsIds,
            @Param("limit") int limit,
            @Param("offset") int offset
    );

    @Query(value = """
    SELECT COUNT(*) FROM (
        (SELECT p.id
         FROM posts p
         WHERE p.user_id = :userId)
        UNION ALL
        (SELECT p.id
         FROM posts p
         WHERE p.user_id IN :friendsIds)
    ) AS combined_posts
    """, nativeQuery = true)
    long countUserAndFriendsPosts(
            @Param("userId") Long userId,
            @Param("friendsIds") List<Long> friendsIds
    );
}
