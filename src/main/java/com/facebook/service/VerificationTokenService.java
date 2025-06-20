package com.facebook.service;

import com.facebook.exception.NotFoundException;
import com.facebook.model.User;
import com.facebook.model.VerificationToken;
import com.facebook.repository.UserRepository;
import com.facebook.repository.VerificationTokenRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@AllArgsConstructor
public class VerificationTokenService {
    private final VerificationTokenRepository verificationTokenRepository;
    private final UserRepository userRepository;

    public String createPasswordResetToken(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));

        String token = UUID.randomUUID().toString();
        LocalDateTime expireAt = LocalDateTime.now().plusHours(24);

        var verificationToken = new VerificationToken();
        verificationToken.setUser(user);
        verificationToken.setToken(token);
        verificationToken.setExpiredAt(expireAt);

        verificationTokenRepository.save(verificationToken);

        return token;
    }
}
