package com.facebook.repository;

import com.facebook.model.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Long> {
    Page<Message> findAllBySenderIdOrReceiverIdOrderByCreatedDateDesc(Long senderId, Long receiverId, Pageable pageable);
}
