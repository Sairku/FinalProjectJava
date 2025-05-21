package com.facebook.controller;

import com.facebook.dto.FriendDetailsDto;
import com.facebook.dto.UserAuthDto;
import com.facebook.dto.UserDetailsDto;
import com.facebook.dto.UserUpdateRequestDto;
import com.facebook.service.UserService;
import com.facebook.util.ResponseHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserDetails(@PathVariable long userId) {
//        UserAuthDto currentUserData = (UserAuthDto) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//        Long currentUserId = currentUserData.getId();
        // TODO: Uncomment the above lines and remove the below line, we need to use token to get the current user
        Long currentUserId;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.getPrincipal() instanceof UserAuthDto currentUserData) {
            currentUserId = currentUserData.getId();
        } else {
            currentUserId = userId;
        }

        if (currentUserId.equals(userId)) {
            UserDetailsDto userDetails = userService.getCurrentUserDetails(userId);

            return ResponseHandler.generateResponse(
                    HttpStatus.OK,
                    false,
                    "User details retrieved successfully",
                    userDetails
            );
        } else {
            FriendDetailsDto friendDetails = userService.getFriendDetails(userId, currentUserId);

            return ResponseHandler.generateResponse(
                    HttpStatus.OK,
                    false,
                    "Friend details retrieved successfully",
                    friendDetails
            );
        }
    }

    @PutMapping("/{userId}")
    public ResponseEntity<?> updateUser(@PathVariable long userId, @RequestBody @Validated UserUpdateRequestDto userUpdateRequestDto) {
        UserAuthDto currentUserData = (UserAuthDto) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long currentUserId = currentUserData.getId();

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
