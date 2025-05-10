package com.facebook.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GroupUpdateRequest {

    @Size(max = 255, message = "Group's description length must be less than 255")
    private String description;

    @Size(max = 2083, message = "Image's URL length must be less than 2083")
    private String imgUrl;

    @Pattern(
            regexp = "^#([A-Fa-f0-9]{6})$",
            message = "Color must be in HEX format like #FFFFFF"
    )
    private String color;

    @AssertTrue(message = "Description must not be empty or blank")
    private boolean isDescriptionValid() {
        return description == null || !description.trim().isEmpty();
    }

    @AssertTrue(message = "Image URL must not be empty or blank")
    private boolean isImageUrlValid() {
        return imgUrl == null || !imgUrl.trim().isEmpty();
    }

    @AssertTrue(message = "Color must not be empty or blank")
    private boolean isColorValid() {
        return color == null || !color.trim().isEmpty();
    }
}
