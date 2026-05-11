package com.brickerp.auth.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.Set;

@Data
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String phone;
    private Boolean isActive;
    private Boolean isLocked;
    private Set<String> roles;
    private LocalDateTime createdAt;
}