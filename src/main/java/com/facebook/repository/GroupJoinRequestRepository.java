package com.facebook.repository;

import com.facebook.model.GroupJoinRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GroupJoinRequestRepository extends JpaRepository<GroupJoinRequest, Long> {
    Optional<GroupJoinRequest> findTop1ByGroup_IdAndUser_IdAndInitiator_IdOrderByCreatedDateDesc(
            Long groupId, Long userId, Long initiatorId
    );
}
