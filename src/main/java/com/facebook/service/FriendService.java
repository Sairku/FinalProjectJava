package com.facebook.service;

import com.facebook.enums.FriendStatus;
import com.facebook.exception.NotFoundException;
import com.facebook.model.Friend;
import com.facebook.model.User;
import com.facebook.repository.FriendRepository;
import com.facebook.repository.UserRepository;
import com.facebook.util.ResponseHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class FriendService {
    private final FriendRepository friendRepository;
    private final UserRepository userRepository;

    public boolean isFriend(Long userId, Long friendId) {
        if (friendRepository.findByUserIdAndFriendId(userId, friendId).isPresent())
            return true;
        else
            return friendRepository.findByUserIdAndFriendId(friendId, userId).isPresent();
    }

    // User trying to get a friend (like object Friend) by his friend's id (friendId)
    public Friend getFriendById(Long userId, Long friendId) {
        return friendRepository.findByUserIdAndFriendId(userId, friendId)
                .orElseThrow(() -> new NotFoundException("Friend not found"));
    }

    // User trying to get a friend (like object User) by his friend's id (friendId)
    public User getUserWhoIsFriendById(Long userId, Long friendId) {
        return getFriendById(userId, friendId).getFriend();
    }

    public List<Friend> getAllFriendRequests(Long userId) {
        return friendRepository.findByUserId(userId);
    }

    // Get all friends (like object Friend) of a user with 'userId'
    public List<Friend> getAllFriends(Long userId) {
        return friendRepository.findByStatusAndUserId(FriendStatus.ACCEPTED, userId);
    }

    // Get all friends (like object User) of a user with 'userId'
    public List<User> getAllFriendUsers(Long userId) {
        return friendRepository.findByStatusAndUserId(FriendStatus.ACCEPTED, userId)
                .stream()
                .map(Friend::getFriend)
                .toList();
    }

    // Get all friends (like object Friend) to whom the user with 'userId' sent a request but not yet accepted
    public List<Friend> getAllFriendsWhoHaveNotYetAccepted(Long userId) {
        return friendRepository.findByStatusAndUserId(FriendStatus.PENDING, userId);
    }

    // Get all friends (like object Friend)
    // who sent a request to the user with 'userId' but user hasn't accepted them yet
    public List<Friend> getAllFriendsWhoSentRequest(Long userId) {
        return friendRepository.findByStatusAndFriendId(FriendStatus.PENDING, userId);
    }

    // Get all friends (like object User) of a user with 'userId'
    public List<User> getAllUsersWhoAreFriends(Long userId) {
        return friendRepository.findByStatusAndUserId(FriendStatus.ACCEPTED, userId)
                .stream()
                .map(Friend::getFriend)
                .toList();
    }

    // Get all friends (like object User) to whom the user with 'userId' sent a request but not yet accepted
    public List<User> getAllUsersWhoHaveNotYetAccepted(Long userId) {
        return friendRepository.findByStatusAndUserId(FriendStatus.PENDING, userId)
                .stream()
                .map(Friend::getFriend)
                .toList();
    }

    // Get all friends (like object User)
    // who sent a request to the user with 'userId' but user hasn't accepted them yet
    public List<User> getAllUsersWhoSentRequest(Long userId) {
        return getAllFriendsWhoSentRequest(userId).stream()
                .map(Friend::getFriend)
                .toList();
    }

    public ResponseEntity<Object> addFriendRequest(Long userId, Long friendId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        User friend = userRepository.findById(friendId)
                .orElseThrow(() -> new NotFoundException("Friend not found"));

        if (friendRepository.findByUserIdAndFriendId(userId, friendId).isPresent() ||
                friendRepository.findByUserIdAndFriendId(friendId, userId).isPresent()) {
            return ResponseHandler.generateResponse(
                    HttpStatus.BAD_REQUEST,
                    true,
                    "Friend request already exists",
                    null
            );
        }

        friendRepository.save(new Friend(FriendStatus.PENDING, user, friend, null));
        log.info("Adding friend request from user {} to user {}", userId, friendId);
        return ResponseHandler.generateResponse(
                HttpStatus.OK,
                false,
                "Friend request sent",
                null
        );
    }

    public ResponseEntity<Object> responseToFriendRequest(Long userId, Long friendId ,FriendStatus status) {
        if (userRepository.findById(userId).isEmpty()) {
            return ResponseHandler.generateResponse(
                    HttpStatus.BAD_REQUEST,
                    true,
                    "User not found",
                    null
            );
        }
        if (userRepository.findById(friendId).isEmpty()) {
            return ResponseHandler.generateResponse(
                    HttpStatus.BAD_REQUEST,
                    true,
                    "Friend not found",
                    null
            );
        }

        Optional<Friend> existingRequest = friendRepository.findByUserIdAndFriendId(friendId, userId);
        if (existingRequest.isEmpty()) {
            return ResponseHandler.generateResponse(
                    HttpStatus.BAD_REQUEST,
                    true,
                    "Friend request doesn't exist",
                    null
            );
        }

        if (status == FriendStatus.ACCEPTED) {
            existingRequest.get().setStatus(FriendStatus.ACCEPTED);
            friendRepository.save(existingRequest.get());
            log.info("Friend request from user {} to user {} accepted", friendId, userId);
            return ResponseHandler.generateResponse(
                    HttpStatus.OK,
                    false,
                    "Friend request accepted",
                    null
            );
        } else if (status == FriendStatus.DECLINED) {
            friendRepository.delete(existingRequest.get());
            log.info("Friend request from user {} to user {} rejected", friendId, userId);
            return ResponseHandler.generateResponse(
                    HttpStatus.OK,
                    false,
                    "Friend request rejected",
                    null
            );
        } else {
            return ResponseHandler.generateResponse(
                    HttpStatus.BAD_REQUEST,
                    true,
                    "Invalid status",
                    null
            );
        }
    }

    public ResponseEntity<Object> deleteFriend(Long userId, Long friendId) {
        if (userRepository.findById(userId).isEmpty()) {
            return ResponseHandler.generateResponse(
                    HttpStatus.BAD_REQUEST,
                    true,
                    "User not found",
                    null
            );
        }
        if (userRepository.findById(friendId).isEmpty()) {
            return ResponseHandler.generateResponse(
                    HttpStatus.BAD_REQUEST,
                    true,
                    "Friend not found",
                    null
            );
        }

        Optional<Friend> firstExistingRequest = friendRepository.findByUserIdAndFriendId(userId, friendId);
        Optional<Friend> secondExistingRequest = friendRepository.findByUserIdAndFriendId(friendId, userId);

        if (firstExistingRequest.isPresent()) {
            friendRepository.delete(firstExistingRequest.get());
            log.info("Deleting friend {} from user {}", friendId, userId);
            return ResponseHandler.generateResponse(
                    HttpStatus.OK,
                    false,
                    "Friend deleted",
                    null
            );
        } else {
            if (secondExistingRequest.isPresent()) {
                friendRepository.delete(secondExistingRequest.get());
                log.info("Deleting friend {} from user {}", userId, friendId);
                return ResponseHandler.generateResponse(
                        HttpStatus.OK,
                        false,
                        "Friend deleted",
                        null
                );
            }
        }

        return ResponseHandler.generateResponse(
                HttpStatus.BAD_REQUEST,
                true,
                "Friend request doesn't exist",
                null
        );

    }

    public List<User> getRecommendedFriends(Long userId) {
        List<User> friends = getAllUsersWhoAreFriends(userId);

        List<User> result = new ArrayList<>();
        boolean enough = false;

        for (User friend : friends) {
            List<User> friendsOfFriend = getAllUsersWhoAreFriends(friend.getId());
            for (User friendOfFriend : friendsOfFriend){
                if (friendOfFriend.getId() != userId && !result.contains(friendOfFriend) && !friends.contains(friendOfFriend))
                    result.add(friendOfFriend);
                if (result.size() >= 40)
                    enough = true;
                if (enough)
                    break;
            }
            if (enough)
                break;
        }

        return result;
    }
}