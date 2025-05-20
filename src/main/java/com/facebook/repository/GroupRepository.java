package com.facebook.repository;

import com.facebook.model.Group;
import com.facebook.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupRepository extends JpaRepository<Group, Long> {
    Optional<Group> findByNameAndIsPrivateAndOwner(String name, boolean isPrivate, User owner);
    Optional<List<Group>>  findTop10ByOrderByIdDesc();
}
