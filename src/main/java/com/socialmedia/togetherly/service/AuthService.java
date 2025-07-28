package com.socialmedia.togetherly.service;

import com.socialmedia.togetherly.dto.request.ForgotPasswordRequest;
import com.socialmedia.togetherly.dto.request.LoginRequest;
import com.socialmedia.togetherly.dto.request.RegisterRequest;
import com.socialmedia.togetherly.dto.request.ResetPasswordRequest;
import org.springframework.http.ResponseEntity;


public interface AuthService {
    ResponseEntity<String> register(RegisterRequest request);

    ResponseEntity<?> login(LoginRequest request);

    void verifyEmail(String token);

    void forgotPassword(ForgotPasswordRequest forgotPasswordRequest);

    void resetPassword(ResetPasswordRequest resetPasswordRequest);
}
