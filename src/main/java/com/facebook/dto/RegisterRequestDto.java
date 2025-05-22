package com.facebook.dto;

import com.facebook.validation.PastAtLeastYears;
import com.facebook.validation.ValidGender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequestDto {
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Password is required")
    @Length(min = 8,
            message = "Password should be at least 8 characters"
    )
    private String password;

    @NotBlank(message = "First name is required")
    @Length(min = 2,
            max = 255,
            message = "First name should be at least 2 characters"
    )
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Length(min = 2,
            max = 255,
            message = "Last name should be at least 2 characters"
    )
    private String lastName;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Birthdate must be in the past")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @PastAtLeastYears(years = 10, message = "You must be at least 10 years old")
    private Date birthdate;

    @NotBlank(message = "Gender is required")
    @ValidGender
    private String gender;
}
