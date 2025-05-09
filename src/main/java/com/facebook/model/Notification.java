package com.facebook.model;

import com.facebook.enums.NotificationType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notifications")
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class Notification extends AbstractEntity {
    @NotBlank(message = "Notification text is mandatory")
    @Enumerated(EnumType.STRING)
    private NotificationType type;

    @Column(
            name = "is_read",
            columnDefinition = "BOOLEAN DEFAULT FALSE"
    )
    private boolean isRead;

    @ManyToOne
    @JoinColumn(
            name = "user_id",
            foreignKey = @ForeignKey(name = "FK_notifications_user_id"),
            nullable = false
    )
    @JsonIgnore
    private User user;

    @ManyToOne
    @JoinColumn(
            name = "related_user_id",
            foreignKey = @ForeignKey(name = "FK_notifications_related_user_id"),
            nullable = false
    )
    @JsonIgnore
    private User sender;

    @ManyToOne
    @JoinColumn(
            name = "related_post_id",
            foreignKey = @ForeignKey(name = "FK_notifications_related_post_id"),
            nullable = false
    )
    @JsonIgnore
    private Post post;
}
