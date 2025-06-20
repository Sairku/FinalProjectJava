package com.facebook.controller;

import com.facebook.dto.*;
import com.facebook.enums.Provider;
import com.facebook.service.AuthService;
//import com.facebook.service.EmailService;
//import com.facebook.service.VerificationTokenService;
import com.facebook.service.EmailService;
import com.facebook.service.VerificationTokenService;
import com.facebook.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {
    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @Mock
    private EmailService emailService;

    @Mock
    private VerificationTokenService verificationTokenService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthController authController;

    private ObjectMapper objectMapper;

    private LoginResponseDto mockLoginResponse;

    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders
                .standaloneSetup(authController)
                .build();

        mockLoginResponse = new LoginResponseDto();
        mockLoginResponse.setUserId(1L);
        mockLoginResponse.setEmail("test@example.com");
        mockLoginResponse.setToken("jwt-token");
    }

    @Test
    void register_shouldReturn201_whenNewUser() throws Exception {
        RegisterRequestDto request = new RegisterRequestDto(
                "test@example.com",
                "pass1232323",
                "John",
                "Doe",
                new GregorianCalendar(2000, Calendar.JANUARY, 1).getTime(), // точно в минулому
                "male" // перевір, які значення приймає gender
        );

        when(authService.userByEmailExists("test@example.com")).thenReturn(false);
        when(authService.register(any(RegisterRequestDto.class))).thenReturn(mockLoginResponse);
        when(jwtUtil.generateToken("test@example.com")).thenReturn("jwt-token");
        when(authenticationManager.authenticate(any())).thenReturn(Mockito.mock(Authentication.class));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("User registered successfully"))
                .andExpect(jsonPath("$.data.email").value("test@example.com"));
    }


    @Test
    void register_shouldReturn400_whenEmailExists() throws Exception {
        RegisterRequestDto request = new RegisterRequestDto(
                "existing@example.com",
                "pass1232323",
                "John",
                "Doe",
                new GregorianCalendar(2000, Calendar.JANUARY, 1).getTime(),
                "male"
        );

        when(authService.userByEmailExists("existing@example.com")).thenReturn(true);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Email already exists"));
    }

//    @Test
//    void testRequestPasswordReset() throws Exception {
//        String email = "test@example.com";
//        String token = "mocked-token";
//        PasswordResetRequestDto passwordResetRequest = new PasswordResetRequestDto(email);
//
//        when(verificationTokenService.createPasswordResetToken(email)).thenReturn(token);
//
//        mockMvc.perform(post("/api/auth/request-reset-password")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(passwordResetRequest)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.error").value(false))
//                .andExpect(jsonPath("$.message").value("Password reset link sent to your email test@example.com"));
//
//        verify(emailService, times(1)).sendEmail(eq(email), anyString(), contains(token));
//    }

//    @Test
//    void testResetPasswordSuccess() throws Exception {
//        PasswordResetDto passwordReset = new PasswordResetDto();
//        passwordReset.setToken("token123");
//        passwordReset.setNewPassword("newPass123");
//        passwordReset.setConfirmPassword("newPass123");
//
//        mockMvc.perform(post("/api/auth/reset-password")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(passwordReset)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.error").value(false))
//                .andExpect(jsonPath("$.message").value("Password reset successfully"));
//
//        verify(authService, times(1)).resetPassword("token123", "newPass123", "newPass123");
//    }

//    @Test
//    void testResetPasswordMismatch() throws Exception {
//        PasswordResetDto passwordReset = new PasswordResetDto();
//        passwordReset.setToken("token123");
//        passwordReset.setNewPassword("newPass123");
//        passwordReset.setConfirmPassword("wrongPass");
//
//        doThrow(new IllegalArgumentException("Passwords do not match"))
//                .when(authService)
//                .resetPassword("token123", "newPass123", "wrongPass");
//
//        mockMvc.perform(post("/api/auth/reset-password")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(passwordReset)))
//                .andExpect(status().isBadRequest());
//
//        verify(authService).resetPassword("token123", "newPass123", "wrongPass");
//    }
}
