package com.brickerp.auth.security;

import com.brickerp.auth.entity.AppUser;
import com.brickerp.auth.entity.Role;
import com.brickerp.auth.entity.Role.RoleName;
import com.brickerp.auth.repository.RoleRepository;
import com.brickerp.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        initRoles();
        initAdminUser();
    }

    private void initRoles() {
        Arrays.stream(RoleName.values()).forEach(roleName -> {
            if (roleRepository.findByName(roleName).isEmpty()) {
                Role role = Role.builder()
                        .name(roleName)
                        .description(roleName.name().replace("ROLE_", "")
                                + " role")
                        .build();
                roleRepository.save(role);
                log.info("Created role: {}", roleName);
            }
        });
    }

    private void initAdminUser() {
        if (!userRepository.existsByUsername("admin")) {
            Role adminRole = roleRepository.findByName(RoleName.ROLE_SUPER_ADMIN)
                    .orElseThrow();

            AppUser admin = AppUser.builder()
                    .username("admin")
                    .email("admin@brickerp.com")
                    .password(passwordEncoder.encode("Admin@123"))
                    .fullName("System Administrator")
                    .isLocked(false)
                    .roles(new HashSet<>(Set.of(adminRole)))
                    .build();

            userRepository.save(admin);
            log.info("Created default admin user: admin / Admin@123");
        }
    }
}