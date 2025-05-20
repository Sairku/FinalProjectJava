package com.facebook.repository;

import com.facebook.model.GroupJoinRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupJoinRequestRepository extends JpaRepository<GroupJoinRequest, Long> {
    Optional<GroupJoinRequest> findTop1ByGroup_IdAndUser_IdOrderByCreatedDateDesc(
            Long groupId, Long userId
    );

    Optional<List<GroupJoinRequest>> findByGroupId(long groupId);

    Optional<GroupJoinRequest> findByGroupIdAndUserId(long groupId, long userId);
}
