package com.facebook.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User extends AbstractEntity {
    @NotBlank(message = "User email is mandatory")
    @Column(unique = true)
    private String email;

    @NotBlank(message = "User password is mandatory")
    private String password;

    @Id
    private Long id;

    private String firstName;

    private String lastName;
}
