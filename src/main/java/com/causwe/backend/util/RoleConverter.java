package com.causwe.backend.util;

import com.causwe.backend.dto.UserRequestDTO;
import com.causwe.backend.model.User;

public class RoleConverter {
    public static User.Role convertToUserRole(UserRequestDTO.Role dtoRole) {
        if (dtoRole == null) {
            return null;
        }
        switch (dtoRole) {
            case ADMIN:
                return User.Role.ADMIN;
            case PL:
                return User.Role.PL;
            case DEV:
                return User.Role.DEV;
            case TESTER:
                return User.Role.TESTER;
            default:
                throw new IllegalArgumentException("Unknown role: " + dtoRole);
        }
    }
}
