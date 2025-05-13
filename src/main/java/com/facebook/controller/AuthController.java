package com.facebook.controller;

import com.facebook.dto.*;
import com.facebook.service.AuthService;
import com.facebook.service.CustomUserDetailsService;
import com.facebook.util.GoogleTokenVerifier;
import com.facebook.util.JwtUtil;
import com.facebook.util.ResponseHandler;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final CustomUserDetailsService userDetailsService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final GoogleTokenVerifier googleTokenVerifier;

    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(@RequestBody @Validated GoogleRequest googleRequest) {
        LoginResponse loginResponse;

        try {
            GoogleIdToken.Payload payload = googleTokenVerifier.verify(googleRequest.getIdToken());
            String email = payload.getEmail();

            if (email == null) {
                log.info("Invalid Google token");

                return ResponseHandler.generateResponse(
                        HttpStatus.UNAUTHORIZED,
                        true,
                        "Invalid Google token",
                        null
                );
            }

            if (!authService.userByEmailExists(email)) {
                String firstName = (String) payload.get("given_name");
                String lastName = (String) payload.get("family_name");

                googleRequest.setFirstName(firstName);
                googleRequest.setLastName(lastName);

                loginResponse = authService.registerGoogleUser(googleRequest);
            } else {
                UserAuth userDetails = userDetailsService.loadUserByEmail(email);

                loginResponse = new LoginResponse();
                loginResponse.setUserId(userDetails.getId());
                loginResponse.setEmail(email);
            }

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email,null)
            );
            String jwtToken = jwtUtil.generateToken(email);

            loginResponse.setToken(jwtToken);

            log.info("User with email {} logged in via Google successfully", email);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return ResponseHandler.generateResponse(
                HttpStatus.OK,
                false,
                "User logged in via Google successfully",
                loginResponse
        );
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Validated RegisterRequest registerRequest) {
        if (authService.userByEmailExists(registerRequest.getEmail())) {
            log.info("User with email: {} can't be created. It already exists.", registerRequest.getEmail());

            return ResponseHandler.generateResponse(
                    HttpStatus.BAD_REQUEST,
                    true,
                    "Email already exists",
                    null
            );
        }

        LoginResponse registerResponse = authService.register(registerRequest);

        System.out.println("registerResponse = " + registerResponse);
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(registerRequest.getEmail(), registerRequest.getPassword())
        );
        String jwtToken = jwtUtil.generateToken(registerResponse.getEmail());

        registerResponse.setToken(jwtToken);

        log.info("User with email {} registered successfully", registerResponse.getEmail());

        return ResponseHandler.generateResponse(
                HttpStatus.CREATED,
                false,
                "User registered successfully",
                registerResponse
        );
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Validated LoginRequest loginRequest) {
        UserAuth userDetails = userDetailsService.loadUserByEmail(loginRequest.getEmail());

        if (!authService.isValidPassword(loginRequest.getPassword(), userDetails.getPassword())) {
            log.info("Invalid password for user with email: {}", userDetails.getUsername());

            return ResponseHandler.generateResponse(
                    HttpStatus.UNAUTHORIZED,
                    true,
                    "Invalid password",
                    null
            );
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(userDetails.getUsername(), loginRequest.getPassword())
        );
        String jwtToken = jwtUtil.generateToken(userDetails.getUsername());

        log.info("User with email {} logged in successfully", userDetails.getUsername());

        return ResponseHandler.generateResponse(
                HttpStatus.OK,
                false,
                "User logged in successfully",
                new LoginResponse(userDetails.getId(), userDetails.getUsername(), jwtToken)
        );
    }
}
