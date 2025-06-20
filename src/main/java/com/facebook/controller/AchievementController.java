package com.facebook.controller;

import com.facebook.annotation.CurrentUser;
import com.facebook.dto.AchievementResponseDto;
import com.facebook.dto.UserAuthDto;
import com.facebook.model.User;
import com.facebook.service.UserAchievementService;
import com.facebook.service.UserService;
import com.facebook.util.ResponseHandler;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/achievements")
@RequiredArgsConstructor
@Tag(name = "Achievement API", description = "Endpoints for achievement operations")
public class AchievementController {
    private final UserAchievementService userAchievementService;
    private final UserService userService;
    @GetMapping("/achievements")
    public ResponseEntity<?> getUserAchievements(
            @Parameter(hidden = true) @CurrentUser UserAuthDto currentUser
    ) {
        long userId = currentUser.getId();
        User user = userService.findUserById(userId);
        List<AchievementResponseDto> achievements = userAchievementService.getAllAchievementsOfUser(user);

        return ResponseHandler.generateResponse(
                HttpStatus.OK,
                false,
                "Achievements retrieved successfully",
                achievements
        );
    }
}
