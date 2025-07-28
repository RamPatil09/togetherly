package com.socialmedia.togetherly.controller;

import com.socialmedia.togetherly.dto.request.LoginRequest;
import com.socialmedia.togetherly.dto.request.RegisterRequest;
import com.socialmedia.togetherly.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }


    @GetMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestParam String token) {
        authService.verifyEmail(token);
        return ResponseEntity.ok("Account verified successfully!!");
    }


    @ExceptionHandler(BadCredentialsException.class)
    public String exceptionHandler() {
        return "Invalid credentials";
    }


}