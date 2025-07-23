package com.facebook.service;

import com.facebook.dto.GoogleRequestDto;
import com.facebook.dto.LoginResponseDto;
import com.facebook.dto.RegisterRequestDto;
import com.facebook.enums.Gender;
import com.facebook.enums.Provider;
import com.facebook.exception.NotFoundException;
import com.facebook.model.User;
import com.facebook.model.VerificationToken;
import com.facebook.repository.UserRepository;
import com.facebook.repository.VerificationTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private VerificationTokenRepository verificationTokenRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private RegisterRequestDto registerRequestDto;
    private GoogleRequestDto googleRequestDto;
    private LoginResponseDto loginResponseDto;
    private User user;

    private final String email = "test@gmail.com";
    private final String password = "test1234";

    @BeforeEach
    void setUp() {
        registerRequestDto = new RegisterRequestDto();
        registerRequestDto.setEmail(email);
        registerRequestDto.setPassword(password);

        googleRequestDto = new GoogleRequestDto();
        googleRequestDto.setEmail("google@gmail.com");

        user = new User();
        user.setId(1L);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setProvider(Provider.LOCAL);

        loginResponseDto = new LoginResponseDto();
        loginResponseDto.setUserId(1L);
        loginResponseDto.setEmail(email);
        loginResponseDto.setToken("access_token");
    }

    @Test
    void testRegister() {
        when(modelMapper.map(registerRequestDto, User.class)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(modelMapper.map(user, LoginResponseDto.class)).thenReturn(loginResponseDto);

        LoginResponseDto result = authService.register(registerRequestDto);

        assertNotNull(result);
        assertEquals(user.getId(), result.getUserId());
        assertEquals(user.getEmail(), result.getEmail());
    }

    @Test
    void testRegisterGoogleUser() {
        googleRequestDto.setPassword("googlePassword");

        user.setEmail(googleRequestDto.getEmail());
        user.setPassword(passwordEncoder.encode("googlePassword"));
        user.setGender(Gender.CUSTOM);
        user.setProvider(Provider.GOOGLE);

        when(userRepository.save(any(User.class))).thenReturn(user);

        LoginResponseDto result = authService.registerGoogleUser(googleRequestDto);

        assertNotNull(result);
        assertEquals(user.getId(), result.getUserId());
        assertEquals(user.getEmail(), result.getEmail());
    }

    @Test
    void testUserByEmailExistsTrue() {
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        assertTrue(authService.userByEmailExists(email));
    }

    @Test
    void testUserByEmailExistsFalse() {
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertFalse(authService.userByEmailExists(email));
    }

    @Test
    void testIsValidPasswordTrue() {
        when(passwordEncoder.matches(password, password)).thenReturn(true);

        assertTrue(authService.isValidPassword(password, password));
    }

    @Test
    void testIsValidPasswordFalse() {
        assertFalse(authService.isValidPassword("raw", "encoded"));
    }

    @Test
    void testResetPasswordSuccess() {
        VerificationToken token = new VerificationToken();

        token.setToken("valid-token");
        token.setExpiredAt(LocalDateTime.now().plusMinutes(10));
        token.setUser(user);

        when(verificationTokenRepository.findByToken("valid-token")).thenReturn(Optional.of(token));

        authService.resetPassword("valid-token", "newPassword", "newPassword");

        verify(userRepository, times(1)).save(user);
        verify(verificationTokenRepository, times(1)).delete(token);
    }

    @Test
    void testResetPasswordPasswordsNotMatch() {
        assertThrows(IllegalArgumentException.class, () ->
                authService.resetPassword("token", "pass1", "pass2"));
    }

    @Test
    void testResetPasswordTokenNotFound() {
        when(verificationTokenRepository.findByToken("token")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                authService.resetPassword("token", "pass", "pass"));
    }

    @Test
    void testResetPasswordTokenExpired() {
        VerificationToken token = new VerificationToken();
        token.setExpiredAt(LocalDateTime.now().minusMinutes(5));
        token.setUser(user);

        when(verificationTokenRepository.findByToken("token")).thenReturn(Optional.of(token));

        assertThrows(RuntimeException.class, () ->
                authService.resetPassword("token", "pass", "pass"));
    }
}
