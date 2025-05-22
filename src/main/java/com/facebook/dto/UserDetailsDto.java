package com.facebook.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserDetailsDto extends UserCurrentDetailsDto {
    private Date createdDate;
    private List<UserShortDto> mutualFriends = new ArrayList<>();
}
