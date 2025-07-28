package com.socialmedia.togetherly.service.impl;

import com.socialmedia.togetherly.Config.AppProperties;
import com.socialmedia.togetherly.dto.request.ForgotPasswordRequest;
import com.socialmedia.togetherly.dto.request.LoginRequest;
import com.socialmedia.togetherly.dto.request.RegisterRequest;
import com.socialmedia.togetherly.dto.request.ResetPasswordRequest;
import com.socialmedia.togetherly.dto.response.LoginResponse;
import com.socialmedia.togetherly.exception.BadRequestException;
import com.socialmedia.togetherly.exception.RoleNotFoundException;
import com.socialmedia.togetherly.exception.UserNotFoundException;
import com.socialmedia.togetherly.model.*;
import com.socialmedia.togetherly.repositories.PasswordResetTokenRepository;
import com.socialmedia.togetherly.repositories.RoleRepository;
import com.socialmedia.togetherly.repositories.UserRepository;
import com.socialmedia.togetherly.repositories.VerificationTokenRepository;
import com.socialmedia.togetherly.security.UserDetailsImpl;
import com.socialmedia.togetherly.security.util.JwtUtil;
import com.socialmedia.togetherly.service.AuthService;
import com.socialmedia.togetherly.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class AuthServiceImpl implements AuthService {

    private final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsImpl userDetails;
    private final JwtUtil jwtUtil;
    private final VerificationTokenRepository tokenRepository;
    private final AppProperties appProperties;
    private final EmailService emailService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    public AuthServiceImpl(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, UserDetailsImpl userDetails, JwtUtil jwtUtil, VerificationTokenRepository tokenRepository, AppProperties appProperties, EmailService emailService, PasswordResetTokenRepository passwordResetTokenRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.userDetails = userDetails;
        this.jwtUtil = jwtUtil;
        this.tokenRepository = tokenRepository;
        this.appProperties = appProperties;
        this.emailService = emailService;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
    }

    @Override
    @Transactional
    public ResponseEntity<String> register(RegisterRequest request) {
        try {
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
            sendVerificationEmail(user);
            return ResponseEntity.ok("User registered successfully!");

        } catch (RoleNotFoundException e) {
            logger.error("Registration failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("User role not configured.");
        } catch (Exception e) {
            logger.error("Unexpected error during registration", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error registering user.");
        }
    }

    @Override
    public ResponseEntity<?> login(LoginRequest request) {
        try {
            logger.info("Authenticating the user...");
            doAuthenticate(request.getUsername(), request.getPassword());

            logger.info("Getting user by username: {}", request.getUsername());
            Optional<User> optionalUser = userRepository.findByUsername(request.getUsername());
            if (optionalUser.isEmpty()) {
                return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
            }

            User user = optionalUser.get();

            logger.info("Checking if the user's account is enabled.");
            if (!user.isEnabled()) {
                logger.info("User account is not enabled. Sending verification email.");
                sendVerificationEmail(user);
                return new ResponseEntity<>("Your account is not yet activated. We've sent you a new verification email — please check your inbox to complete the activation.",
                        HttpStatus.FORBIDDEN);
            }

            UserDetails userDetails1 = userDetails.loadUserByUsername(user.getUsername());
            String token = jwtUtil.generateToken(userDetails1);
            return ResponseEntity.ok(new LoginResponse(token));
        } catch (BadCredentialsException ex) {
            return new ResponseEntity<>("Invalid username or password", HttpStatus.UNAUTHORIZED);
        }
    }


    @Transactional
    public void verifyEmail(String token) {
        logger.info("Verifying token from DB...");
        VerificationToken verificationToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new BadRequestException("Invalid token"));

        User user = verificationToken.getUser();
        if (user.isEnabled()) {
            throw new BadRequestException("Email already verified");
        }

        logger.info("Verification token validated. Activating user account.");
        user.setEnabled(true);
        user.setActive(true);
        // No need to explicitly call userRepository.save(user) if user is a managed entity
        logger.info("Verification token deleted from the database.");
        tokenRepository.delete(verificationToken);
    }

    @Override
    public void forgotPassword(ForgotPasswordRequest forgotPasswordRequest) {
        User user = userRepository.findByEmail(forgotPasswordRequest.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found with Email: " + forgotPasswordRequest.getEmail()));

        logger.info("Creating Password reset token and saving into db...");
        String forgotPasswordToken = UUID.randomUUID().toString();

        PasswordResetToken passwordResetToken = PasswordResetToken.builder()
                .id(UUID.randomUUID().toString())
                .user(user)
                .expiryDate(LocalDateTime.now().plusHours(1))
                .token(forgotPasswordToken)
                .build();

        passwordResetTokenRepository.save(passwordResetToken);

        logger.info("Token saved successfully into DB!!");

        String forgotPasswordTokenUrl = appProperties.getFrontend().getBaseUrl() + "/reset-password?token=" + forgotPasswordToken;

        String emailBody = "<p>Hello " + user.getFullName() + ",</p>" +
                "<p>We received a request to reset your password.</p>" +
                "<p>You can reset your password by clicking the link below:</p>" +
                "<p><a href=\"" + forgotPasswordTokenUrl + "\">Reset Password</a></p>" +
                "<p>If you did not request a password reset, please ignore this email.</p>" +
                "<p>Best regards,<br>Your Company Team</p>";


        emailService.sendHtmlEmail(user.getEmail(), "Verify your email", emailBody);
    }

    @Transactional
    @Override
    public void resetPassword(ResetPasswordRequest resetPasswordRequest) {
        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByToken(resetPasswordRequest.getToken())
                .orElseThrow(() -> new BadRequestException("Invalid token"));

        User user = passwordResetToken.getUser();
        user.setPassword(passwordEncoder.encode(resetPasswordRequest.getNewPassword()));
        userRepository.save(user);
        passwordResetTokenRepository.delete(passwordResetToken);
    }


    private void doAuthenticate(String username, String password) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password);

        try {
            authenticationManager.authenticate(authenticationToken);
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Invalid Username or Password!");
        }
    }

    private void sendVerificationEmail(User user) {
        String verificationTokenStr = UUID.randomUUID().toString();

        logger.info("Creating verification token and saving into db...");
        VerificationToken verificationToken = VerificationToken.builder()
                .id(UUID.randomUUID().toString())
                .token(verificationTokenStr)
                .user(user)
                .expiryDate(LocalDateTime.now().plusHours(24))
                .build();


        tokenRepository.save(verificationToken);

        logger.info("Token saved successfully into DB!!");

        String verificationTokenUrl = appProperties.getFrontend().getBaseUrl() + "/verify-email?token=" + verificationTokenStr;

        String emailBody = "<p>Hello " + user.getFullName() + ",</p>" +
                "<p>Thank you for registering with us!</p>" +
                "<p>Please verify your email address by clicking the link below:</p>" +
                "<p><a href=\"" + verificationTokenUrl + "\">Verify Email</a></p>" +
                "<p>If you did not create an account, you can safely ignore this email.</p>" +
                "<p>Best regards,<br>Your Company Team</p>";

        emailService.sendHtmlEmail(user.getEmail(), "Verify your email", emailBody);
    }

}
