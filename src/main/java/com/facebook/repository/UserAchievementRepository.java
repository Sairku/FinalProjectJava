package com.facebook.repository;

import com.facebook.model.Achievement;
import com.facebook.model.User;
import com.facebook.model.UserAchievement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserAchievementRepository extends JpaRepository<UserAchievement, Long> {
    public Optional<UserAchievement> findByUserAndAchievement(User user, Achievement achievement);
}
