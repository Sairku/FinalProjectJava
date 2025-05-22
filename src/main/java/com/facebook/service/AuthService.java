package com.facebook.service;

import com.facebook.dto.GoogleRequestDto;
import com.facebook.dto.LoginResponseDto;
import com.facebook.dto.RegisterRequestDto;
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

    public LoginResponseDto register(RegisterRequestDto registerRequest) {
        User user = modelMapper.map(registerRequest, User.class);

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setProvider(Provider.LOCAL);

        User savedUser = userRepository.save(user);

        return modelMapper.map(savedUser, LoginResponseDto.class);
    }

    public LoginResponseDto registerGoogleUser(GoogleRequestDto googleRequestDto) {
        User user = modelMapper.map(googleRequestDto, User.class);

        user.setProvider(Provider.GOOGLE);

        User savedUser = userRepository.save(user);
        LoginResponseDto loginResponseDto = new LoginResponseDto();

        loginResponseDto.setUserId(savedUser.getId());
        loginResponseDto.setEmail(savedUser.getEmail());

        return loginResponseDto;
    }

    public boolean userByEmailExists(String email) {
        Optional<User> user = userRepository.findByEmail(email);

        return user.isPresent();
    }

    public boolean isValidPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}
