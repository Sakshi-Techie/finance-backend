package com.zorvyn.finance.service;

import com.zorvyn.finance.dto.UpdateUserRequest;
import com.zorvyn.finance.dto.UserResponse;
import com.zorvyn.finance.exception.ResourceNotFoundException;
import com.zorvyn.finance.model.User;
import com.zorvyn.finance.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserResponse::fromUser)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        return UserResponse.fromUser(user);
    }

    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        if (request.getName() != null && !request.getName().isBlank()) {
            user.setName(request.getName());
        }
        if (request.getRole() != null) {
            user.setRole(request.getRole());
            log.info("User {} role updated to {}", user.getEmail(), request.getRole());
        }
        if (request.getActive() != null) {
            user.setActive(request.getActive());
            log.info("User {} status set to active={}", user.getEmail(), request.getActive());
        }

        user = userRepository.save(user);
        return UserResponse.fromUser(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        // Soft delete by deactivating
        user.setActive(false);
        userRepository.save(user);
        log.info("User {} deactivated (soft deleted)", user.getEmail());
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUserProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return UserResponse.fromUser(user);
    }
}
