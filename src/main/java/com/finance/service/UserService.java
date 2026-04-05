package com.finance.service;

import com.finance.dto.user.UpdateUserRequest;
import com.finance.dto.user.UserResponse;
import com.finance.entity.User;
import com.finance.enums.UserStatus;
import com.finance.exception.AppException;
import com.finance.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(AuthService::toResponse)
                .toList();
    }

    public UserResponse getUserById(Long id) {
        return AuthService.toResponse(findOrThrow(id));
    }

    public UserResponse updateUser(Long id, UpdateUserRequest req) {
        User user = findOrThrow(id);
        if (req.name() != null && !req.name().isBlank()) {
            user.setName(req.name());
        }
        if (req.role() != null) {
            user.setRole(req.role());
        }
        if (req.status() != null) {
            user.setStatus(req.status());
        }
        return AuthService.toResponse(userRepository.save(user));
    }

    public void deactivateUser(Long id) {
        User user = findOrThrow(id);
        user.setStatus(UserStatus.INACTIVE);
        userRepository.save(user);
    }

    private User findOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "User not found with id: " + id));
    }
}
