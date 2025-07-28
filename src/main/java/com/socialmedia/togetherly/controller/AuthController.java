package com.socialmedia.togetherly.controller;

import com.socialmedia.togetherly.dto.request.LoginRequest;
import com.socialmedia.togetherly.dto.request.RegisterRequest;
import com.socialmedia.togetherly.dto.response.LoginResponse;
import com.socialmedia.togetherly.model.ERole;
import com.socialmedia.togetherly.model.Role;
import com.socialmedia.togetherly.repositories.RoleRepository;
import com.socialmedia.togetherly.security.UserDetailsImpl;
import com.socialmedia.togetherly.security.util.JwtUtil;
import com.socialmedia.togetherly.service.UserService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserDetailsImpl userDetails;
    private final UserService userService;
    private final RoleRepository roleRepository;

    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil, UserDetailsImpl userDetails, UserService userService, RoleRepository roleRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userDetails = userDetails;
        this.userService = userService;
        this.roleRepository = roleRepository;
    }


    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        return userService.register(request);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        this.doAuthenticate(request.getUsername(), request.getPassword());

        UserDetails userDetails1 = userDetails.loadUserByUsername(request.getUsername());
        String token = this.jwtUtil.generateToken(userDetails1);

        LoginResponse loginResponse = new LoginResponse(token);
        return new ResponseEntity<>(loginResponse, HttpStatus.OK);
    }

    private void doAuthenticate(String username, String password) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password);

        try {
            authenticationManager.authenticate(authenticationToken);
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Invalid Username or Password!");
        }
    }

    @ExceptionHandler(BadCredentialsException.class)
    public String exceptionHandler() {
        return "Invalid credentials";
    }

}