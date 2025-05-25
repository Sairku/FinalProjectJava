package com.facebook.controller;

import com.facebook.dto.*;
import com.facebook.enums.Provider;
import com.facebook.service.AuthService;
import com.facebook.service.CustomUserDetailsService;
import com.facebook.util.GoogleTokenVerifier;
import com.facebook.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private GoogleTokenVerifier googleTokenVerifier;

    @InjectMocks
    private AuthController authController;

    private ObjectMapper objectMapper;

    private UserAuthDto mockUser;
    private LoginResponseDto mockLoginResponse;

    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();

        mockUser = new UserAuthDto(
                1L,
                "test@example.com",
                "hashedPassword",
                Provider.LOCAL,
                List.of()
        );

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

        Mockito.when(authService.userByEmailExists("test@example.com")).thenReturn(false);
        Mockito.when(authService.register(any(RegisterRequestDto.class))).thenReturn(mockLoginResponse);
        Mockito.when(jwtUtil.generateToken("test@example.com")).thenReturn("jwt-token");
        Mockito.when(authenticationManager.authenticate(any())).thenReturn(Mockito.mock(Authentication.class));

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

        Mockito.when(authService.userByEmailExists("existing@example.com")).thenReturn(true);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Email already exists"));
    }
}
