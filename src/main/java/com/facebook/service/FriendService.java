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
    public Friend getFriendById(Long friendId) {
        return friendRepository.findById(friendId)
                .orElseThrow(() -> new NotFoundException("Friend not found"));
    }

    // User trying to get a friend (like object User) by his friend's id (friendId)
    public User getUserWhoIsMayBeFriend(Long userId) {
        return getFriendById(userId).getUser();
    }

    public List<Friend> getAllFriendRequests(Long userId) {
       return friendRepository.findByUserId(userId);
    }

    // Get all friends (like object Friend) of a user with 'userId'
    public List<Friend> getAllFriends(Long userId) {
        return friendRepository.findByStatusAndUserId(FriendStatus.ACCEPTED, userId);
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
        List<Friend> friends = getAllFriends(userId);
        return userRepository.findAllById(friends.stream()
                .map(Friend::getFriend)
                .map(User::getId)
                .toList());
    }

    // Get all friends (like object User) to whom the user with 'userId' sent a request but not yet accepted
    public List<User> getAllUsersWhoHaveNotYetAccepted(Long userId) {
        List<Friend> friends = getAllFriendsWhoHaveNotYetAccepted(userId);
        return userRepository.findAllById(friends.stream()
                .map(Friend::getFriend)
                .map(User::getId)
                .toList());
    }

    // Get all friends (like object User)
    // who sent a request to the user with 'userId' but user hasn't accepted them yet
    public List<User> getAllUsersWhoSentRequest(Long userId) {
        List<Friend> friends = getAllFriendsWhoSentRequest(userId);
        return userRepository.findAllById(friends.stream()
                .map(Friend::getUser)
                .map(User::getId)
                .toList());
    }

    public ResponseEntity<Object> addFriendRequest(Long userId, Long friendId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        User friend = userRepository.findById(friendId)
                .orElseThrow(() -> new NotFoundException("Friend not found"));

//        if (userRepository.findById(userId).isEmpty()) {
//            return ResponseHandler.generateResponse(
//                HttpStatus.BAD_REQUEST,
//                true,
//                "User not found",
//                null
//            );
//        }
//        if (userRepository.findById(friendId).isEmpty()) {
//            return ResponseHandler.generateResponse(
//                    HttpStatus.BAD_REQUEST,
//                    true,
//                    "Friend not found",
//                    null
//            );
//        }

        Optional<Friend> existingRequest = friendRepository.findByUserIdAndFriendId(userId, friendId);
        if (existingRequest.isPresent()) {
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

        Optional<Friend> existingRequest = friendRepository.findByUserIdAndFriendId(userId, friendId);
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
            log.info("Friend request from user {} to user {} accepted", userId, friendId);
            return ResponseHandler.generateResponse(
                    HttpStatus.OK,
                    false,
                    "Friend request accepted",
                    null
            );
        } else if (status == FriendStatus.DECLINED) {
            friendRepository.delete(existingRequest.get());
            log.info("Friend request from user {} to user {} rejected", userId, friendId);
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

        Optional<Friend> existingRequest = friendRepository.findByUserIdAndFriendId(userId, friendId);
        if (existingRequest.isEmpty()) {
            return ResponseHandler.generateResponse(
                    HttpStatus.BAD_REQUEST,
                    true,
                    "Friend request doesn't exist",
                    null
            );
        }

        friendRepository.delete(existingRequest.get());
        log.info("Deleting friend {} from user {}", friendId, userId);
        return ResponseHandler.generateResponse(
                HttpStatus.OK,
                false,
                "Friend deleted",
                null
        );
    }
}
