package com.brickerp.auth.service;

import com.brickerp.auth.dto.*;
import java.util.List;

public interface AuthService {
    AuthResponse login(LoginRequest request);

    UserResponse register(RegisterRequest request);

    List<UserResponse> getAllUsers();

    UserResponse getUserById(Long id);

    UserResponse updateUserRoles(Long id, java.util.Set<String> roles);

    void lockUser(Long id);

    void unlockUser(Long id);

    void deleteUser(Long id);

    void changePassword(String username, ChangePasswordRequest request);
}