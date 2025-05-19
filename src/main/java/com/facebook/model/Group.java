package com.facebook.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "groups")
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class Group extends AbstractEntity {
    @NotBlank(message = "Group name is mandatory")
    private String name;

    private String description;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(columnDefinition = "VARCHAR(7) DEFAULT '#FFFFFF'")
    private String color;

    @Column(
            name="is_private",
            columnDefinition = "BOOLEAN DEFAULT FALSE"
    )
    private boolean privateGroup;

    @ManyToOne
    @JoinColumn(
            name = "created_by",
            foreignKey = @ForeignKey(name = "FK_groups_created_by"),
            nullable = false
    )
    @JsonIgnore
    private User owner;
}
