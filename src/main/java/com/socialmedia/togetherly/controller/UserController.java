package com.socialmedia.togetherly.controller;

import com.socialmedia.togetherly.dto.request.RegisterRequest;
import com.socialmedia.togetherly.dto.response.ApiResponse;
import com.socialmedia.togetherly.service.UserService;
import com.socialmedia.togetherly.service.UserServiceImpl;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request, BindingResult bindingResult) {

        ResponseEntity<?> errorResponse = validateError(bindingResult);
        if (errorResponse != null) {
            logger.warn("Validation failed: {}", errorResponse.getBody());
            return errorResponse;
        }

        userService.register(request);
        logger.info("User registered successfully");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse("User registered Successfully!!", true));
    }

    private ResponseEntity<?> validateError(BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage()));
            return ResponseEntity.badRequest().body(errors);
        }
        return null;
    }


}
