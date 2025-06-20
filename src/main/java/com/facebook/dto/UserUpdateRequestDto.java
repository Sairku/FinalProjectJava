package com.facebook.dto;

import com.facebook.annotation.NotBlankIfNotNull;
import com.facebook.annotation.PastAtLeastYears;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.URL;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequestDto {
    @NotBlankIfNotNull(message = "First name cannot be blank")
    @Length(min = 2,
            max = 255,
            message = "First name should be at least 2 characters"
    )
    private String firstName;

    @NotBlankIfNotNull(message = "Last name cannot be blank")
    @Length(min = 2,
            max = 255,
            message = "Last name should be at least 2 characters"
    )
    private String lastName;

    @Pattern(regexp = "^[1-9]\\d{1,3}\\d{4,10}$",
            message = "Invalid phone number. It should follow the format: 1 to 3 digits for country code and 4 to 10 digits for the subscriber number."
    )
    private String phone;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Past(message = "Birthdate must be in the past")
    @PastAtLeastYears(years = 10, message = "You must be at least 10 years old")
    private Date birthdate;

    @URL(message = "Avatar URL must be a valid URL")
    private String avatarUrl;

    @URL(message = "Header photo URL must be a valid URL")
    private String headerPhotoUrl;

    @Length(min = 2,
            max = 255,
            message = "Home city should be at least 2 characters"
    )
    private String homeCity;

    @Length(min = 2,
            max = 255,
            message = "Current city should be at least 2 characters"
    )
    private String currentCity;


}
