package com.facebook.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserShortDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String avatarUrl;
    private Date birthdate = null;

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (!(obj instanceof UserShortDto currObj)) return false;

        return Objects.equals(id, currObj.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}