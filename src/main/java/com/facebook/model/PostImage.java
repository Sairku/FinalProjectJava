package com.facebook.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "post_images")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class PostImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "image_url")
    private String imageUrl;

    @ManyToOne
    @JoinColumn(
            name = "post_id",
            foreignKey = @ForeignKey(name = "FK_post_images_post_id"),
            nullable = false
    )
    @JsonIgnore
    private Post post;
}
