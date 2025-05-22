package com.facebook.service;

import com.facebook.dto.UserAuthDto;
import com.facebook.model.User;
import com.facebook.exception.NotFoundException;
import com.facebook.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@AllArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    public UserAuthDto loadUserByEmail(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new NotFoundException("User not found with email: " + email));

        return new UserAuthDto(
                user.getId(),
                user.getEmail(),
                user.getPassword(),
                user.getProvider(),
                new ArrayList<>()
        );
    }

    @Override
    public UserAuthDto loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .map(user -> new UserAuthDto(
                        user.getId(),
                        user.getEmail(),
                        user.getPassword(),
                        user.getProvider(),
                        new ArrayList<>()
                ))
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }
}
