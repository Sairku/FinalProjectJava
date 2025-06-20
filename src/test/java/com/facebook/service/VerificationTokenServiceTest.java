package com.facebook.service;

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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class VerificationTokenServiceTest {
    @Mock
    private VerificationTokenRepository verificationTokenRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private VerificationTokenService verificationTokenService;

    private User user;

    private final String email = "test@gmail.com";

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail(email);
    }

    @Test
    void testCreatePasswordResetTokenSuccess() {
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        String token = verificationTokenService.createPasswordResetToken(email);

        assertNotNull(token);
        verify(verificationTokenRepository).save(any(VerificationToken.class));
    }

    @Test
    void testCreatePasswordResetTokenUserNotFound() {
        when(userRepository.findByEmail("nonexisting@gmail.com")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> {
            verificationTokenService.createPasswordResetToken("nonexisting@gmail.com");
        });
    }
}
