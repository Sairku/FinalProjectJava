package com.facebook.controller;

import com.facebook.annotation.CurrentUser;
import com.facebook.dto.UserAuthDto;
import com.facebook.dto.UserShortDto;
import com.facebook.enums.FriendStatus;
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
    @PostMapping("/add/{friendId}")
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

        friendService.addFriendRequest(userId, friendId);

        log.info("Adding friend request from user {} to user {}", userId, friendId);
        return ResponseHandler.generateResponse(
                HttpStatus.CREATED,
                false,
                "Friend request sent successfully",
                null
        );
    }

    @Operation(
            summary = "Respond to request",
            description = "Giving a response to a friend request",
            parameters = {
                    @Parameter(name = "friendId", description = "ID of the friend request to respond to", required = true),
                    @Parameter(name = "status", description = "Response status (accepted or declined | ACCEPTED or DECLINED)", required = true)
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Friend request responded successfully",
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
                            description = "Bad respond status",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            implementation = ErrorResponseWrapper.class
                                    )
                            )
                    )
            }
    )
    @PostMapping("/respond/{friendId}/{status}")
    public ResponseEntity<?> respondToFriendRequest(@PathVariable Long friendId,
                                                    @PathVariable String status,
                                                    @Parameter(hidden = true)
                                                    @CurrentUser UserAuthDto currentUser) {
        Long userId = currentUser.getId();


        if (Objects.equals(userId, friendId)) {
            return ResponseHandler.generateResponse(
                    HttpStatus.BAD_REQUEST,
                    true,
                    "You cannot respond to your own friend request",
                    null
            );
        }

        status = status.toUpperCase();

        if (!(FriendStatus.ACCEPTED.name().equals(status) ||
                FriendStatus.DECLINED.name().equals(status))
        ) {
            log.info("Invalid status provided: {}", status);

            return ResponseHandler.generateResponse(
                    HttpStatus.NOT_FOUND,
                    true,
                    "Invalid status",
                    null
            );
        }

        friendService.responseToFriendRequest(
                userId,
                friendId,
                FriendStatus.valueOf(status)
        );

        log.info("Responding from user {} to friend {} request with {} status", userId, friendId, status);

        return ResponseHandler.generateResponse(
                HttpStatus.OK,
                false,
                "Friend request " + status.toLowerCase() + " successfully",
                null
        );
    }

    @Operation(
            summary = "Delete friend",
            description = "Delete a friend from the user's friend list",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Friend removed",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            implementation = VoidSuccessResponseWrapper.class
                                    )
                            )
                    )
            }
    )
    @DeleteMapping("/delete/{friendId}")
    public ResponseEntity<?> deleteFriend(@PathVariable Long friendId,
                                          @Parameter(hidden = true)
                                          @CurrentUser UserAuthDto currentUser) {
        Long userId = currentUser.getId();

        friendService.deleteFriend(userId, friendId);

        log.info("Deleting friend with ID: {}", friendId);
        return ResponseHandler.generateResponse(
                HttpStatus.OK,
                false,
                "Friend removed",
                null
        );
    }

    @Operation(
            summary = "Get friends",
            description = "Get all friends of the user",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Friends retrieved successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(type = "array", implementation = UserShortDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "No friends found",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            implementation = ErrorResponseWrapper.class
                                    )
                            )
                    )
            }
    )
    @GetMapping("/get-friends")
    public ResponseEntity<?> getFriends(@Parameter(hidden = true)
                                        @CurrentUser UserAuthDto currentUser) {
        Long userId = currentUser.getId();

        return ResponseHandler.generateResponse(
                HttpStatus.OK,
                false,
                "Friends retrieved successfully",
                friendService.getAllFriendUsers(userId)
        );
    }

    @Operation(
            summary = "Get friend requests",
            description = "Get all users who have sent friend requests",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Friend requests retrieved successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(type = "array", implementation = UserShortDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "No friend requests found",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            implementation = ErrorResponseWrapper.class
                                    )
                            )
                    )
            }
    )
    @GetMapping("/get-requests")
    public ResponseEntity<?> getRequests(@Parameter(hidden = true)
                                         @CurrentUser UserAuthDto currentUser) {
        Long userId = currentUser.getId();

        return ResponseHandler.generateResponse(
                HttpStatus.OK,
                false,
                "Friend requests retrieved successfully",
                friendService.getAllUsersWhoSentRequest(userId)
        );
    }

    @Operation(
            summary = "Get recommended friends",
            description = "Get a list of recommended friends for the user",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Recommended friends retrieved successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(type = "array", implementation = UserShortDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "No recommended friends found",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            implementation = ErrorResponseWrapper.class
                                    )
                            )
                    )
            }
    )
    @GetMapping("/recommended")
    public ResponseEntity<?> getRecommendedFriends(@Parameter(hidden = true)
                                                   @CurrentUser UserAuthDto currentUser) {
        Long userId = currentUser.getId();

        return ResponseHandler.generateResponse(
                HttpStatus.OK,
                false,
                "Recommended friends retrieved successfully",
                friendService.getRecommendedFriends(userId)
        );
    }
}
