package com.socialmedia.togetherly.exception.exceptionhandler;

import com.socialmedia.togetherly.exception.UserAlreadyExistException;
import com.socialmedia.togetherly.exception.UserNotFoundException;
import com.socialmedia.togetherly.exception.response.ExceptionMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.HashMap;
import java.util.Map;


@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ExceptionMessage userNotFoundException(UserNotFoundException exception) {
        ExceptionMessage message = new ExceptionMessage(HttpStatus.NOT_FOUND, exception.getMessage());
        return message;
    }

    @ExceptionHandler(UserAlreadyExistException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.CONFLICT)
    public ExceptionMessage userAlreadyExistException(UserAlreadyExistException exception) {
        ExceptionMessage message = new ExceptionMessage(HttpStatus.CONFLICT, exception.getMessage());
        return message;
    }

}
