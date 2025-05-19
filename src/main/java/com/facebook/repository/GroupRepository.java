package com.facebook.repository;

import com.facebook.model.Group;
import com.facebook.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface GroupRepository extends JpaRepository<Group, Long> {
    Optional<Group> findById(Long id);
    Optional<Group> findByNameAndPrivateGroupAndOwner(String name, boolean isPrivate, User owner);
}
