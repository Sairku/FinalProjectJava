package com.facebook.service;

import com.facebook.dto.AchievementResponseDto;
import com.facebook.model.Achievement;
import com.facebook.model.User;
import com.facebook.model.UserAchievement;
import com.facebook.repository.AchievementRepository;
import com.facebook.repository.UserAchievementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserAchievementServiceTest {

    private UserAchievementRepository userAchievementRepository;
    private AchievementRepository achievementRepository;
    private ModelMapper modelMapper;

    private UserAchievementService service;

    private User user;

    @BeforeEach
    void setUp() {
        userAchievementRepository = mock(UserAchievementRepository.class);
        achievementRepository = mock(AchievementRepository.class);
        modelMapper = new ModelMapper();

        service = new UserAchievementService(userAchievementRepository, achievementRepository, modelMapper);

        user = new User();
        user.setId(1L);
    }

    @Test
    void awardAchievement_shouldSaveIfNotAlreadyAwarded() {
        Achievement achievement = new Achievement();
        achievement.setId(1L);
        achievement.setName("First Login");

        when(achievementRepository.findByName("First Login")).thenReturn(Optional.of(achievement));
        when(userAchievementRepository.findByUserAndAchievement(user, achievement)).thenReturn(Optional.empty());

        service.awardAchievement(user, "First Login");

        verify(userAchievementRepository).save(any(UserAchievement.class));
    }

    @Test
    void awardAchievement_shouldNotSaveIfAlreadyAwarded() {
        Achievement achievement = new Achievement();
        achievement.setId(1L);
        achievement.setName("First Login");

        UserAchievement ua = new UserAchievement();
        ua.setUser(user);
        ua.setAchievement(achievement);

        when(achievementRepository.findByName("First Login")).thenReturn(Optional.of(achievement));
        when(userAchievementRepository.findByUserAndAchievement(user, achievement)).thenReturn(Optional.of(ua));

        service.awardAchievement(user, "First Login");

        verify(userAchievementRepository, never()).save(any(UserAchievement.class));
    }

    @Test
    void awardAchievement_shouldThrowException_whenAchievementNotFound() {
        when(achievementRepository.findByName("Unknown")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.awardAchievement(user, "Unknown"));

        assertEquals("Unknown achievement name: Unknown", ex.getMessage());
    }

    @Test
    void getAllAchievementsOfUser_shouldReturnListOfAchievementDtos() {
        Achievement achievement1 = new Achievement("Sweet & Signed In","First Login", false);
        achievement1.setId(1L);
        Achievement achievement2 = new Achievement("Pink Profile", "Completed your profile + added avatar", false);
        achievement2.setId(2L);

        UserAchievement ua1 = new UserAchievement();
        ua1.setAchievement(achievement1);
        ua1.setUser(user);

        UserAchievement ua2 = new UserAchievement();
        ua2.setAchievement(achievement2);
        ua2.setUser(user);

        when(userAchievementRepository.findAllByUser(user)).thenReturn(Optional.of(List.of(ua1, ua2)));
        when(achievementRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(achievement1, achievement2));

        List<AchievementResponseDto> dtos = service.getAllAchievementsOfUser(user);

        assertEquals(2, dtos.size());
        assertEquals("Sweet & Signed In", dtos.get(0).getName());
        assertEquals("Pink Profile", dtos.get(1).getName());
    }

    @Test
    void getAllAchievementsOfUser_shouldReturnEmptyList_whenUserHasNoAchievements() {
        when(userAchievementRepository.findAllByUser(user)).thenReturn(Optional.of(List.of()));

        List<AchievementResponseDto> dtos = service.getAllAchievementsOfUser(user);

        assertTrue(dtos.isEmpty());
        verify(achievementRepository).findAllById(List.of());
    }

    @Test
    void userHaveAchievement_shouldReturnTrue_whenAchievementExists() {
        Achievement achievement = new Achievement("Pink Profile", "Completed your profile", false);
        achievement.setId(1L);

        when(userAchievementRepository.findAllByUser(user)).thenReturn(
                Optional.of(List.of(new UserAchievement(user, achievement)))
        );
        when(achievementRepository.findAllById(List.of(1L))).thenReturn(List.of(achievement));

        boolean hasAchievement = service.userHaveAchievement(user, "Pink Profile");

        assertTrue(hasAchievement);
    }

    @Test
    void userHaveAchievement_shouldReturnFalse_whenAchievementNotExists() {
        Achievement achievement = new Achievement("Other Achievement", "Desc", false);
        achievement.setId(2L);

        when(userAchievementRepository.findAllByUser(user)).thenReturn(
                Optional.of(List.of(new UserAchievement(user, achievement)))
        );
        when(achievementRepository.findAllById(List.of(2L))).thenReturn(List.of(achievement));

        boolean hasAchievement = service.userHaveAchievement(user, "Pink Profile");

        assertFalse(hasAchievement);
    }

}
