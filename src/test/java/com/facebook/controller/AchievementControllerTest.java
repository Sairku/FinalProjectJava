package com.facebook.controller;

import com.facebook.dto.AchievementResponseDto;
import com.facebook.dto.UserAuthDto;
import com.facebook.model.User;
import com.facebook.service.UserAchievementService;
import com.facebook.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AchievementControllerTest {

    @Mock
    private UserAchievementService userAchievementService;

    @Mock
    private UserService userService;

    @InjectMocks
    private AchievementController achievementController;

    private UserAuthDto currentUser;

    @BeforeEach
    void setUp() {
        currentUser = new UserAuthDto(
                1L,
                "testuser",
                "password",
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    @Test
    void getUserAchievements_shouldReturnAchievementsSuccessfully() {
        User user = new User();
        Mockito.when(userService.findUserById(1L)).thenReturn(user);

        AchievementResponseDto achievement1 = new AchievementResponseDto();
        AchievementResponseDto achievement2 = new AchievementResponseDto();
        List<AchievementResponseDto> achievements = List.of(achievement1, achievement2);
        Mockito.when(userAchievementService.getAllAchievementsOfUser(user)).thenReturn(achievements);

        ResponseEntity<?> responseEntity = achievementController.getUserAchievements(currentUser);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        Object body = responseEntity.getBody();
        assertNotNull(body);
        assertInstanceOf(Map.class, body);

        @SuppressWarnings("unchecked")
        Map<String, Object> responseMap = (Map<String, Object>) body;

        assertEquals(false, responseMap.get("error"));
        assertEquals("Achievements retrieved successfully", responseMap.get("message"));
        assertEquals(achievements, responseMap.get("data"));
    }
}
