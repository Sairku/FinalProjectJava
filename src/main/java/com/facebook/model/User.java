package com.facebook.model;

import com.facebook.enums.Gender;
import com.facebook.enums.Provider;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@EntityListeners(AuditingEntityListener.class)
public class User extends AbstractEntity {
    @Column(unique = true)
    @NotBlank(message = "Email is mandatory")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Password is mandatory")
    private String password;

    @Column(name = "first_name")
    @NotBlank(message = "First name is mandatory")
    private String firstName;

    @Column(name = "last_name")
    @NotBlank(message = "Last name is mandatory")
    private String lastName;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(length = 20)
    private String phone;

    private Date birthdate;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "header_photo_url")
    private String headerPhotoUrl;

    @Column(name = "home_city")
    private String homeCity;

    @Column(name = "current_city")
    private String currentCity;

    @Column(columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean verified;

    @Column(name = "updated_at")
    @LastModifiedDate
    private LocalDateTime modifiedDate;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(20) DEFAULT 'LOCAL'")
    private Provider provider;
}
