package com.facebook.repository;

import com.facebook.model.Achievement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AchievementRepository extends JpaRepository<Achievement,Long> {
    public Optional<Achievement> findByName(String name);
}
