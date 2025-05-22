package com.facebook.service;

import com.facebook.dto.UserDetailsDto;
import com.facebook.dto.UserCurrentDetailsDto;
import com.facebook.dto.UserUpdateRequestDto;
import com.facebook.enums.FriendStatus;
import com.facebook.enums.Gender;
import com.facebook.enums.Provider;
import com.facebook.exception.NotFoundException;
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

import java.sql.Date;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private FriendRepository friendRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private UserService userService;

    private User user;
    private UserUpdateRequestDto userUpdateRequestDto;
    private UserCurrentDetailsDto userCurrentDetailsDto;

    @BeforeEach
    void setUp() {
        user = new User();

        user.setId(1L);
        user.setEmail("test@gmail.com");
        user.setFirstName("FirstName");
        user.setLastName("LastName");
        user.setGender(Gender.FEMALE);
        user.setPhone("1234567890");
        user.setAvatarUrl("http://example.com/avatar.jpg");
        user.setHeaderPhotoUrl("http://example.com/header.jpg");
        user.setBirthdate(Date.valueOf("2000-10-20"));
        user.setHomeCity("HomeCity");
        user.setCurrentCity("CurrentCity");
        user.setProvider(Provider.LOCAL);

        userCurrentDetailsDto = new UserCurrentDetailsDto();
        userCurrentDetailsDto.setId(1L);
        userCurrentDetailsDto.setEmail("test@gmail.com");
        userCurrentDetailsDto.setFirstName("FirstName");
        userCurrentDetailsDto.setLastName("LastName");
        userCurrentDetailsDto.setPhone("1234567890");
        userCurrentDetailsDto.setAvatarUrl("http://example.com/avatar.jpg");
        userCurrentDetailsDto.setHeaderPhotoUrl("http://example.com/header.jpg");
        userCurrentDetailsDto.setBirthdate(Date.valueOf("2000-10-20"));
        userCurrentDetailsDto.setHomeCity("HomeCity");
        userCurrentDetailsDto.setCurrentCity("CurrentCity");

        userUpdateRequestDto = new UserUpdateRequestDto(
                "New First Name",
                "New Last Name",
                "380987654321",
                Date.valueOf("2000-10-22"),
                "http://example.com/new_avatar.jpg",
                "http://example.com/new_header.jpg",
                "New Home City",
                "New Current City"
        );
    }

    @Test
    void testUpdateUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        user.setFirstName(userUpdateRequestDto.getFirstName());
        user.setLastName(userUpdateRequestDto.getLastName());
        user.setPhone(userUpdateRequestDto.getPhone());
        user.setBirthdate(userUpdateRequestDto.getBirthdate());
        user.setAvatarUrl(userUpdateRequestDto.getAvatarUrl());
        user.setHeaderPhotoUrl(userUpdateRequestDto.getHeaderPhotoUrl());
        user.setHomeCity(userUpdateRequestDto.getHomeCity());
        user.setCurrentCity(userUpdateRequestDto.getCurrentCity());
        userCurrentDetailsDto.setFirstName(userUpdateRequestDto.getFirstName());
        userCurrentDetailsDto.setLastName(userUpdateRequestDto.getLastName());
        userCurrentDetailsDto.setPhone(userUpdateRequestDto.getPhone());
        userCurrentDetailsDto.setBirthdate(userUpdateRequestDto.getBirthdate());
        userCurrentDetailsDto.setAvatarUrl(userUpdateRequestDto.getAvatarUrl());
        userCurrentDetailsDto.setHeaderPhotoUrl(userUpdateRequestDto.getHeaderPhotoUrl());
        userCurrentDetailsDto.setHomeCity(userUpdateRequestDto.getHomeCity());
        userCurrentDetailsDto.setCurrentCity(userUpdateRequestDto.getCurrentCity());

        when(userRepository.save(user)).thenReturn(user);
        when(modelMapper.map(user, UserCurrentDetailsDto.class)).thenReturn(userCurrentDetailsDto);

        UserCurrentDetailsDto updatedUser = userService.updateUser(1L, userUpdateRequestDto);

        verify(userRepository, times(1)).save(user);

        assertNotNull(updatedUser);
        assertEquals(userUpdateRequestDto.getFirstName(), updatedUser.getFirstName());
        assertEquals(userUpdateRequestDto.getLastName(), updatedUser.getLastName());
        assertEquals(userUpdateRequestDto.getPhone(), updatedUser.getPhone());
        assertEquals(userUpdateRequestDto.getBirthdate(), updatedUser.getBirthdate());
        assertEquals(userUpdateRequestDto.getAvatarUrl(), updatedUser.getAvatarUrl());
        assertEquals(userUpdateRequestDto.getHeaderPhotoUrl(), updatedUser.getHeaderPhotoUrl());
        assertEquals(userUpdateRequestDto.getHomeCity(), updatedUser.getHomeCity());
        assertEquals(userUpdateRequestDto.getCurrentCity(), updatedUser.getCurrentCity());
    }

    @Test
    void testUpdateUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.updateUser(1L, userUpdateRequestDto));
    }

    @Test
    void testGetCurrentUserDetails() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(modelMapper.map(user, UserCurrentDetailsDto.class)).thenReturn(userCurrentDetailsDto);
        when(friendRepository.findByStatusAndUserId(FriendStatus.ACCEPTED, 1L)).thenReturn(new ArrayList<>());
        when(friendRepository.findByStatusAndFriendId(FriendStatus.PENDING, 1L)).thenReturn(new ArrayList<>());

        UserCurrentDetailsDto foundUser = userService.getCurrentUserDetails(1L);

        assertNotNull(foundUser);
        assertEquals(user.getId(), foundUser.getId());
        assertEquals(user.getEmail(), foundUser.getEmail());
        assertEquals(user.getFirstName(), foundUser.getFirstName());
        assertEquals(user.getLastName(), foundUser.getLastName());
        assertEquals(user.getPhone(), foundUser.getPhone());
        assertEquals(user.getAvatarUrl(), foundUser.getAvatarUrl());
        assertEquals(user.getHeaderPhotoUrl(), foundUser.getHeaderPhotoUrl());
        assertEquals(user.getBirthdate(), foundUser.getBirthdate());
        assertEquals(user.getHomeCity(), foundUser.getHomeCity());
        assertEquals(user.getCurrentCity(), foundUser.getCurrentCity());
    }

    @Test
    void testGetCurrentUserDetailsNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.getCurrentUserDetails(1L));
    }

    @Test
    void testGetUserDetails() {
        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setEmail("other@gmail.com");
        otherUser.setFirstName("First Name Other");
        otherUser.setLastName("Last Name Other");
        otherUser.setPhone("1434767890");
        otherUser.setAvatarUrl("http://example.com/avatar_other.jpg");
        otherUser.setHeaderPhotoUrl("http://example.com/header_other.jpg");
        otherUser.setBirthdate(Date.valueOf("1993-10-20"));
        otherUser.setHomeCity("Home City Other");
        otherUser.setCurrentCity("Current City Other");
        otherUser.setProvider(Provider.LOCAL);

        UserDetailsDto userDetailsDto = new UserDetailsDto();
        userDetailsDto.setId(2L);
        userDetailsDto.setEmail("other@gmail.com");
        userDetailsDto.setFirstName("First Name Other");
        userDetailsDto.setLastName("Last Name Other");
        userDetailsDto.setPhone("1434767890");
        userDetailsDto.setAvatarUrl("http://example.com/avatar_other.jpg");
        userDetailsDto.setHeaderPhotoUrl("http://example.com/header_other.jpg");
        userDetailsDto.setBirthdate(Date.valueOf("1993-10-20"));
        userDetailsDto.setHomeCity("Home City Other");
        userDetailsDto.setCurrentCity("Current City Other");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findById(2L)).thenReturn(Optional.of(otherUser));
        when(friendRepository.findByStatusAndUserId(FriendStatus.ACCEPTED, 1L)).thenReturn(new ArrayList<>());
        when(friendRepository.findByStatusAndUserId(FriendStatus.ACCEPTED, 2L)).thenReturn(new ArrayList<>());
        when(friendRepository.findByStatusAndFriendId(FriendStatus.PENDING, 2L)).thenReturn(new ArrayList<>());

        when(modelMapper.map(otherUser, UserDetailsDto.class)).thenReturn(userDetailsDto);

        UserCurrentDetailsDto foundUser = userService.getUserDetails(2L, 1L);

        assertNotNull(foundUser);
        assertEquals(otherUser.getId(), foundUser.getId());
        assertEquals(otherUser.getEmail(), foundUser.getEmail());
        assertEquals(otherUser.getFirstName(), foundUser.getFirstName());
        assertEquals(otherUser.getLastName(), foundUser.getLastName());
        assertEquals(otherUser.getPhone(), foundUser.getPhone());
        assertEquals(otherUser.getAvatarUrl(), foundUser.getAvatarUrl());
        assertEquals(otherUser.getHeaderPhotoUrl(), foundUser.getHeaderPhotoUrl());
        assertEquals(otherUser.getBirthdate(), foundUser.getBirthdate());
        assertEquals(otherUser.getHomeCity(), foundUser.getHomeCity());
        assertEquals(otherUser.getCurrentCity(), foundUser.getCurrentCity());
    }

    @Test
    void testGetUserDetailsNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.getUserDetails(1L, 1L));
    }
}
