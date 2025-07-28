package com.socialmedia.togetherly.exception;

import jakarta.validation.constraints.Email;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String message) {
        super(message);
    }
}
