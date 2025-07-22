package com.facebook.controller;

import com.facebook.dto.*;
import com.facebook.enums.Provider;
import com.facebook.openapi.ErrorResponseWrapper;
import com.facebook.openapi.LoginResponseWrapper;
import com.facebook.openapi.VoidSuccessResponseWrapper;
import com.facebook.service.AuthService;
import com.facebook.service.CustomUserDetailsService;
import com.facebook.service.EmailService;
import com.facebook.service.VerificationTokenService;
import com.facebook.util.GoogleTokenVerifier;
import com.facebook.util.JwtUtil;
import com.facebook.util.ResponseHandler;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication API", description = "Endpoints for registration and login")
public class AuthController {
    private final AuthService authService;
    private final CustomUserDetailsService userDetailsService;
    private final VerificationTokenService verificationTokenService;
    private final EmailService emailService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final GoogleTokenVerifier googleTokenVerifier;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Operation(
            summary = "Google Login",
            description = "Login using Google account",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "User logged in via Google successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            implementation = LoginResponseWrapper.class
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Invalid Google token",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            implementation = ErrorResponseWrapper.class
                                    )
                            )
                    )
            }
    )
    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(@RequestBody @Valid GoogleRequestDto googleRequestDto) {
        LoginResponseDto loginResponse;

        try {
            GoogleIdToken.Payload payload = googleTokenVerifier.verify(googleRequestDto.getIdToken());
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

                googleRequestDto.setFirstName(firstName);
                googleRequestDto.setLastName(lastName);

                loginResponse = authService.registerGoogleUser(googleRequestDto);
            } else {
                UserAuthDto userDetails = userDetailsService.loadUserByEmail(email);

                loginResponse = new LoginResponseDto();
                loginResponse.setUserId(userDetails.getId());
                loginResponse.setEmail(email);
            }

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, null)
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

    @Operation(
            summary = "Register User",
            description = "Register a new user",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "User registered successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            implementation = LoginResponseWrapper.class
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Email already exists",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            implementation = ErrorResponseWrapper.class
                                    )
                            )
                    )
            }
    )
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid RegisterRequestDto registerRequest) {
        if (authService.userByEmailExists(registerRequest.getEmail())) {
            log.info("User with email: {} can't be created. It already exists.", registerRequest.getEmail());

            return ResponseHandler.generateResponse(
                    HttpStatus.BAD_REQUEST,
                    true,
                    "Email already exists",
                    null
            );
        }

        registerRequest.setGender(registerRequest.getGender().toUpperCase());

        LoginResponseDto loginResponse = authService.register(registerRequest);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(registerRequest.getEmail(), registerRequest.getPassword())
        );
        String jwtToken = jwtUtil.generateToken(loginResponse.getEmail());

        loginResponse.setToken(jwtToken);

        log.info("User with email {} registered successfully", loginResponse.getEmail());

        return ResponseHandler.generateResponse(
                HttpStatus.CREATED,
                false,
                "User registered successfully",
                loginResponse
        );
    }

    @Operation(
            summary = "Login User",
            description = "Login using email and password",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "User logged in successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            implementation = LoginResponseWrapper.class
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Should be logged in via Google or invalid password",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            implementation = ErrorResponseWrapper.class
                                    )
                            )
                    )
            }
    )
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequestDto loginRequestDto) {
        UserAuthDto userDetails = userDetailsService.loadUserByEmail(loginRequestDto.getEmail());

        if (userDetails.getProvider().equals(Provider.GOOGLE)) {
            String message = "User with email: " + userDetails.getUsername() + " can't login. User registered via Google";

            log.info(message);

            return ResponseHandler.generateResponse(
                    HttpStatus.UNAUTHORIZED,
                    true,
                    message,
                    null
            );
        }

        if (!authService.isValidPassword(loginRequestDto.getPassword(), userDetails.getPassword())) {
            log.info("Invalid password for user with email: {}", userDetails.getUsername());

            return ResponseHandler.generateResponse(
                    HttpStatus.UNAUTHORIZED,
                    true,
                    "Invalid password",
                    null
            );
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(userDetails.getUsername(), loginRequestDto.getPassword())
        );
        String jwtToken = jwtUtil.generateToken(userDetails.getUsername());

        log.info("User with email {} logged in successfully", userDetails.getUsername());

        return ResponseHandler.generateResponse(
                HttpStatus.OK,
                false,
                "User logged in successfully",
                new LoginResponseDto(userDetails.getId(), userDetails.getUsername(), jwtToken)
        );
    }

    @Operation(
            summary = "Request Password Reset",
            description = "Request a password reset link",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Password reset link sent to email",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            implementation = VoidSuccessResponseWrapper.class
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Email not found",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            implementation = ErrorResponseWrapper.class
                                    )
                            )
                    )
            }
    )
    @PostMapping("/request-reset-password")
    public ResponseEntity<?> requestPasswordReset(@RequestBody @Valid PasswordResetRequestDto passwordResetRequest) {
        String email = passwordResetRequest.getEmail();
        String token = verificationTokenService.createPasswordResetToken(email);

        log.info("Password reset token created for user with email: {}", email);

        String resetLink = frontendUrl + "/reset-password?token=" + token;
        String subject = "Reset your password";
        String body = "Hello!\n\nTo reset your password, click the link below:\n" + resetLink +
                "\n\nIf you didn't request this, ignore the email.\n\nThanks!";

        emailService.sendEmail(email, subject, body);

        return ResponseHandler.generateResponse(
                HttpStatus.OK,
                false,
                "Password reset link sent to your email " + passwordResetRequest.getEmail(),
                null
        );
    }

    @Operation(
            summary = "Reset Password",
            description = "Reset password using a token",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Password reset successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            implementation = VoidSuccessResponseWrapper.class
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid token or passwords do not match",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            implementation = ErrorResponseWrapper.class
                                    )
                            )
                    )
            }
    )
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody @Valid PasswordResetDto passwordReset) {
        authService.resetPassword(
                passwordReset.getToken(),
                passwordReset.getNewPassword(),
                passwordReset.getConfirmPassword()
        );

        log.info("Password reset successfully for token: {}", passwordReset.getToken());

        return ResponseHandler.generateResponse(
                HttpStatus.OK,
                false,
                "Password reset successfully",
                null
        );
    }
}
