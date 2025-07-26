package com.socialmedia.togetherly.exception.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExceptionMessage {

    private HttpStatus httpStatus;
    private String message;
}
