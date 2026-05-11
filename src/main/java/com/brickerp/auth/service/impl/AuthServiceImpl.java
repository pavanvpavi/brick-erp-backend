package com.brickerp.auth.service.impl;

import com.brickerp.auth.dto.*;
import com.brickerp.auth.entity.AppUser;
import com.brickerp.auth.entity.Role;
import com.brickerp.auth.entity.Role.RoleName;
import com.brickerp.auth.repository.RoleRepository;
import com.brickerp.auth.repository.UserRepository;
import com.brickerp.auth.security.JwtUtils;
import com.brickerp.auth.service.AuthService;
import com.brickerp.common.exception.BusinessException;
import com.brickerp.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    @Override
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(), request.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtUtils.generateToken(authentication);

        AppUser user = userRepository.findByUsername(request.getUsername())
                .orElseThrow();

        Set<String> roles = user.getRoles().stream()
                .map(r -> r.getName().name())
                .collect(Collectors.toSet());

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .roles(roles)
                .build();
    }

    @Override
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("Username '" + request.getUsername() + "' already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email '" + request.getEmail() + "' already registered");
        }

        Set<Role> roles = new HashSet<>();
        if (request.getRoles() == null || request.getRoles().isEmpty()) {
            roleRepository.findByName(RoleName.ROLE_VIEWER)
                    .ifPresent(roles::add);
        } else {
            for (String roleName : request.getRoles()) {
                try {
                    RoleName rn = RoleName.valueOf(roleName);
                    roleRepository.findByName(rn).ifPresent(roles::add);
                } catch (IllegalArgumentException e) {
                    throw new BusinessException("Invalid role: " + roleName);
                }
            }
        }

        AppUser user = AppUser.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .isLocked(false)
                .roles(roles)
                .build();

        return toUserResponse(userRepository.save(user));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findByIsActiveTrue()
                .stream().map(this::toUserResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        return userRepository.findById(id)
                .map(this::toUserResponse)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    @Override
    public UserResponse updateUserRoles(Long id, Set<String> roleNames) {
        AppUser user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        Set<Role> roles = new HashSet<>();
        for (String roleName : roleNames) {
            try {
                RoleName rn = RoleName.valueOf(roleName);
                roleRepository.findByName(rn).ifPresent(roles::add);
            } catch (IllegalArgumentException e) {
                throw new BusinessException("Invalid role: " + roleName);
            }
        }

        user.setRoles(roles);
        return toUserResponse(userRepository.save(user));
    }

    @Override
    public void lockUser(Long id) {
        AppUser user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        user.setIsLocked(true);
        userRepository.save(user);
    }

    @Override
    public void unlockUser(Long id) {
        AppUser user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        user.setIsLocked(false);
        userRepository.save(user);
    }

    @Override
    public void deleteUser(Long id) {
        AppUser user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        user.setIsActive(false);
        userRepository.save(user);
    }

    @Override
    public void changePassword(String username, ChangePasswordRequest request) {
        AppUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found: " + username));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BusinessException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    private UserResponse toUserResponse(AppUser u) {
        UserResponse r = new UserResponse();
        r.setId(u.getId());
        r.setUsername(u.getUsername());
        r.setEmail(u.getEmail());
        r.setFullName(u.getFullName());
        r.setPhone(u.getPhone());
        r.setIsActive(u.getIsActive());
        r.setIsLocked(u.getIsLocked());
        r.setCreatedAt(u.getCreatedAt());
        r.setRoles(u.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet()));
        return r;
    }
}