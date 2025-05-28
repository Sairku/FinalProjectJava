package com.facebook.controller;

import com.facebook.annotation.CurrentUser;
import com.facebook.dto.UserAuthDto;
import com.facebook.dto.UserDetailsDto;
import com.facebook.dto.UserUpdateRequestDto;
import com.facebook.openapi.ErrorResponseWrapper;
import com.facebook.openapi.NotFoundResponseWrapper;
import com.facebook.openapi.UserDetailsWrapper;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/api/users")
@Tag(name = "Users API", description = "Endpoints for user operations")
public class UserController {
    private final UserService userService;

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
    @PutMapping("/{userId}")
    public ResponseEntity<?> updateUser(
            @PathVariable long userId,
            @RequestBody @Valid UserUpdateRequestDto userUpdateRequestDto,
            @Parameter(hidden = true)
            @CurrentUser UserAuthDto currentUser
    ) {
        Long currentUserId = currentUser.getId();

        if (!currentUserId.equals(userId)) {
            return ResponseHandler.generateResponse(
                    HttpStatus.FORBIDDEN,
                    true,
                    "You are not authorized to update this user",
                    null
            );
        }

        UserDetailsDto updatedUser = userService.updateUser(userId, userUpdateRequestDto);

        return ResponseHandler.generateResponse(
                HttpStatus.OK,
                false,
                "User updated successfully",
                updatedUser
        );
    }
}
