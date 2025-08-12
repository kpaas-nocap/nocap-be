package com.example.nocap.exception;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ErrorResponse {

    private final int status;
    private final String message;
}
