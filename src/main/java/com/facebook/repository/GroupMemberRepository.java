package com.facebook.repository;

import com.facebook.model.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupMemberRepository  extends JpaRepository<GroupMember, Long> {
    Optional<List<GroupMember>> findByGroupId(Long groupId);
    Optional<GroupMember> findByGroupIdAndUserId(Long groupId, Long userId);
}
