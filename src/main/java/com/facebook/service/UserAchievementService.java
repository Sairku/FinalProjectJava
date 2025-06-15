package com.facebook.service;

import com.facebook.model.Achievement;
import com.facebook.model.User;
import com.facebook.model.UserAchievement;
import com.facebook.repository.AchievementRepository;
import com.facebook.repository.UserAchievementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserAchievementService {
    private final UserAchievementRepository userAchievementRepository;
    private final AchievementRepository achievementRepository;

    public void awardAchievement(User user, String achievementName) {
        Optional<Achievement> achievementOpt = achievementRepository.findByName(achievementName);

        if (achievementOpt.isEmpty()) {
            throw new IllegalArgumentException("Unknown achievement code: " + achievementName);
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
}
