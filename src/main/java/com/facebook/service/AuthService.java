package com.facebook.service;

import com.facebook.dto.GoogleRequest;
import com.facebook.dto.LoginResponse;
import com.facebook.dto.RegisterRequest;
import com.facebook.enums.Provider;
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
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    public LoginResponse register(RegisterRequest registerRequest) {
        User user = modelMapper.map(registerRequest, User.class);

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setProvider(Provider.LOCAL);

        User savedUser = userRepository.save(user);

        return modelMapper.map(savedUser, LoginResponse.class);
    }

    public LoginResponse registerGoogleUser(GoogleRequest googleRequest) {
        User user = modelMapper.map(googleRequest, User.class);

        user.setProvider(Provider.GOOGLE);

        User savedUser = userRepository.save(user);
        LoginResponse loginResponse = new LoginResponse();

        loginResponse.setUserId(savedUser.getId());
        loginResponse.setEmail(savedUser.getEmail());

        return loginResponse;
    }

    public boolean userByEmailExists(String email) {
        Optional<User> user = userRepository.findByEmail(email);

        return user.isPresent();
    }

    public boolean isValidPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}
