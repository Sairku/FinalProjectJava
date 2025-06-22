package com.facebook.service;

import com.facebook.enums.FriendStatus;
import com.facebook.exception.NotFoundException;
import com.facebook.model.Friend;
import com.facebook.model.User;
import com.facebook.repository.FriendRepository;
import com.facebook.repository.UserRepository;
import com.facebook.util.ResponseHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FriendServiceTest {
    @Mock
    private FriendRepository friendRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private FriendService friendService;

    private User user = new User();
    private User friend = new User();
    private User notYetFriend = new User();
    private Friend friendObject = new Friend();
    private Friend notYetFriendObject = new Friend();

    @BeforeEach
    void setUp(){
        user.setId(1L);
        friend.setId(2L);
        notYetFriend.setId(3L);
        user.setFirstName("Test Name 1");
        friend.setFirstName("Test Name 2");
        notYetFriend.setFirstName("Test Name 3");
        user.setLastName("Last Name 1");
        friend.setLastName("Last Name 2");
        notYetFriend.setLastName("Last Name 3");

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
    void testGetFriendById() {
        when(friendRepository.findByUserIdAndFriendId(1L, 2L))
                .thenReturn(Optional.of(friendObject));

        Friend result = friendService.getFriendById(1L, 2L);

        assertEquals(friendObject, result);
    }

    @Test
    void testGetUserWhoIsFriendById() {
        when(friendRepository.findByUserIdAndFriendId(1L, 2L))
                .thenReturn(Optional.of(friendObject));

        User result = friendService.getUserWhoIsFriendById(1L, 2L);

        assertEquals(friend, result);
    }

    @Test
    void testGetAllFriendRequests() {
        when(friendRepository.findByUserId(1L))
                .thenReturn(List.of(friendObject));

        List<Friend> result = friendService.getAllFriendRequests(1L);

        assertEquals(1, result.size());
        assertEquals(friendObject, result.getFirst());
    }

    @Test
    void testGetAllFriends() {
        when(friendRepository.findByStatusAndUserId(FriendStatus.ACCEPTED, 1L))
                .thenReturn(List.of(friendObject));

        List<Friend> result = friendService.getAllFriends(1L);

        assertEquals(1, result.size());
        assertEquals(friendObject, result.getFirst());
    }

    @Test
    void testGetAllFriendUsers() {
        when(friendRepository.findByStatusAndUserId(FriendStatus.ACCEPTED, 1L))
                .thenReturn(List.of(friendObject));

        List<User> result = friendService.getAllFriendUsers(1L);

        assertEquals(1, result.size());
        assertEquals(friend, result.getFirst());
    }

    @Test
    void testGetAllFriendsWhoHaveNotYetAccepted() {
        when(friendRepository.findByStatusAndUserId(FriendStatus.PENDING, 1L))
                .thenReturn(List.of(notYetFriendObject));

        List<Friend> result = friendService.getAllFriendsWhoHaveNotYetAccepted(1L);

        assertEquals(1, result.size());
        assertEquals(notYetFriendObject, result.getFirst());
    }

    @Test
    void testGetAllFriendsWhoSentRequest() {
        when(friendRepository.findByStatusAndFriendId(FriendStatus.PENDING, 1L))
                .thenReturn(List.of(notYetFriendObject));

        List<Friend> result = friendService.getAllFriendsWhoSentRequest(1L);

        assertEquals(1, result.size());
        assertEquals(notYetFriendObject, result.getFirst());
    }

    @Test
    void testGetAllUsersWhoAreFriends() {
        when(friendRepository.findByStatusAndUserId(FriendStatus.ACCEPTED, 1L))
                .thenReturn(List.of(friendObject));

        List<User> result = friendService.getAllUsersWhoAreFriends(1L);
        assertEquals(1, result.size());
        assertEquals(friend, result.getFirst());
    }

    @Test
    void testGetAllUsersWhoHaveNotYetAccepted() {
        when(friendRepository.findByStatusAndUserId(FriendStatus.PENDING, 1L))
                .thenReturn(List.of(notYetFriendObject));

        List<User> result = friendService.getAllUsersWhoHaveNotYetAccepted(1L);

        assertEquals(1, result.size());
        assertEquals(notYetFriend, result.getFirst());
    }

    @Test
    void testGetAllUsersWhoSentRequest() {
        when(friendRepository.findByStatusAndFriendId(FriendStatus.PENDING, 1L))
                .thenReturn(List.of(notYetFriendObject));

        List<User> result = friendService.getAllUsersWhoSentRequest(1L);

        assertEquals(1, result.size());
        assertEquals(notYetFriend, result.getFirst());
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

        ResponseEntity<?> result = friendService.addFriendRequest(1L, 3L);

        assertEquals(HttpStatus.OK, result.getStatusCode());
    }

    @Test
    void testAddFriendRequest_BadRequest() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findById(2L)).thenReturn(Optional.of(friend));
        when(friendRepository.findByUserIdAndFriendId(1L, 2L))
                .thenReturn(Optional.of(friendObject));

        ResponseEntity<?> result = friendService.addFriendRequest(1L, 2L);

        assertEquals(ResponseHandler.generateResponse(HttpStatus.BAD_REQUEST, true, "Friend request already exists", null), result);
    }

    @Test
    void testAddFriendRequest_UserNotFound(){
        when(userRepository.findById(4L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                friendService.addFriendRequest(4L, 2L));
    }

    @Test
    void testAddFriendRequest_FriendNotFound(){
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findById(4L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                friendService.addFriendRequest(1L, 4L));
    }

    @Test
    void testResponseToFriendRequest_whenAccepted_OK() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findById(2L)).thenReturn(Optional.of(friend));
        when(friendRepository.findByUserIdAndFriendId(2L, 1L)).thenReturn(Optional.of(friendObject));

        ResponseEntity<Object> response = friendService.responseToFriendRequest(1L, 2L, FriendStatus.ACCEPTED);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(FriendStatus.ACCEPTED, friendObject.getStatus());
    }

    @Test
    void testResponseToFriendRequest_whenDeclined_OK() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findById(2L)).thenReturn(Optional.of(friend));
        when(friendRepository.findByUserIdAndFriendId(2L, 1L)).thenReturn(Optional.of(friendObject));

        ResponseEntity<Object> response = friendService.responseToFriendRequest(1L, 2L, FriendStatus.DECLINED);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testResponseToFriendRequest_BadRequest(){
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findById(2L)).thenReturn(Optional.of(friend));
        when(friendRepository.findByUserIdAndFriendId(2L, 1L)).thenReturn(Optional.of(friendObject));

        ResponseEntity<Object> response = friendService.responseToFriendRequest(1L, 2L, FriendStatus.PENDING);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testResponseToFriendRequest_FriendRequestNotFound(){
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findById(2L)).thenReturn(Optional.of(friend));
        when(friendRepository.findByUserIdAndFriendId(2L, 1L)).thenReturn(Optional.empty());

        ResponseEntity<Object> response = friendService.responseToFriendRequest(1L, 2L, FriendStatus.ACCEPTED);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void deleteFriend_WhenValid_ShouldDeleteFriend() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findById(2L)).thenReturn(Optional.of(friend));
        when(friendRepository.findByUserIdAndFriendId(1L, 2L)).thenReturn(Optional.of(friendObject));

        ResponseEntity<Object> response = friendService.deleteFriend(1L, 2L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void deleteFriend_WhenRequestNotFound_ShouldReturnBadRequest() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findById(2L)).thenReturn(Optional.of(friend));
        when(friendRepository.findByUserIdAndFriendId(1L, 2L)).thenReturn(Optional.empty());

        ResponseEntity<Object> response = friendService.deleteFriend(1L, 2L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void getRecommendedFriends_ShouldReturnRecommendedFriends() {
        // Setup test data
        User user3 = new User();
        user3.setId(3L);
        user3.setFirstName("User Three");

        User user4 = new User();
        user4.setId(4L);
        user4.setFirstName("User Four");

        Friend friend1 = new Friend();
        friend1.setUser(user);
        friend1.setFriend(friend);
        friend1.setStatus(FriendStatus.ACCEPTED);

        Friend friend2 = new Friend();
        friend2.setUser(friend);
        friend2.setFriend(user3);
        friend2.setStatus(FriendStatus.ACCEPTED);

        Friend friend3 = new Friend();
        friend3.setUser(friend);
        friend3.setFriend(user4);
        friend3.setStatus(FriendStatus.ACCEPTED);

        List<Friend> user1Friends = List.of(friend1);
        List<Friend> user2Friends = List.of(friend2, friend3);

        when(friendRepository.findByStatusAndUserId(FriendStatus.ACCEPTED, 1L))
                .thenReturn(user1Friends);
        when(friendRepository.findByStatusAndUserId(FriendStatus.ACCEPTED, 2L))
                .thenReturn(user2Friends);

        List<User> recommendedFriends = friendService.getRecommendedFriends(1L);

        assertEquals(2, recommendedFriends.size());
        assertTrue(recommendedFriends.contains(user3));
        assertTrue(recommendedFriends.contains(user4));
    }

}
