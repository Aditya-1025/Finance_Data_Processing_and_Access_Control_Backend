package com.finance.dto.user;

import com.finance.enums.Role;
import com.finance.enums.UserStatus;

public record UpdateUserRequest(
        String name,
        Role role,
        UserStatus status
) {}
