package com.facebook.controller;

import com.facebook.dto.FriendRequest;
import com.facebook.service.FriendService;
import com.facebook.util.ResponseHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/api/friends")
public class FriendController {
    private final FriendService friendService;

    @PostMapping("/add")
    public ResponseEntity<Object> addFriend(@RequestBody FriendRequest friendRequest) {
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

    @PutMapping("/respond")
    public ResponseEntity<Object> respondToFriendRequest(@RequestBody FriendRequest friendRequest) {
        log.info("Responding to friend request with ID: {}", friendRequest.getFriendId());
        return friendService.responseToFriendRequest(
                friendRequest.getUserId(),
                friendRequest.getFriendId(),
                friendRequest.getStatus()
        );
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Object> deleteFriend(@RequestBody FriendRequest friendRequest) {
        log.info("Deleting friend with ID: {}", friendRequest.getFriendId());
        return friendService.deleteFriend(friendRequest.getUserId(), friendRequest.getFriendId());
    }
}
