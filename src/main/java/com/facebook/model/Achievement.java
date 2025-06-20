package com.facebook.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "achievements")
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper=true)
public class Achievement extends AbstractEntity{
    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, unique = true)
    private String description;

    @Column(nullable = false)
    private boolean isPremium = false;
}
