package com.facebook.model;

import com.facebook.enums.GroupJoinStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "group_join_requests")
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class GroupJoinRequest extends AbstractEntity {
    @ManyToOne
    @JoinColumn(
            name = "group_id",
            foreignKey = @ForeignKey(name = "FK_group_requests_group_id"),
            nullable = false
    )
    private Group group;

    @ManyToOne
    @JoinColumn(
            name = "user_id",
            foreignKey = @ForeignKey(name = "FK_group_requests_user_id"),
            nullable = false
    )
    private User user;

    @ManyToOne
    @JoinColumn(
            name = "initiated_by",
            foreignKey = @ForeignKey(name = "FK_group_requests_initiated_by"),
            nullable = false
    )
    private User initiator;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(20) DEFAULT 'PENDING'")
    private GroupJoinStatus status;
}
