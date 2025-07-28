package com.socialmedia.togetherly.service;

import com.socialmedia.togetherly.dto.request.RegisterRequest;
import org.springframework.http.ResponseEntity;


public interface UserService {
    ResponseEntity<String> register(RegisterRequest request);

}
