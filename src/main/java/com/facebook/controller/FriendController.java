package com.facebook.controller;

import com.facebook.annotation.CurrentUser;
import com.facebook.dto.UserAuthDto;
import com.facebook.enums.FriendStatus;
import com.facebook.model.User;
import com.facebook.openapi.ErrorResponseWrapper;
import com.facebook.openapi.VoidSuccessResponseWrapper;
import com.facebook.service.FriendService;
import com.facebook.util.ResponseHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/api/friends")
@Tag(name = "Friends management API", description = "Endpoints for friends management")
public class FriendController {
    private final FriendService friendService;

    @Operation(
            summary = "Add friend",
            description = "Add a friend request to a user",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Friend request sent",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            implementation = VoidSuccessResponseWrapper.class
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Validation failed",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            implementation = ErrorResponseWrapper.class
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "You cannot send a friend request to yourself",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            implementation = ErrorResponseWrapper.class
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "Friend request already exists",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            implementation = ErrorResponseWrapper.class
                                    )
                            )
                    )

            }
    )
    @GetMapping("/add/{friendId}")
    public ResponseEntity<?> addFriend(@PathVariable Long friendId,
                                            @Parameter(hidden = true)
                                            @CurrentUser UserAuthDto currentUser) {
        Long userId = currentUser.getId();
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

    @Operation(
            summary = "Respond to request",
            description = "Giving a response to a friend request",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Friend request sent",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            implementation = VoidSuccessResponseWrapper.class
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Validation failed",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            implementation = ErrorResponseWrapper.class
                                    )
                            )
                    )
            }
    )
    @GetMapping("/respond/{friendId}/{status}")
    public ResponseEntity<?> respondToFriendRequest(@PathVariable Long friendId,
                                                         @PathVariable String status,
                                                         @Parameter(hidden = true)
                                                         @CurrentUser UserAuthDto currentUser) {
        Long userId = currentUser.getId();
        log.info("Responding to friend request with ID: {}", friendId);
        return switch (status) {
            case "accept" -> friendService.responseToFriendRequest(
                    userId,
                    friendId,
                    FriendStatus.ACCEPTED
            );
            case "decline" -> friendService.responseToFriendRequest(
                    userId,
                    friendId,
                    FriendStatus.DECLINED
            );
            default -> ResponseHandler.generateResponse(
                    HttpStatus.BAD_REQUEST,
                    true,
                    "Invalid status",
                    null
            );
        };
    }

    @DeleteMapping("/delete/{friendId}")
    public ResponseEntity<?> deleteFriend(@PathVariable Long friendId,
                                               @Parameter(hidden = true)
                                               @CurrentUser UserAuthDto currentUser) {
        Long userId = currentUser.getId();
        log.info("Deleting friend with ID: {}", friendId);
        return friendService.deleteFriend(userId, friendId);
    }

    @GetMapping("/get-friends")
    public ResponseEntity<List<User>> getFriends(@Parameter(hidden = true)
                                                 @CurrentUser UserAuthDto currentUser) {
        Long userId = currentUser.getId();
        log.info("Getting friends for user with ID: {}", userId);
        return ResponseEntity.ok(friendService.getAllFriendUsers(userId));
    }

    @GetMapping("/get-requests")
    public ResponseEntity<List<User>> getRequests(@Parameter(hidden = true)
                                                  @CurrentUser UserAuthDto currentUser) {
        Long userId = currentUser.getId();
        log.info("Getting friends for user with ID: {}", userId);
        return ResponseEntity.ok(friendService.getAllUsersWhoHaveNotYetAccepted(userId));
    }

    @GetMapping("/recommended")
    public ResponseEntity<List<User>> getRecommendedFriends(@Parameter(hidden = true)
                                                            @CurrentUser UserAuthDto currentUser) {
        Long userId = currentUser.getId();
        log.info("Getting recommended friends for user with ID: {}", userId);
        return ResponseEntity.ok(friendService.getRecommendedFriends(userId));
    }
}
