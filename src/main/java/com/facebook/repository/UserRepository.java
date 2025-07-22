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
    @Query("SELECT u FROM User u WHERE LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT(:query, '%'))")
    Optional<List<User>> searchByFullNamePrefix(@Param("query") String query);

    @Query("""
SELECT u FROM User u
WHERE
    (LOWER(u.firstName) LIKE LOWER(CONCAT('%', :word1, '%'))
    OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :word1, '%')))
AND
    (LOWER(u.firstName) LIKE LOWER(CONCAT('%', :word2, '%'))
    OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :word2, '%')))
""")
    Optional<List<User>> searchByTwoWords(@Param("word1") String word1, @Param("word2") String word2);

    @Query("SELECT u FROM User u WHERE LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :query, '%'))")
    Optional<List<User>> searchByFullNameContains(@Param("query") String query);

}
