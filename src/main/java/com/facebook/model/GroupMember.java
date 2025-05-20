package com.facebook.model;

import com.facebook.enums.GroupRole;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity
@Table(name = "group_members")
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class GroupMember extends AbstractEntity{

    @ManyToOne
    @JoinColumn(
            name = "group_id",
            foreignKey = @ForeignKey(name = "FK_group_members_group_id"),
            nullable = false
    )
    @JsonIgnore
    private Group group;

    @ManyToOne
    @JoinColumn(
            name = "user_id",
            foreignKey = @ForeignKey(name = "FK_group_members_user_id"),
            nullable = false
    )
    @JsonIgnore
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(20) DEFAULT 'MEMBER'")
    private GroupRole role;

    @Column(name = "joined_at", nullable = false, updatable = false)
    @CreatedDate
    private LocalDateTime joinedDate;
}
