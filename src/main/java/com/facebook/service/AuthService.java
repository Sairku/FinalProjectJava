package com.facebook.service;

import com.facebook.model.User;
import com.facebook.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
//    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

//    public RegisterResponse register(RegisterRequest registerRequest) {
//        User user = modelMapper.map(registerRequest, User.class);
//
//        user.setPassword(passwordEncoder.encode(user.getPassword()));
//
//        User savedUser = userRepository.save(user);
//
//        return modelMapper.map(savedUser, RegisterResponse.class);
//    }

    public boolean userByEmailExists(String email) {
        Optional<User> user = userRepository.findByEmail(email);

        return user.isPresent();
    }

    public boolean isValidPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}
