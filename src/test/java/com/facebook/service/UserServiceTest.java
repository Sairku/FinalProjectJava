package com.facebook.service;

import com.facebook.dto.UserDetailsDto;
import com.facebook.dto.UserShortDto;
import com.facebook.dto.UserUpdateRequestDto;
import com.facebook.enums.FriendStatus;
import com.facebook.enums.Gender;
import com.facebook.enums.Provider;
import com.facebook.exception.NotFoundException;
import com.facebook.model.User;
import com.facebook.repository.FriendRepository;
import com.facebook.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
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

    @Mock
    private UserAchievementService userAchievementService;

    @InjectMocks
    private UserService userService;

    private User user;
    private UserUpdateRequestDto userUpdateRequestDto;
    private UserDetailsDto userCurrentDetailsDto;

    @BeforeEach
    void setUp() {
        user = new User();

        user.setId(1L);
        user.setEmail("test@gmail.com");
        user.setFirstName("First Name");
        user.setLastName("Last Name");
        user.setGender(Gender.FEMALE);
        user.setPhone("1234567890");
        user.setAvatarUrl("http://example.com/avatar.jpg");
        user.setHeaderPhotoUrl("http://example.com/header.jpg");
        user.setBirthdate(Date.valueOf("2000-10-20"));
        user.setHomeCity("Home City");
        user.setCurrentCity("Current City");
        user.setProvider(Provider.LOCAL);

        userCurrentDetailsDto = new UserDetailsDto();
        userCurrentDetailsDto.setId(1L);
        userCurrentDetailsDto.setEmail("test@gmail.com");
        userCurrentDetailsDto.setFirstName("First Name");
        userCurrentDetailsDto.setLastName("Last Name");
        userCurrentDetailsDto.setPhone("1234567890");
        userCurrentDetailsDto.setAvatarUrl("http://example.com/avatar.jpg");
        userCurrentDetailsDto.setHeaderPhotoUrl("http://example.com/header.jpg");
        userCurrentDetailsDto.setBirthdate(Date.valueOf("2000-10-20"));
        userCurrentDetailsDto.setHomeCity("Home City");
        userCurrentDetailsDto.setCurrentCity("Current City");

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
        when(modelMapper.map(user, UserDetailsDto.class)).thenReturn(userCurrentDetailsDto);

        UserDetailsDto updatedUser = userService.updateUser(1L, userUpdateRequestDto);

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
        when(modelMapper.map(user, UserDetailsDto.class)).thenReturn(userCurrentDetailsDto);
        when(friendRepository.findByStatusAndUserId(FriendStatus.ACCEPTED, 1L)).thenReturn(new ArrayList<>());
        when(friendRepository.findByStatusAndFriendId(FriendStatus.PENDING, 1L)).thenReturn(new ArrayList<>());

        UserDetailsDto foundUser = userService.getCurrentUserDetails(1L);

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

        UserDetailsDto foundUser = userService.getUserDetails(2L, 1L);

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

    @Test
    void testFindUserById_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User foundUser = userService.findUserById(1L);

        assertNotNull(foundUser);
        assertEquals(1L, foundUser.getId());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void testFindUserById_notFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.findUserById(1L));
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void testGetAllUsersExceptCurrent() {
        long currentUserId = 1L;
        int page = 0;
        int size = 2;

        Pageable pageable = PageRequest.of(page, size);

        // Mock users
        User user1 = new User();
        user1.setId(2L);
        user1.setFirstName("Alice");
        user1.setLastName("Smith");

        User user2 = new User();
        user2.setId(3L);
        user2.setFirstName("Bob");
        user2.setLastName("Johnson");

        List<User> users = List.of(user1, user2);
        Page<User> usersPage = new PageImpl<>(users, pageable, users.size());

        when(userRepository.findAllByIdNotOrderByCreatedDateDesc(currentUserId, pageable))
                .thenReturn(usersPage);

        UserShortDto dto1 = new UserShortDto(user1.getId(), user1.getFirstName(), user1.getLastName(), null);
        UserShortDto dto2 = new UserShortDto(user2.getId(), user2.getFirstName(), user2.getLastName(), null);

        when(modelMapper.map(user1, UserShortDto.class)).thenReturn(dto1);
        when(modelMapper.map(user2, UserShortDto.class)).thenReturn(dto2);

        Page<UserShortDto> result = userService.getAllUsersExceptCurrent(currentUserId, page, size);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());

        assertEquals(dto1, result.getContent().get(0));
        assertEquals(dto2, result.getContent().get(1));

        verify(userRepository, times(1)).findAllByIdNotOrderByCreatedDateDesc(currentUserId, pageable);
        verify(modelMapper, times(1)).map(user1, UserShortDto.class);
        verify(modelMapper, times(1)).map(user2, UserShortDto.class);
    }

    @Test
    void testFindAllUsersByFirstNameAndLastName_returnsMappedDtoList() {
        String firstName = "First Name";
        String lastName = "Last Name";

        List<User> users = List.of(user);
        UserShortDto userShortDto = new UserShortDto();
        userShortDto.setId(user.getId());
        userShortDto.setFirstName(user.getFirstName());
        userShortDto.setLastName(user.getLastName());

        Mockito.when(userRepository.findAllByFirstNameAndLastName(firstName, lastName))
                .thenReturn(Optional.of(users));

        Mockito.when(modelMapper.map(user, UserShortDto.class))
                .thenReturn(userShortDto);

        List<UserShortDto> result = userService.findAllUsersByFirstNameAndLastName(firstName, lastName);

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(userShortDto, result.get(0));
        Mockito.verify(userRepository).findAllByFirstNameAndLastName(firstName, lastName);
        Mockito.verify(modelMapper).map(user, UserShortDto.class);
    }

    @Test
    void testFindAllUsersByFirstNameAndLastName_returnsEmptyList_whenNoUsersFound() {
        String firstName = "Nonexistent";
        String lastName = "User";

        Mockito.when(userRepository.findAllByFirstNameAndLastName(firstName, lastName))
                .thenReturn(Optional.empty());

        List<UserShortDto> result = userService.findAllUsersByFirstNameAndLastName(firstName, lastName);

        Assertions.assertTrue(result.isEmpty());
        Mockito.verify(userRepository).findAllByFirstNameAndLastName(firstName, lastName);
        Mockito.verifyNoInteractions(modelMapper);
    }
}
