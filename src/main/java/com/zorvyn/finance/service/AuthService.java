package com.zorvyn.finance.service;

import com.zorvyn.finance.dto.LoginRequest;
import com.zorvyn.finance.dto.LoginResponse;
import com.zorvyn.finance.dto.RegisterRequest;
import com.zorvyn.finance.dto.UserResponse;
import com.zorvyn.finance.exception.BadRequestException;
import com.zorvyn.finance.model.User;
import com.zorvyn.finance.repository.UserRepository;
import com.zorvyn.finance.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtUtils.generateToken(userDetails);

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow();

        log.info("User logged in: {}", user.getEmail());

        return LoginResponse.builder()
                .token(token)
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already registered: " + request.getEmail());
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole() != null ? request.getRole() : com.zorvyn.finance.model.Role.VIEWER)
                .active(true)
                .build();

        user = userRepository.save(user);
        log.info("New user registered: {} with role {}", user.getEmail(), user.getRole());
        return UserResponse.fromUser(user);
    }
}
