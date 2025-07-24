package com.facebook.repository;

import com.facebook.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Page<User> findAllByIdNotOrderByCreatedDateDesc(Long excludedUserId, Pageable pageable);

    List<User> findAllByIdNot(Long excludedUserId);

    List<User> findTop40ByIdNot(Long excludedUserId);

    List<User> findTop40ByIdNotOrderByCreatedDateDesc(Long excludedUserId);

    @Query(value = """
                    SELECT u.*
                    FROM users u
                    WHERE LOWER(CONCAT(u.first_name, ' ', u.last_name)) LIKE LOWER(CONCAT(:query, '%'))
                    	AND NOT EXISTS (
                    		SELECT 1
                            FROM friends fr
                            WHERE (fr.user_id = :userId AND fr.friend_id = u.id)
                    			OR (fr.user_id = u.id AND fr.friend_id = :userId)
                    	)
            """, nativeQuery = true)
    Optional<List<User>> searchByFullNamePrefix(@Param("userId") long userId, @Param("query") String query);

    @Query(value = """
                    SELECT *
                    FROM users u
                    WHERE 1 = 1
                    	AND (
                    		LOWER(u.first_name) LIKE LOWER(CONCAT('%', :word1, '%'))
                    		OR LOWER(u.last_name) LIKE LOWER(CONCAT('%', :word1, '%'))
                        )
                        AND (
                            LOWER(u.first_name) LIKE LOWER(CONCAT('%', :word2, '%'))
                            OR LOWER(u.last_name) LIKE LOWER(CONCAT('%', :word2, '%'))
                        )
                    	AND NOT EXISTS (
                    		SELECT 1
                    		FROM friends fr
                    		WHERE (fr.user_id = :userId AND fr.friend_id = u.id)
                    			OR (fr.user_id = u.id AND fr.friend_id = :userId)
                    	)
            """, nativeQuery = true)
    Optional<List<User>> searchByTwoWords(
            @Param("userId") long userId,
            @Param("word1") String word1,
            @Param("word2") String word2
    );

    @Query(value = """
                    SELECT u.*
                    FROM users u
                    WHERE LOWER(CONCAT(u.first_name, ' ', u.last_name)) LIKE LOWER(CONCAT('%', :query, '%'))
                    	AND NOT EXISTS (
                    		SELECT 1
                            FROM friends fr
                            WHERE (fr.user_id = :userId AND fr.friend_id = u.id)
                    			OR (fr.user_id = u.id AND fr.friend_id = :userId)
                    	)
            """, nativeQuery = true)
    Optional<List<User>> searchByFullNameContains(@Param("userId") long userId, @Param("query") String query);

}
