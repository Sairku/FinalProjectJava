package com.facebook.controller;

import com.facebook.annotation.CurrentUser;
import com.facebook.dto.*;
import com.facebook.openapi.ErrorResponseWrapper;
import com.facebook.openapi.NotFoundResponseWrapper;
import com.facebook.openapi.UserDetailsWrapper;
import com.facebook.service.PostService;
import com.facebook.service.UserService;
import com.facebook.util.ResponseHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/api/users")
@Tag(name = "Users API", description = "Endpoints for user operations")
public class UserController {
    private final UserService userService;
    private final PostService postService;

    @Operation(
            summary = "Get current user details",
            description = "Retrieve details of current user",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "User details retrieved successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            implementation = UserDetailsWrapper.class
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "User not found",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            implementation = NotFoundResponseWrapper.class
                                    )
                            )
                    )
            }
    )
    @GetMapping("/current")
    public ResponseEntity<?> geCurrentUserDetails(
            @Parameter(hidden = true)
            @CurrentUser UserAuthDto currentUser
    ) {
        UserDetailsDto userDetails = userService.getCurrentUserDetails(currentUser.getId());

        return ResponseHandler.generateResponse(
                HttpStatus.OK,
                false,
                "User details retrieved successfully",
                userDetails
        );
    }

    @Operation(
            summary = "Get user details",
            description = "Retrieve details of a user by ID",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "User details retrieved successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            implementation = UserDetailsWrapper.class
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "User not found",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            implementation = NotFoundResponseWrapper.class
                                    )
                            )
                    )
            }
    )
    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserDetails(
            @PathVariable long userId,
            // Hide from Swagger UI
            @Parameter(hidden = true)
            @CurrentUser UserAuthDto currentUser
    ) {
        Long currentUserId = currentUser.getId();
        UserDetailsDto userDetails = currentUserId.equals(userId) ?
                userService.getCurrentUserDetails(userId) :
                userService.getUserDetails(userId, currentUserId);

        return ResponseHandler.generateResponse(
                HttpStatus.OK,
                false,
                "User details retrieved successfully",
                userDetails
        );
    }

    @Operation(
            summary = "Update user date",
            description = "Update details of a user by ID",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "User updated successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            implementation = UserDetailsWrapper.class
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "You are not authorized to update this user",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            implementation = ErrorResponseWrapper.class
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "User not found",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            implementation = NotFoundResponseWrapper.class
                                    )
                            )
                    )
            }
    )
    @PutMapping
    public ResponseEntity<?> updateUser(
            @RequestBody @Valid UserUpdateRequestDto userUpdateRequestDto,
            @Parameter(hidden = true)
            @CurrentUser UserAuthDto currentUser
    ) {
        long currentUserId = currentUser.getId();
        UserDetailsDto updatedUser = userService.updateUser(currentUserId, userUpdateRequestDto);

        return ResponseHandler.generateResponse(
                HttpStatus.OK,
                false,
                "User updated successfully",
                updatedUser
        );
    }

    @Operation(
            summary = "Get all users",
            description = "Retrieve a paginated list of all users except the current user",
            parameters = {
                    @Parameter(name = "page", description = "Page number (default is 0)"),
                    @Parameter(name = "size", description = "Number of users per page (default is 10)")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Users retrieved successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            type = "object",
                                            example = """
                                                    {
                                                      "error": false,
                                                      "message": "Users retrieved successfully",
                                                      "data": {
                                                        "content": [
                                                          {
                                                            "id": 1,
                                                            "firstName": "John",
                                                            "lastName": "Doe"
                                                          }
                                                        ],
                                                        "totalElements": 1,
                                                        "totalPages": 1,
                                                        "size": 10,
                                                        "number": 0
                                                      }
                                                    }
                                                """)
                            )
                    )
            }
    )
    @GetMapping
    public ResponseEntity<?> getAllUsers(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @Parameter(hidden = true)
            @CurrentUser UserAuthDto currentUser
    ) {
        Page<UserShortDto> users = userService.getAllUsersExceptCurrent(currentUser.getId(), page, size);
        PageResponseDto<UserShortDto> response = new PageResponseDto<>(users);

        return ResponseHandler.generateResponse(
                HttpStatus.OK,
                false,
                "Users retrieved successfully",
                response
        );
    }

    @Operation(
            summary = "Get user posts",
            description = "Retrieve all posts made by certain user",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "User posts retrieved successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            type = "object",
                                            example = """
                                                    {
                                                      "error": false,
                                                      "message": "User posts retrieved successfully",
                                                      "data": [
                                                        {
                                                          "id": 1,
                                                          "text": "Hello World!",
                                                          "createdDate": "2023-10-01T12:00:00Z",
                                                          "likesCount": 10,
                                                          "commentsCount": 5,
                                                          "repostsCount": 2
                                                        }
                                                      ]
                                                    }
                                                """)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "User not found",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            implementation = NotFoundResponseWrapper.class
                                    )
                            )
                    )
            }
    )
    @GetMapping("/{userId}/posts")
    public ResponseEntity<?> getUserPosts(
            @PathVariable long userId
    ) {
        List<PostResponseDto> posts = postService.getUserPosts(userId);

        return ResponseHandler.generateResponse(
                HttpStatus.OK,
                false,
                "User posts retrieved successfully",
                posts
        );
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchUsersByFullName(@RequestParam String query) {
        List<UserShortDto> maybeUsers = userService.searchUsersByFullName(query);

        String responseMessage = !maybeUsers.isEmpty()
                ? String.format("The search by \"%s\" yielded results", query)
                : String.format("No users found for \"%s\"", query);

        return ResponseHandler.generateResponse(
                HttpStatus.OK,
                false,
                responseMessage,
                maybeUsers
        );
    }
}
