package com.facebook.service;

import com.facebook.dto.FriendDetailsDto;
import com.facebook.dto.UserDetailsDto;
import com.facebook.dto.UserShortDto;
import com.facebook.dto.UserUpdateRequestDto;
import com.facebook.enums.FriendStatus;
import com.facebook.model.User;
import com.facebook.repository.FriendRepository;
import com.facebook.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final FriendRepository friendRepository;
    private final ModelMapper modelMapper;

    public UserDetailsDto getCurrentUserDetails(long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        UserDetailsDto userDetailsDto = modelMapper.map(user, UserDetailsDto.class);

        List<UserShortDto> friends = friendRepository.findByStatusAndUserId(FriendStatus.ACCEPTED, userId)
                .stream()
                .map(friend -> modelMapper.map(friend.getFriend(), UserShortDto.class))
                .toList();
        List<UserShortDto> friendsRequests = friendRepository.findByStatusAndFriendId(FriendStatus.PENDING, userId)
                .stream()
                .map(friend -> modelMapper.map(friend.getFriend(), UserShortDto.class))
                .toList();

        userDetailsDto.setFriends(friends);
        userDetailsDto.setFriendsRequests(friendsRequests);

        return userDetailsDto;
    }

    public FriendDetailsDto getFriendDetails(long userId, long currentUserId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + currentUserId));

        FriendDetailsDto friendDetailsDto = modelMapper.map(user, FriendDetailsDto.class);

        List<UserShortDto> friends = friendRepository.findByStatusAndUserId(FriendStatus.ACCEPTED, userId)
                .stream()
                .map(friend -> modelMapper.map(friend.getFriend(), UserShortDto.class))
                .collect(Collectors.toList());
        List<UserShortDto> friendsRequests = friendRepository.findByStatusAndFriendId(FriendStatus.PENDING, userId)
                .stream()
                .map(friend -> modelMapper.map(friend.getFriend(), UserShortDto.class))
                .toList();
        List<UserShortDto> currentUserFriends = friendRepository.findByStatusAndUserId(FriendStatus.ACCEPTED, currentUserId)
                .stream()
                .map(friend -> modelMapper.map(friend.getFriend(), UserShortDto.class))
                .toList();

        // Find mutual friends (not show add button)
        List<UserShortDto> mutualFriends = friends.stream()
                .filter(currentUserFriends::contains)
                .toList();

        // Remove mutuals from original friends (show add button)
        friends.removeAll(mutualFriends);

        friendDetailsDto.setFriends(friends);
        friendDetailsDto.setFriendsRequests(friendsRequests);
        friendDetailsDto.setMutualFriends(mutualFriends);

        return friendDetailsDto;
    }

    public UserDetailsDto updateUser(long userId, UserUpdateRequestDto updatedData) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        if (updatedData.getFirstName() != null) {
            user.setFirstName(updatedData.getFirstName());
        }

        if (updatedData.getLastName() != null) {
            user.setLastName(updatedData.getLastName());
        }

        if (updatedData.getPhone() != null) {
            user.setPhone(updatedData.getPhone());
        }

        if (updatedData.getBirthdate() != null) {
            user.setBirthdate(updatedData.getBirthdate());
        }

        if (updatedData.getAvatarUrl() != null) {
            user.setAvatarUrl(updatedData.getAvatarUrl());
        }

        if (updatedData.getHeaderPhotoUrl() != null) {
            user.setHeaderPhotoUrl(updatedData.getHeaderPhotoUrl());
        }

        if (updatedData.getHomeCity() != null) {
            user.setHomeCity(updatedData.getHomeCity());
        }

        if (updatedData.getCurrentCity() != null) {
            user.setCurrentCity(updatedData.getCurrentCity());
        }

        User savedUser = userRepository.save(user);

        return modelMapper.map(savedUser, UserDetailsDto.class);
    }
}
