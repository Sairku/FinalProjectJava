package com.facebook.service;

import com.facebook.dto.AchievementResponseDto;
import com.facebook.model.Achievement;
import com.facebook.model.User;
import com.facebook.model.UserAchievement;
import com.facebook.repository.AchievementRepository;
import com.facebook.repository.UserAchievementRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserAchievementService {
    private final UserAchievementRepository userAchievementRepository;
    private final AchievementRepository achievementRepository;
    private final ModelMapper modelMapper;


    public void awardAchievement(User user, String achievementName) {
        Optional<Achievement> achievementOpt = achievementRepository.findByName(achievementName);

        if (achievementOpt.isEmpty()) {
            throw new IllegalArgumentException("Unknown achievement name: " + achievementName);
        }

        Achievement achievement = achievementOpt.get();

        Optional<UserAchievement> alreadyAwarded = userAchievementRepository
                .findByUserAndAchievement(user, achievement);

        if (alreadyAwarded.isEmpty()) {
            UserAchievement ua = new UserAchievement();
            ua.setUser(user);
            ua.setAchievement(achievement);
            userAchievementRepository.save(ua);
        }
    }

    public List<AchievementResponseDto> getAllAchievementsOfUser(User user) {
        Optional<List<UserAchievement>> userAchievementsOpt = userAchievementRepository.findAllByUser(user);

        List<Long> achievementIds = userAchievementsOpt
                .orElse(List.of())
                .stream()
                .map(ua -> ua.getAchievement().getId())
                .toList();

        List<Achievement> achievements = achievementRepository.findAllById(achievementIds);

        return achievements.stream()
                .map(achievement -> modelMapper.map(achievement, AchievementResponseDto.class))
                .toList();
    }

    public boolean userHaveAchievement(User user, String achievementName) {
        return getAllAchievementsOfUser(user).stream()
                .anyMatch(achievement -> achievement.getName().equals(achievementName));
    }
}
