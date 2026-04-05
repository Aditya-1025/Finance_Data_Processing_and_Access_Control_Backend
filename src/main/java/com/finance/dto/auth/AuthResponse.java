package com.finance.dto.auth;

import com.finance.enums.Role;

public record AuthResponse(
        String token,
        String tokenType,
        Long userId,
        String name,
        String email,
        Role role
) {
    public static AuthResponse of(String token, Long userId, String name, String email, Role role) {
        return new AuthResponse(token, "Bearer", userId, name, email, role);
    }
}
