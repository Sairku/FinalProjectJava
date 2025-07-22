package com.facebook.service;

import com.facebook.dto.UserShortDto;
import com.facebook.enums.FriendStatus;
import com.facebook.exception.NotFoundException;
import com.facebook.model.Friend;
import com.facebook.model.User;
import com.facebook.repository.FriendRepository;
import com.facebook.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FriendServiceTest {
    @Mock
    private FriendRepository friendRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private FriendService friendService;

    private User user = new User();
    private User friend = new User();
    private UserShortDto friendShort = new UserShortDto();
    private User notYetFriend = new User();
    private Friend friendObject = new Friend();
    private Friend notYetFriendObject = new Friend();

    @BeforeEach
    void setUp() {
        user.setId(1L);
        friend.setId(2L);
        notYetFriend.setId(3L);
        user.setFirstName("Test Name 1");
        friend.setFirstName("Test Name 2");
        notYetFriend.setFirstName("Test Name 3");
        user.setLastName("Last Name 1");
        friend.setLastName("Last Name 2");
        notYetFriend.setLastName("Last Name 3");

        friendShort.setId(friend.getId());
        friendShort.setFirstName(friend.getFirstName());
        friendShort.setLastName(friend.getLastName());

        friendObject.setId(1L);
        friendObject.setUser(user);
        friendObject.setFriend(friend);
        friendObject.setStatus(FriendStatus.ACCEPTED);
        notYetFriendObject.setId(2L);
        notYetFriendObject.setUser(user);
        notYetFriendObject.setFriend(notYetFriend);
        notYetFriendObject.setStatus(FriendStatus.PENDING);
        notYetFriendObject.setAcceptedDate(null);
    }

    @Test
    void testGetAllFriendUsers() {
        when(friendRepository.findByStatusAndUserId(FriendStatus.ACCEPTED, 1L))
                .thenReturn(List.of(friendObject));
        when(modelMapper.map(friendObject.getFriend(), UserShortDto.class)).thenReturn(friendShort);

        List<UserShortDto> result = friendService.getAllFriendUsers(1L);

        assertEquals(1, result.size());
        assertEquals(friendShort, result.getFirst());
    }

    @Test
    void testGetAllUsersWhoSentRequest() {
        UserShortDto notYetFriendDto = new UserShortDto();
        notYetFriendDto.setId(notYetFriend.getId());
        notYetFriendDto.setFirstName(notYetFriend.getFirstName());

        when(friendRepository.findByStatusAndUserId(FriendStatus.PENDING, 1L))
                .thenReturn(List.of(notYetFriendObject));
        when(modelMapper.map(notYetFriend, UserShortDto.class)).thenReturn(notYetFriendDto);

        List<UserShortDto> result = friendService.getAllUsersWhoSentRequest(1L);

        assertEquals(1, result.size());
        assertEquals(notYetFriendDto, result.getFirst());
    }

    @Test
    void testGetAllUsersWhomSentRequest() {
        UserShortDto notYetFriendDto = new UserShortDto();
        notYetFriendDto.setId(notYetFriend.getId());
        notYetFriendDto.setFirstName(notYetFriend.getFirstName());

        when(friendRepository.findByStatusAndFriendId(FriendStatus.PENDING, 1L))
                .thenReturn(List.of(notYetFriendObject));
        when(modelMapper.map(notYetFriend, UserShortDto.class)).thenReturn(notYetFriendDto);

        List<UserShortDto> result = friendService.getAllUsersWhomSentRequest(1L);

        assertEquals(1, result.size());
        assertEquals(notYetFriendDto, result.getFirst());
    }

    @Test
    void testAddFriendRequest_OK() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findById(3L)).thenReturn(Optional.of(notYetFriend));
        when(friendRepository.findByUserIdAndFriendId(3L, 1L))
                .thenReturn(Optional.empty());
        when(friendRepository.findByUserIdAndFriendId(1L, 3L))
                .thenReturn(Optional.empty());
        when(friendRepository.save(any(Friend.class))).thenReturn(notYetFriendObject);

        friendService.addFriendRequest(1L, 3L);
        verify(friendRepository).save(any(Friend.class));
    }

    @Test
    void testAddFriendRequest_BadRequest() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findById(2L)).thenReturn(Optional.of(friend));
        when(friendRepository.findByUserIdAndFriendId(1L, 2L))
                .thenReturn(Optional.of(friendObject));

        assertThrows(IllegalArgumentException.class, () -> friendService.addFriendRequest(1L, 2L));
    }

    @Test
    void testAddFriendRequest_UserNotFound() {
        when(userRepository.findById(4L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                friendService.addFriendRequest(4L, 2L));
    }

    @Test
    void testAddFriendRequest_FriendNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findById(4L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                friendService.addFriendRequest(1L, 4L));
    }

    @Test
    void testResponseToFriendRequest_whenAccepted_OK() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(userRepository.findById(1L)).thenReturn(Optional.of(friend));
        when(friendRepository.findByUserIdAndFriendId(2L, 1L)).thenReturn(Optional.of(friendObject));

        friendService.responseToFriendRequest(2L, 1L, FriendStatus.ACCEPTED);

        verify(friendRepository, times(2)).save(any(Friend.class));
    }

    @Test
    void testResponseToFriendRequest_whenDeclined_OK() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(userRepository.findById(1L)).thenReturn(Optional.of(friend));
        when(friendRepository.findByUserIdAndFriendId(2L, 1L)).thenReturn(Optional.of(friendObject));

        friendService.responseToFriendRequest(2L, 1L, FriendStatus.DECLINED);

        verify(friendRepository).delete(any(Friend.class));
    }

    @Test
    void testResponseToFriendRequest_FriendRequestNotFound() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(userRepository.findById(1L)).thenReturn(Optional.of(friend));
        when(friendRepository.findByUserIdAndFriendId(2L, 1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                friendService.responseToFriendRequest(2L, 1L, FriendStatus.ACCEPTED));
    }

    @Test
    void deleteFriend_WhenValid_ShouldDeleteFriend() {
        when(friendRepository.findByUserIdAndFriendId(1L, 2L)).thenReturn(Optional.of(friendObject));

        friendService.deleteFriend(1L, 2L);

        verify(friendRepository, atLeastOnce()).delete(any(Friend.class));
    }

    @Test
    void testGetRecommendedFriends_NoFriends() {
        when(friendService.getAllFriendUsers(1L)).thenReturn(List.of());

        User otherUser1 = new User();
        otherUser1.setId(2L);
        otherUser1.setFirstName("Alice");
        otherUser1.setLastName("Smith");

        User otherUser2 = new User();
        otherUser2.setId(3L);
        otherUser2.setFirstName("Bob");
        otherUser2.setLastName("Jones");

        List<User> topUsers = List.of(otherUser1, otherUser2);

        UserShortDto dto1 = new UserShortDto(otherUser1.getId(), otherUser1.getFirstName(), otherUser1.getLastName(), null, null);
        UserShortDto dto2 = new UserShortDto(otherUser2.getId(), otherUser2.getFirstName(), otherUser2.getLastName(), null, null);

        when(userRepository.findTop40ByIdNotOrderByCreatedDateDesc(1L)).thenReturn(topUsers);
        when(friendRepository.findByUserIdAndFriendId(1L, 2L)).thenReturn(Optional.empty());
        when(friendRepository.findByUserIdAndFriendId(2L, 1L)).thenReturn(Optional.empty());
        when(friendRepository.findByUserIdAndFriendId(1L, 3L)).thenReturn(Optional.empty());
        when(friendRepository.findByUserIdAndFriendId(3L, 1L)).thenReturn(Optional.empty());
        when(modelMapper.map(otherUser1, UserShortDto.class)).thenReturn(dto1);
        when(modelMapper.map(otherUser2, UserShortDto.class)).thenReturn(dto2);

        List<UserShortDto> result = friendService.getRecommendedFriends(1L);

        assertEquals(2, result.size());
        assertTrue(result.contains(dto1));
        assertTrue(result.contains(dto2));
    }

    @Test
    void testGetRecommendedFriends_WithFriends() {
        User friendUser = new User();
        friendUser.setId(2L);
        friendUser.setFirstName("Friend");
        friendUser.setLastName("User");

        Friend friend = new Friend();
        friend.setFriend(friendUser);
        friend.setUser(new User()); // dummy owner user

        when(friendRepository.findByStatusAndUserId(FriendStatus.ACCEPTED, 1L))
                .thenReturn(List.of(friend));

        UserShortDto friendDto = new UserShortDto(2L, "Friend", "User", null, null);
        when(modelMapper.map(friendUser, UserShortDto.class)).thenReturn(friendDto);

        User foaf1 = new User();
        foaf1.setId(3L);
        foaf1.setFirstName("FriendOf");
        foaf1.setLastName("Friend1");

        User foaf2 = new User();
        foaf2.setId(4L);
        foaf2.setFirstName("FriendOf");
        foaf2.setLastName("Friend2");

        Friend friendOfFriend1 = new Friend();
        friendOfFriend1.setFriend(foaf1);
        friendOfFriend1.setUser(new User());

        Friend friendOfFriend2 = new Friend();
        friendOfFriend2.setFriend(foaf2);
        friendOfFriend2.setUser(new User());

        when(friendRepository.findByStatusAndUserId(FriendStatus.ACCEPTED, 2L))
                .thenReturn(List.of(friendOfFriend1, friendOfFriend2));

        UserShortDto foafDto1 = new UserShortDto(3L, "FriendOf", "Friend1", null, null);
        UserShortDto foafDto2 = new UserShortDto(4L, "FriendOf", "Friend2", null, null);

        when(friendRepository.findByUserIdAndFriendId(1L, 3L)).thenReturn(Optional.empty());
        when(friendRepository.findByUserIdAndFriendId(3L, 1L)).thenReturn(Optional.empty());
        when(friendRepository.findByUserIdAndFriendId(1L, 4L)).thenReturn(Optional.empty());
        when(friendRepository.findByUserIdAndFriendId(4L, 1L)).thenReturn(Optional.empty());

        when(modelMapper.map(foaf1, UserShortDto.class)).thenReturn(foafDto1);
        when(modelMapper.map(foaf2, UserShortDto.class)).thenReturn(foafDto2);

        List<UserShortDto> result = friendService.getRecommendedFriends(1L);

        assertEquals(2, result.size());
        assertTrue(result.contains(foafDto1));
        assertTrue(result.contains(foafDto2));
    }
}
