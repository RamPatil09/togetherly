package com.socialmedia.togetherly.exception;

public class RoleNotFoundException extends RuntimeException {

    public RoleNotFoundException(String message) {
        super(message);
    }

    public RoleNotFoundException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
