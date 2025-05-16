package com.facebook.controller;

import com.facebook.dto.FriendRequest;
import com.facebook.enums.FriendStatus;
import com.facebook.model.User;
import com.facebook.service.FriendService;
import com.facebook.util.ResponseHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/api/friends")
public class FriendController {
    private final FriendService friendService;

    @PostMapping("/add")
    public ResponseEntity<Object> addFriend_v1(@RequestBody FriendRequest friendRequest) {
        log.info("Adding friend with ID: {}", friendRequest.getFriendId());

        if (friendRequest.getUserId().equals(friendRequest.getFriendId())) {
            return ResponseHandler.generateResponse(
                    HttpStatus.BAD_REQUEST,
                    true,
                    "You cannot send a friend request to yourself",
                    null
            );
        }
        return friendService.addFriendRequest(friendRequest.getUserId(), friendRequest.getFriendId());
    }

    @GetMapping("/{userId}/add/{friendId}")
    public ResponseEntity<Object> addFriend_v2(@PathVariable Long userId,
                                                @PathVariable Long friendId) {
        log.info("Adding friend with ID: {}", friendId);

        if (Objects.equals(userId, friendId)) {
            return ResponseHandler.generateResponse(
                    HttpStatus.BAD_REQUEST,
                    true,
                    "You cannot send a friend request to yourself",
                    null
            );
        }
        return friendService.addFriendRequest(userId, friendId);
    }

    @PutMapping("/respond")
    public ResponseEntity<Object> respondToFriendRequest_v1(@RequestBody FriendRequest friendRequest) {
        log.info("Responding to friend request with ID: {}", friendRequest.getFriendId());
        return friendService.responseToFriendRequest(
                friendRequest.getUserId(),
                friendRequest.getFriendId(),
                friendRequest.getStatus()
        );
    }

    @GetMapping("/{userId}/respond/{friendId}/{status}")
    public ResponseEntity<Object> respondToFriendRequest_v2(@PathVariable Long userId,
                                                         @PathVariable Long friendId,
                                                         @PathVariable String status) {
        log.info("Responding to friend request with ID: {}", friendId);
        return friendService.responseToFriendRequest(
                userId,
                friendId,
                status.equals("accept") ? FriendStatus.ACCEPTED : FriendStatus.DECLINED
        );
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Object> deleteFriend_v1(@RequestBody FriendRequest friendRequest) {
        log.info("Deleting friend with ID: {}", friendRequest.getFriendId());
        return friendService.deleteFriend(friendRequest.getUserId(), friendRequest.getFriendId());
    }

    @DeleteMapping("/{userId}/delete/{friendId}")
    public ResponseEntity<Object> deleteFriend_v2(@PathVariable Long userId,
                                                  @PathVariable Long friendId) {
        log.info("Deleting friend with ID: {}", friendId);
        return friendService.deleteFriend(userId, friendId);
    }

    @GetMapping("/{userId}/get-friends")
    public ResponseEntity<List<User>> getFriends(@PathVariable Long userId) {
        log.info("Getting friends for user with ID: {}", userId);
        return ResponseEntity.ok(friendService.getAllFriendUsers(userId));
    }

    @GetMapping("/{userId}/recommended")
    public ResponseEntity<List<User>> getRecommendedFriends(@PathVariable Long userId) {
        log.info("Getting recommended friends for user with ID: {}", userId);
        return ResponseEntity.ok(friendService.getRecommendedFriends(userId));
    }
}
