package com.facebook.repository;

import com.facebook.model.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MessageRepository extends JpaRepository<Message, Long> {
    @Query(value = """
                SELECT *
                FROM messages
                WHERE (sender_id = :userId1 AND receiver_id = :userId2)
                   OR (sender_id = :userId2 AND receiver_id = :userId1)
                ORDER BY created_at DESC
            """, nativeQuery = true)
    Page<Message> findConversationBetweenUsers(@Param("userId1") Long userId1, @Param("userId2") Long userId2, Pageable pageable);
}
