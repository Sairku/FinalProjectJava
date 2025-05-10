package com.facebook.model;

import com.facebook.enums.FriendStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Entity
@Table(name = "friends")
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class Friend extends AbstractEntity {
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(20) DEFAULT 'PENDING'")
    private FriendStatus status;

    @ManyToOne
    @JoinColumn(
            name = "user_id",
            foreignKey = @ForeignKey(name = "FK_friends_user_id"),
            nullable = false
    )
    @JsonIgnore
    private User user;

    @ManyToOne
    @JoinColumn(
            name = "friend_id",
            foreignKey = @ForeignKey(name = "FK_friends_friend_id"),
            nullable = false
    )
    @JsonIgnore
    private User friend;

    @Column(name = "accepted_at")
    @LastModifiedDate
    private LocalDateTime acceptedDate;
}
