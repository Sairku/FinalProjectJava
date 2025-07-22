package com.facebook.service;

import com.facebook.dto.UserShortDto;
import com.facebook.enums.FriendStatus;
import com.facebook.exception.NotFoundException;
import com.facebook.model.Friend;
import com.facebook.model.User;
import com.facebook.repository.FriendRepository;
import com.facebook.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class FriendService {
    private final FriendRepository friendRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    public boolean isFriend(Long userId, Long friendId) {
        if (friendRepository.findByUserIdAndFriendId(userId, friendId).isPresent())
            return true;
        else
            return friendRepository.findByUserIdAndFriendId(friendId, userId).isPresent();
    }

    // Get all friends
    public List<UserShortDto> getAllFriendUsers(Long userId) {
        return friendRepository.findByStatusAndUserId(FriendStatus.ACCEPTED, userId)
                .stream()
                .map(friend -> modelMapper.map(friend.getFriend(), UserShortDto.class))
                .toList();
    }

    // Get all friend requests
    public List<UserShortDto> getAllUsersWhoSentRequest(Long userId) {
        return friendRepository.findByStatusAndUserId(FriendStatus.PENDING, userId)
                .stream()
                .map(friend -> modelMapper.map(friend.getFriend(), UserShortDto.class))
                .toList();
    }

    public List<UserShortDto> getAllUsersWhomSentRequest(Long userId) {
        return friendRepository.findByStatusAndFriendId(FriendStatus.PENDING, userId)
                .stream()
                .map(friend -> modelMapper.map(friend.getFriend(), UserShortDto.class))
                .toList();
    }

    private List<UserShortDto> filterRecommendedFriends(List<UserShortDto> recommendedFriends, Long userId) {
        return recommendedFriends.stream()
                .filter(friend -> !(friendRepository.findByUserIdAndFriendId(userId, friend.getId()).isPresent() ||
                        friendRepository.findByUserIdAndFriendId(friend.getId(), userId).isPresent())) // Exclude the current user
                .toList();
    }

    public List<UserShortDto> getRecommendedFriends(Long userId) {
        List<UserShortDto> currentUserFriends = getAllFriendUsers(userId);

        if (currentUserFriends.isEmpty()) {
            List<User> topUsers = userRepository.findTop40ByIdNotOrderByCreatedDateDesc(userId);
            List<UserShortDto> topUsersShort = topUsers.stream()
                    .map(user -> modelMapper.map(user, UserShortDto.class))
                    .toList();

            return filterRecommendedFriends(topUsersShort, userId);
        }

        List<UserShortDto> result = new ArrayList<>();

        for (UserShortDto friend : currentUserFriends) {
            List<UserShortDto> friendsOfFriend = new ArrayList<>(getAllFriendUsers(friend.getId()));

            List<UserShortDto> mutualFriends = friendsOfFriend.stream()
                    .filter(currentUserFriends::contains)
                    .toList();

            friendsOfFriend.removeAll(mutualFriends); // Remove mutual friends

            result.addAll(friendsOfFriend);

            if (result.size() >= 40) {
                break; // Limit to 40 recommendations
            }
        }

        if (result.size() < 40) {
            List<User> topUsers = userRepository.findTop40ByIdNotOrderByCreatedDateDesc(userId);
            List<UserShortDto> topUsersShort = topUsers.stream()
                    .map(user -> modelMapper.map(user, UserShortDto.class))
                    .toList();

            result.addAll(topUsersShort);
        }

        return filterRecommendedFriends(result, userId);
    }

    public void addFriendRequest(Long userId, Long friendId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        User friend = userRepository.findById(friendId)
                .orElseThrow(() -> new NotFoundException("Friend not found"));

        if (friendRepository.findByUserIdAndFriendId(userId, friendId).isPresent() ||
                friendRepository.findByUserIdAndFriendId(friendId, userId).isPresent()) {
            throw new IllegalArgumentException("Friend request already exists");
        }

        friendRepository.save(new Friend(FriendStatus.PENDING, friend, user, null));
    }

    public void responseToFriendRequest(Long userId, Long friendId, FriendStatus status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        User friend = userRepository.findById(friendId)
                .orElseThrow(() -> new NotFoundException("Friend not found"));

        Optional<Friend> friendRequest = friendRepository.findByUserIdAndFriendId(userId, friendId);

        if (friendRequest.isEmpty()) {
            throw new IllegalArgumentException("Friend request doesn't exist");
        }

        if (status.equals(FriendStatus.ACCEPTED)) {
            friendRequest.get().setStatus(FriendStatus.ACCEPTED);
            friendRepository.save(friendRequest.get());

            Optional<Friend> friends = friendRepository.findByUserIdAndFriendId(friendId, userId);
            if (friends.isEmpty()) {
                friendRepository.save(new Friend(FriendStatus.ACCEPTED, friend, user, null));
            } else {
                friends.get().setStatus(FriendStatus.ACCEPTED);
                friendRepository.save(friends.get());
            }
        } else if (status == FriendStatus.DECLINED) {
            friendRepository.delete(friendRequest.get());
        }
    }

    public void deleteFriend(Long userId, Long friendId) {
        Optional<Friend> firstExistingRequest = friendRepository.findByUserIdAndFriendId(userId, friendId);
        Optional<Friend> secondExistingRequest = friendRepository.findByUserIdAndFriendId(friendId, userId);

        firstExistingRequest.ifPresent(friendRepository::delete);
        secondExistingRequest.ifPresent(friendRepository::delete);
    }
}