package com.facebook.service;

import com.facebook.dto.UserDetailsDto;
import com.facebook.dto.UserShortDto;
import com.facebook.dto.UserUpdateRequestDto;
import com.facebook.enums.FriendStatus;
import com.facebook.exception.NotFoundException;
import com.facebook.model.User;
import com.facebook.repository.FriendRepository;
import com.facebook.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final FriendRepository friendRepository;
    private final UserAchievementService userAchievementService;
    private final ModelMapper modelMapper;

    public UserDetailsDto getCurrentUserDetails(long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with ID: " + userId));

        UserDetailsDto userCurrentDetailsDto = modelMapper.map(user, UserDetailsDto.class);

        List<UserShortDto> friends = friendRepository.findByStatusAndUserId(FriendStatus.ACCEPTED, userId)
                .stream()
                .map(friend -> modelMapper.map(friend.getFriend(), UserShortDto.class))
                .toList();
        List<UserShortDto> friendsRequests = friendRepository.findByStatusAndFriendId(FriendStatus.PENDING, userId)
                .stream()
                .map(friend -> modelMapper.map(friend.getFriend(), UserShortDto.class))
                .toList();

        userCurrentDetailsDto.setFriends(friends);
        userCurrentDetailsDto.setFriendsRequests(friendsRequests);

        return userCurrentDetailsDto;
    }

    public Page<UserShortDto> getAllUsersExceptCurrent(long currentUserId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> usersPage = userRepository.findAllByIdNotOrderByCreatedDateDesc(currentUserId, pageable);

        return usersPage.map(user -> modelMapper.map(user, UserShortDto.class));
    }

    public UserDetailsDto getUserDetails(long userId, long currentUserId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with ID: " + userId));
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new NotFoundException("Not found user with ID: " + currentUserId));

        UserDetailsDto friendDetailsDto = modelMapper.map(user, UserDetailsDto.class);

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
                .orElseThrow(() -> new NotFoundException("Not found user with ID: " + userId));

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
        String achievementName = "Pink Profile";
        if (allFieldsAreFilled(user) && !userAchievementService.userHaveAchievement(user, achievementName)) {
            userAchievementService.awardAchievement(user, achievementName);
        }

        return modelMapper.map(savedUser, UserDetailsDto.class);
    }

    private boolean allFieldsAreFilled(User user) {
        return user.getFirstName() != null &&
                user.getLastName() != null &&
                user.getPhone() != null &&
                user.getBirthdate() != null &&
                user.getAvatarUrl() != null &&
                user.getHeaderPhotoUrl() != null &&
                user.getHomeCity() != null &&
                user.getCurrentCity() != null;
    }

    public User findUserById(long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Not found user with ID: " + userId));
    }

    public List<UserShortDto> searchUsersByFullName(long currentUserId, String fullName) {
        String[] words = fullName.trim().toLowerCase().split("\\s+");
        List<User> users;

        if (words.length == 1) {
            users = userRepository.searchByFullNamePrefix(currentUserId, words[0]).orElse(new ArrayList<>());
        } else if (words.length == 2) {
            users = userRepository.searchByTwoWords(currentUserId, words[0], words[1]).orElse(new ArrayList<>());
        } else {
            users = userRepository.searchByFullNameContains(currentUserId, fullName).orElse(new ArrayList<>());
        }
        return users.stream()
                .map(user -> modelMapper.map(user, UserShortDto.class))
                .toList();
    }
}
