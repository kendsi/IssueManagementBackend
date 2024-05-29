package com.causwe.backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserResponseDTO {

    private String username;
    private UserResponseDTO.Role role;

    public enum Role {
        ADMIN, PL, DEV, TESTER
    }

    public UserResponseDTO() {}

}
