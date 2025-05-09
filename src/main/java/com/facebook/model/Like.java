package com.facebook.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "likes")
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class Like extends AbstractEntity {
    @ManyToOne
    @JoinColumn(
            name = "user_id",
            foreignKey = @ForeignKey(name = "FK_likes_user_id"),
            nullable = false
    )
    @JsonIgnore
    private User user;

    @ManyToOne
    @JoinColumn(
            name = "post_id",
            foreignKey = @ForeignKey(name = "FK_likes_post_id"),
            nullable = false
    )
    @JsonIgnore
    private Post post;
}
