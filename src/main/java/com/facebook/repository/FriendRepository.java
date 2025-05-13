package com.facebook.repository;

import com.facebook.enums.FriendStatus;
import com.facebook.model.Friend;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FriendRepository extends JpaRepository<Friend, Long> {
    List<Friend> findByStatusAndUserId(FriendStatus status, Long userId);
    List<Friend> findByStatusAndFriendId(FriendStatus status, Long friendId);
    Optional<Friend> findByUserIdAndFriendId(Long userId, Long friendId);
    List<Friend> findByUserId(Long userId);
    List<Friend> findByFriendId(Long friendId);
}
