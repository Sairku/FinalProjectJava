package com.facebook.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "followers")
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class Follower extends AbstractEntity {
    @ManyToOne
    @JoinColumn(
            name = "follower_id",
            foreignKey = @ForeignKey(name = "FK_followers_follower_id"),
            nullable = false
    )
    @JsonIgnore
    private User follower;


    @ManyToOne
    @JoinColumn(
            name = "following_id",
            foreignKey = @ForeignKey(name = "FK_followers_following_id"),
            nullable = false
    )
    @JsonIgnore
    private User following;
}
