package com.socialmedia.togetherly.service;

import com.socialmedia.togetherly.dto.request.RegisterRequest;
import com.socialmedia.togetherly.exception.RoleNotFoundException;
import com.socialmedia.togetherly.exception.UserNotFoundException;
import com.socialmedia.togetherly.model.ERole;
import com.socialmedia.togetherly.model.Role;
import com.socialmedia.togetherly.model.User;
import com.socialmedia.togetherly.repositories.RoleRepository;
import com.socialmedia.togetherly.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public ResponseEntity<String> register(RegisterRequest request) {
        try {
            if (request == null) {
                return ResponseEntity.badRequest().body("Invalid registration request.");
            }

            if (userRepository.existsByUsername(request.getUsername())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already exists.");
            }

            if (userRepository.existsByEmail(request.getEmail())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already registered.");
            }

            // Ensure role exists in DB
            Role userRole = roleRepository.findByName(ERole.ROLE_USER.name())
                    .orElseThrow(() -> new RoleNotFoundException("Role not found: ROLE_USER"));

            // Create new user
            User user = User.builder()
                    .id(UUID.randomUUID().toString())
                    .username(request.getUsername())
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .fullName(request.getFullName())
                    .isActive(false)
                    .enabled(false)
                    .roles(Collections.singletonList(userRole)) // assign role
                    .build();

            // Save user (will also save join table entry)
            userRepository.save(user);

            return ResponseEntity.ok("User registered successfully!");

        } catch (RoleNotFoundException e) {
            logger.error("Registration failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("User role not configured.");
        } catch (Exception e) {
            logger.error("Unexpected error during registration", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error registering user.");
        }
    }
}
