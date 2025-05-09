package com.facebook.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class Message extends AbstractEntity {
    @NotBlank(message = "Message text is mandatory")
    private String text;

    @ManyToOne
    @JoinColumn(
            name = "sender_id",
            foreignKey = @ForeignKey(name = "FK_messages_sender_id"),
            nullable = false
    )
    @JsonIgnore
    private User sender;

    @ManyToOne
    @JoinColumn(
            name = "receiver_id",
            foreignKey = @ForeignKey(name = "FK_messages_receiver_id"),
            nullable = false
    )
    @JsonIgnore
    private User receiver;

    @Column(name = "updated_at")
    @LastModifiedDate
    private LocalDateTime modifiedDate;
}
