package com.facebook.service;

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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FriendServiceTest {

    @Mock
    private FriendRepository friendRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private FriendService friendService;

    private User user;
    private User friend;

    @BeforeEach
    void setup() {
        user = new User();
        user.setId(1L);

        friend = new User();
        friend.setId(2L);
    }

    @Test
    void addFriendRequest_shouldReturnOk_whenRequestIsNew() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findById(2L)).thenReturn(Optional.of(friend));
        when(friendRepository.findByUserIdAndFriendId(1L, 2L)).thenReturn(Optional.empty());

        ResponseEntity<Object> response = friendService.addFriendRequest(1L, 2L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(friendRepository).save(any(Friend.class));
    }

    @Test
    void addFriendRequest_shouldReturnBadRequest_whenRequestExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findById(2L)).thenReturn(Optional.of(friend));
        when(friendRepository.findByUserIdAndFriendId(1L, 2L)).thenReturn(Optional.of(new Friend()));

        ResponseEntity<Object> response = friendService.addFriendRequest(1L, 2L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(friendRepository, never()).save(any());
    }

    @Test
    void addFriendRequest_shouldThrow_whenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> friendService.addFriendRequest(1L, 2L));
    }

    @Test
    void responseToFriendRequest_shouldAcceptRequest() {
        Friend request = new Friend(FriendStatus.PENDING, user, friend, null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findById(2L)).thenReturn(Optional.of(friend));
        when(friendRepository.findByUserIdAndFriendId(1L, 2L)).thenReturn(Optional.of(request));

        ResponseEntity<Object> response = friendService.responseToFriendRequest(1L, 2L, FriendStatus.ACCEPTED);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(friendRepository).save(request);
        verify(friendRepository).save(argThat(f -> f.getUser().equals(friend) && f.getFriend().equals(user)));
    }

    @Test
    void responseToFriendRequest_shouldRejectRequest() {
        Friend request = new Friend(FriendStatus.PENDING, user, friend, null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findById(2L)).thenReturn(Optional.of(friend));
        when(friendRepository.findByUserIdAndFriendId(1L, 2L)).thenReturn(Optional.of(request));

        ResponseEntity<Object> response = friendService.responseToFriendRequest(1L, 2L, FriendStatus.DECLINED);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(friendRepository).delete(request);
    }

    @Test
    void responseToFriendRequest_shouldReturnBadRequest_whenNotExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findById(2L)).thenReturn(Optional.of(friend));
        when(friendRepository.findByUserIdAndFriendId(1L, 2L)).thenReturn(Optional.empty());

        ResponseEntity<Object> response = friendService.responseToFriendRequest(1L, 2L, FriendStatus.ACCEPTED);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void deleteFriend_shouldDelete_whenExists() {
        Friend request = new Friend(FriendStatus.ACCEPTED, user, friend, null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findById(2L)).thenReturn(Optional.of(friend));
        when(friendRepository.findByUserIdAndFriendId(1L, 2L)).thenReturn(Optional.of(request));

        ResponseEntity<Object> response = friendService.deleteFriend(1L, 2L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(friendRepository).delete(request);
    }

    @Test
    void deleteFriend_shouldReturnBadRequest_whenNotExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findById(2L)).thenReturn(Optional.of(friend));
        when(friendRepository.findByUserIdAndFriendId(1L, 2L)).thenReturn(Optional.empty());

        ResponseEntity<Object> response = friendService.deleteFriend(1L, 2L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
