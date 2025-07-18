package com.facebook.repository;

import com.facebook.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Page<User> findAllByIdNotOrderByCreatedDateDesc(Long excludedUserId, Pageable pageable);
    List<User> findAllByIdNot(Long excludedUserId);
    List<User> findTop40ByIdNot(Long excludedUserId);
    List<User> findTop40ByIdNotOrderByCreatedDateDesc(Long excludedUserId);
    Optional<List<User>> findAllByFirstNameAndLastName(String firstName, String lastName);
}
