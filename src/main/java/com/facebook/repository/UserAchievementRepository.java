package com.facebook.repository;

import com.facebook.model.Achievement;
import com.facebook.model.User;
import com.facebook.model.UserAchievement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserAchievementRepository extends JpaRepository<UserAchievement, Long> {
    Optional<UserAchievement> findByUserAndAchievement(User user, Achievement achievement);
    Optional<List<UserAchievement>> findAllByUser(User user);
}
