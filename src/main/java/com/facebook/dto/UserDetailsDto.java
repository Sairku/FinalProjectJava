package com.facebook.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailsDto {
    private long id;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private Date birthdate;
    private String avatarUrl;
    private String headerPhotoUrl;
    private String homeCity;
    private String currentCity;
    private List<UserShortDto> friends = new ArrayList<>();
    private List<UserShortDto> friendsRequests = new ArrayList<>();
    private List<UserShortDto> mutualFriends = new ArrayList<>();
    private Date createdDate;
}
